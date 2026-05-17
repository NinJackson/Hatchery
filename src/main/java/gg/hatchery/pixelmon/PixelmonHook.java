package gg.hatchery.pixelmon;

import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.egg.EggGroup;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.pokemon.species.gender.Gender;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import gg.hatchery.Hatchery;
import gg.hatchery.daycare.Daycare;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Real Pixelmon API integration for Hatchery.
 *
 * NMS-touching paths (sprite item conversion) use reflection so the plugin
 * compiles without a Mojang-mapped Minecraft jar on the classpath. Arclight
 * provides Mojang-mapped NMS classes at runtime.
 */
public class PixelmonHook {

    private final Hatchery plugin;
    private final boolean present;

    private Method craftAsBukkitCopy;          // CraftItemStack.asBukkitCopy(nms ItemStack)
    private Method spriteHelperGetPhoto;        // SpriteItemHelper.getPhoto(Pokemon)
    private Method storageProxyGetPartyNow;     // StorageProxy.getPartyNow(UUID)

    public PixelmonHook(Hatchery plugin) {
        this.plugin = plugin;
        boolean p = false;
        try {
            Class.forName("com.pixelmonmod.pixelmon.Pixelmon");
            p = true;
        } catch (ClassNotFoundException ignored) {}
        this.present = p;

        if (p) {
            try {
                // Sprite helper: returns NMS ItemStack
                Class<?> spriteHelper = Class.forName(
                        "com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper");
                this.spriteHelperGetPhoto = spriteHelper.getMethod("getPhoto", Pokemon.class);

                // CraftItemStack lookup
                String pkg = Bukkit.getServer().getClass().getPackage().getName();
                String version = pkg.substring(pkg.lastIndexOf('.') + 1);
                Class<?> craft = Class.forName(
                        "org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
                Class<?> nmsItemStack = Class.forName("net.minecraft.world.item.ItemStack");
                this.craftAsBukkitCopy = craft.getMethod("asBukkitCopy", nmsItemStack);

                // StorageProxy.getPartyNow(UUID) — find via reflection to skip ServerPlayer overload
                Class<?> storageProxy = Class.forName(
                        "com.pixelmonmod.pixelmon.api.storage.StorageProxy");
                for (Method m : storageProxy.getMethods()) {
                    if (m.getName().equals("getPartyNow")
                            && m.getParameterCount() == 1
                            && m.getParameterTypes()[0] == UUID.class) {
                        this.storageProxyGetPartyNow = m;
                        break;
                    }
                }
            } catch (Throwable t) {
                plugin.getLogger().warning("Pixelmon API reflection setup failed: " + t.getMessage());
            }
        }
    }

    public boolean isPixelmonPresent() { return present; }

    /* ----------------------------------------------------------------
     * Party access
     * ---------------------------------------------------------------- */

    /** Returns the player's party (size 6, slots may be null). */
    public Pokemon[] getParty(Player player) {
        PlayerPartyStorage storage = partyOf(player);
        return storage == null ? new Pokemon[6] : storage.getAll();
    }

    public Pokemon partySlot(Player player, int slot) {
        Pokemon[] party = getParty(player);
        if (slot < 0 || slot >= party.length) return null;
        return party[slot];
    }

    public boolean removeFromPartySlot(Player player, int slot) {
        PlayerPartyStorage storage = partyOf(player);
        if (storage == null) return false;
        try {
            storage.set(new StoragePosition(-1, slot), null);
            return true;
        } catch (Throwable t) {
            plugin.getLogger().warning("removeFromPartySlot failed: " + t.getMessage());
            return false;
        }
    }

    public boolean addToParty(Player player, Pokemon p) {
        if (p == null) return false;
        PlayerPartyStorage storage = partyOf(player);
        if (storage == null) return false;
        try {
            storage.addAndGetPosition(p);
            return true;
        } catch (Throwable t) {
            plugin.getLogger().warning("addToParty failed: " + t.getMessage());
            return false;
        }
    }

    private PlayerPartyStorage partyOf(Player player) {
        if (storageProxyGetPartyNow == null) return null;
        try {
            return (PlayerPartyStorage) storageProxyGetPartyNow.invoke(null, player.getUniqueId());
        } catch (Throwable t) {
            plugin.getLogger().warning("partyOf failed: " + t.getMessage());
            return null;
        }
    }

    /* ----------------------------------------------------------------
     * Compatibility (strict)
     * ---------------------------------------------------------------- */

    public boolean isCompatible(Pokemon a, Pokemon b) {
        if (a == null || b == null) return false;
        Stats sa = a.getForm();
        Stats sb = b.getForm();
        if (sa == null || sb == null) return false;

        boolean aDitto = isDitto(a);
        boolean bDitto = isDitto(b);
        if (aDitto && bDitto) return false;

        List<EggGroup> ga = sa.getEggGroups();
        List<EggGroup> gb = sb.getEggGroups();
        if (containsUndiscovered(ga) || containsUndiscovered(gb)) return false;

        if (!aDitto && !bDitto) {
            boolean canBreed = false;
            for (EggGroup g : ga) {
                if (g.canBreedWith(gb)) { canBreed = true; break; }
            }
            if (!canBreed) return false;

            Gender genderA = a.getGender();
            Gender genderB = b.getGender();
            if (genderA == null || genderB == null) return false;
            if (genderA == Gender.NONE || genderB == Gender.NONE) return false;
            if (genderA == genderB) return false;
        }
        return true;
    }

    private boolean isDitto(Pokemon p) {
        return p.getSpecies() != null && "ditto".equalsIgnoreCase(p.getSpecies().getName());
    }

    private boolean containsUndiscovered(List<EggGroup> list) {
        if (list == null) return true;
        for (EggGroup g : list) {
            String key = g.getKey();
            if (key != null && key.toLowerCase(Locale.ROOT).contains("undiscovered")) return true;
        }
        return false;
    }

    /* ----------------------------------------------------------------
     * Egg generation
     * ---------------------------------------------------------------- */

    public Pokemon makeEgg(Pokemon a, Pokemon b) {
        if (!isCompatible(a, b)) return null;
        Pokemon base = isDitto(a) ? b : a;
        if (base == null) return null;
        try {
            return base.makeEgg();
        } catch (Throwable t) {
            plugin.getLogger().warning("makeEgg failed: " + t.getMessage());
            return null;
        }
    }

    /* ----------------------------------------------------------------
     * Type helpers
     * ---------------------------------------------------------------- */

    public List<String> getTypes(Pokemon p) {
        if (p == null || p.getForm() == null) return Collections.emptyList();
        List<Element> els = p.getForm().getTypes();
        if (els == null || els.isEmpty()) return Collections.emptyList();
        List<String> out = new ArrayList<>(els.size());
        for (Element e : els) {
            // Element.getName() returns the display name like "Fire", "Water".
            out.add(e.getName().toLowerCase(Locale.ROOT));
        }
        return out;
    }

    public List<String> getPairTypes(String pairJson) {
        Pokemon[] pair = PokemonNbtCodec.decodePair(pairJson);
        Set<String> types = new LinkedHashSet<>();
        if (pair[0] != null) types.addAll(getTypes(pair[0]));
        if (pair[1] != null) types.addAll(getTypes(pair[1]));
        return new ArrayList<>(types);
    }

    public String pokemonDisplayName(Pokemon p) {
        if (p == null) return "<empty>";
        return p.getSpecies() == null ? "?" : p.getSpecies().getName();
    }

    /** Returns a Bukkit ItemStack showing the Pokemon's sprite, or null. */
    public ItemStack spriteItem(Pokemon p) {
        if (p == null || craftAsBukkitCopy == null || spriteHelperGetPhoto == null) return null;
        try {
            Object nmsStack = spriteHelperGetPhoto.invoke(null, p);
            return (ItemStack) craftAsBukkitCopy.invoke(null, nmsStack);
        } catch (Throwable t) {
            return null;
        }
    }

    /* ----------------------------------------------------------------
     * Notifications
     * ---------------------------------------------------------------- */

    public void notifyEggReady(Daycare d) {
        Player p = Bukkit.getPlayer(d.getOwner());
        if (p != null && p.isOnline()) {
            p.sendMessage(plugin.getConfigManager().getMessages().get("breeding.egg-ready",
                    "world", d.getWorldName(),
                    "x", d.getX(), "y", d.getY(), "z", d.getZ()));
        }
    }
}
