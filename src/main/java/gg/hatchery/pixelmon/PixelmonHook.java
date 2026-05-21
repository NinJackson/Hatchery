package gg.hatchery.pixelmon;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.egg.EggGroup;
import com.pixelmonmod.pixelmon.api.pokemon.species.gender.Gender;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import gg.hatchery.Hatchery;
import gg.hatchery.daycare.Daycare;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Real Pixelmon API integration for Hatchery.
 */
public class PixelmonHook {

    private final Hatchery plugin;
    private final boolean present;

    /** Cached reflection: org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack#asBukkitCopy */
    private Method craftAsBukkitCopy;

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
                // 1.21.1 Arclight
                Class<?> craft = Class.forName("org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack");
                this.craftAsBukkitCopy = craft.getMethod("asBukkitCopy",
                        net.minecraft.world.item.ItemStack.class);
            } catch (Throwable t) {
                plugin.getLogger().warning("Could not locate CraftItemStack - sprite icons will be unavailable.");
            }
        }
    }

    public boolean isPixelmonPresent() { return present; }

    /* ----------------------------------------------------------------
     * Party access
     * ---------------------------------------------------------------- */

    /** Returns the player's party (size 6, slots may be null). */
    public Pokemon[] getParty(Player player) {
        PlayerPartyStorage storage = StorageProxy.getPartyNow(player.getUniqueId());
        return storage == null ? new Pokemon[6] : storage.getAll();
    }

    public Pokemon partySlot(Player player, int slot) {
        Pokemon[] party = getParty(player);
        if (slot < 0 || slot >= party.length) return null;
        return party[slot];
    }

    public boolean removeFromPartySlot(Player player, int slot) {
        PlayerPartyStorage storage = StorageProxy.getPartyNow(player.getUniqueId());
        if (storage == null) return false;
        try {
            com.pixelmonmod.pixelmon.api.storage.StoragePosition pos =
                    new com.pixelmonmod.pixelmon.api.storage.StoragePosition(-1, slot);
            Pokemon before = storage.get(pos);
            if (before == null) return false;

            storage.set(pos, null);
            invokeIfPresent(storage, "setOriginal",
                    new Class<?>[]{com.pixelmonmod.pixelmon.api.storage.StoragePosition.class, Pokemon.class},
                    new Object[]{pos, null});
            invokeIfPresent(storage, "setNeedsSaving", new Class<?>[0], new Object[0]);
            invokeIfPresent(storage, "sendClientUpdatePacket", new Class<?>[0], new Object[0]);
            invokeIfPresent(storage, "sendClientUpdateSelectedPacket", new Class<?>[0], new Object[0]);

            if (containsPokemonUuid(storage.getAll(), before)
                    || containsPokemonUuid(originalParty(storage), before)) {
                plugin.getLogger().warning("Refusing daycare placement: Pixelmon party slot "
                        + slot + " still contains " + before.getUUID() + " after removal.");
                return false;
            }
            return true;
        } catch (Throwable t) {
            plugin.getLogger().warning("removeFromPartySlot failed: " + t.getMessage());
            return false;
        }
    }

    private Object invokeIfPresent(Object target, String method, Class<?>[] types, Object[] args) {
        try {
            Method m = target.getClass().getMethod(method, types);
            return m.invoke(target, args);
        } catch (NoSuchMethodException ignored) {
            return null;
        } catch (Throwable t) {
            plugin.getLogger().warning(method + " failed: " + t.getMessage());
            return null;
        }
    }

    private Pokemon[] originalParty(PlayerPartyStorage storage) {
        Object result = invokeIfPresent(storage, "getOriginalParty", new Class<?>[0], new Object[0]);
        return result instanceof Pokemon[] ? (Pokemon[]) result : new Pokemon[0];
    }

    private boolean containsPokemonUuid(Pokemon[] party, Pokemon target) {
        if (party == null || target == null || target.getUUID() == null) return false;
        for (Pokemon p : party) {
            if (p != null && target.getUUID().equals(p.getUUID())) return true;
        }
        return false;
    }

    public boolean addToParty(Player player, Pokemon p) {
        if (p == null) return false;
        PlayerPartyStorage storage = StorageProxy.getPartyNow(player.getUniqueId());
        if (storage == null) return false;
        try {
            storage.addAndGetPosition(p);
            return true;
        } catch (Throwable t) {
            plugin.getLogger().warning("addToParty failed: " + t.getMessage());
            return false;
        }
    }

    public boolean hasPokemonInParty(Player player, Pokemon pokemon) {
        if (pokemon == null) return false;
        PlayerPartyStorage storage = StorageProxy.getPartyNow(player.getUniqueId());
        if (storage == null) return false;
        return containsPokemonUuid(storage.getAll(), pokemon)
                || containsPokemonUuid(originalParty(storage), pokemon);
    }

    /* ----------------------------------------------------------------
     * Compatibility (strict)
     * ---------------------------------------------------------------- */

    public boolean isCompatible(Pokemon a, Pokemon b) {
        if (a == null || b == null) return false;
        Stats sa = a.getForm();
        Stats sb = b.getForm();
        if (sa == null || sb == null) return false;

        // Rule: same species can't always breed (e.g. genderless mostly can't except Ditto)
        // Easy short-circuit: Ditto matches any non-undiscovered, except another Ditto.
        boolean aDitto = isDitto(a);
        boolean bDitto = isDitto(b);
        if (aDitto && bDitto) return false;

        // Egg group check (must share a non-undiscovered group, or one is ditto)
        List<EggGroup> ga = sa.getEggGroups();
        List<EggGroup> gb = sb.getEggGroups();
        if (containsUndiscovered(ga) || containsUndiscovered(gb)) return false;

        if (!aDitto && !bDitto) {
            // Use Pixelmon's built-in compatibility check (egg groups)
            boolean canBreed = false;
            for (EggGroup g : ga) {
                if (g.canBreedWith(gb)) { canBreed = true; break; }
            }
            if (!canBreed) return false;

            // Gender check (must be male + female)
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
        Pokemon base;
        boolean aDitto = isDitto(a);
        boolean bDitto = isDitto(b);
        if (aDitto && !bDitto) {
            base = b;
        } else if (bDitto && !aDitto) {
            base = a;
        } else {
            base = pickFemale(a, b);
        }
        if (base == null) return null;
        try {
            Pokemon egg = base.makeEgg();
            clearHeldItem(egg);
            return egg;
        } catch (Throwable t) {
            plugin.getLogger().warning("makeEgg failed: " + t.getMessage());
            return null;
        }
    }

    public void clearHeldItem(Pokemon p) {
        if (p == null) return;
        try {
            p.setHeldItem(emptyNativeItemStack());
        } catch (Throwable t) {
            plugin.getLogger().warning("clearHeldItem failed: " + t.getMessage());
        }
    }

    private net.minecraft.world.item.ItemStack emptyNativeItemStack() throws ReflectiveOperationException {
        try {
            return (net.minecraft.world.item.ItemStack)
                    net.minecraft.world.item.ItemStack.class.getField("EMPTY").get(null);
        } catch (NoSuchFieldException ignored) {
            return (net.minecraft.world.item.ItemStack)
                    net.minecraft.world.item.ItemStack.class.getField("f_41583_").get(null);
        }
    }

    public boolean isSamePokemon(Pokemon a, Pokemon b) {
        return a != null && b != null && Objects.equals(a.getUUID(), b.getUUID());
    }

    private Pokemon pickFemale(Pokemon a, Pokemon b) {
        if (a.getGender() == Gender.FEMALE) return a;
        if (b.getGender() == Gender.FEMALE) return b;
        return a;
    }

    /* ----------------------------------------------------------------
     * Type / display helpers
     * ---------------------------------------------------------------- */

    public List<String> getTypes(Pokemon p) {
        if (p == null || p.getForm() == null) return Collections.emptyList();
        List<?> els = p.getForm().getTypes();
        if (els == null || els.isEmpty()) return Collections.emptyList();
        List<String> out = new ArrayList<>(els.size());
        for (Object e : els) {
            String key = typeKey(e);
            if (key != null && !key.isBlank()) out.add(key);
        }
        return out;
    }

    private String typeKey(Object type) {
        return typeKey(type, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private String typeKey(Object type, Set<Object> seen) {
        if (type == null) return null;
        if (!seen.add(type)) return null;
        if (type instanceof Optional<?>) {
            Optional<?> optional = (Optional<?>) type;
            return optional.map(value -> typeKey(value, seen)).orElse(null);
        }
        for (String method : Arrays.asList("unwrapKey", "location", "getKey", "key", "value")) {
            try {
                Object value = type.getClass().getMethod(method).invoke(type);
                String key = typeKey(value, seen);
                if (key != null && !key.isBlank()) return key;
            } catch (Throwable ignored) {
            }
        }
        for (String method : Arrays.asList("name", "getName", "getKey")) {
            try {
                Object value = type.getClass().getMethod(method).invoke(type);
                String key = stringKey(value.toString());
                if (key != null && !key.isBlank()) return key;
            } catch (Throwable ignored) {
            }
        }
        return stringKey(type.toString());
    }

    private String stringKey(String value) {
        if (value == null) return null;
        String key = value.toLowerCase(Locale.ROOT);
        int namespace = key.lastIndexOf(':');
        if (namespace >= 0 && namespace + 1 < key.length()) {
            key = key.substring(namespace + 1);
        }
        int bracket = key.indexOf(']');
        if (bracket >= 0) key = key.substring(0, bracket);
        return key.replaceAll("[^a-z0-9_\\-]", "");
    }

    /** Returns the merged types of both parents (de-duplicated). */
    public List<String> getPairTypes(String pairJson) {
        Pokemon[] pair = PokemonNbtCodec.decodePair(pairJson);
        return getPairTypes(pair);
    }

    public List<String> getPairTypes(Daycare daycare) {
        return getPairTypes(daycare.decodedPair());
    }

    private List<String> getPairTypes(Pokemon[] pair) {
        Set<String> types = new LinkedHashSet<>();
        if (pair[0] != null) types.addAll(getTypes(pair[0]));
        if (pair[1] != null) types.addAll(getTypes(pair[1]));
        return new ArrayList<>(types);
    }

    public String pokemonDisplayName(Pokemon p) {
        if (p == null) return "<empty>";
        return p.getSpecies() == null ? "?" : p.getSpecies().getName();
    }

    /** Returns a Bukkit ItemStack showing the Pokemon's sprite (Pixelmon photo item). */
    public ItemStack spriteItem(Pokemon p) {
        if (p == null || craftAsBukkitCopy == null) return null;
        try {
            net.minecraft.world.item.ItemStack native_ = SpriteItemHelper.getPhoto(p);
            return (ItemStack) craftAsBukkitCopy.invoke(null, native_);
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
