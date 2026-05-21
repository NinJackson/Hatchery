package gg.hatchery.ui;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import gg.hatchery.Hatchery;
import gg.hatchery.config.MainConfig;
import gg.hatchery.config.MessagesConfig;
import gg.hatchery.daycare.Daycare;
import gg.hatchery.pixelmon.PixelmonHook;
import gg.hatchery.pixelmon.PokemonNbtCodec;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Main daycare GUI: filler-padded chest with two parent slots, status panel,
 * and an egg-collect button when an egg is ready.
 */
public class DaycareMenu implements HatcheryMenu {

    private static final int SIZE          = 27;
    private static final int SLOT_PARENT_A = 11;
    private static final int SLOT_PARENT_B = 15;
    private static final int SLOT_STATUS   = 22;
    private static final int SLOT_EGG      = 4;

    private final Hatchery plugin;
    private final Daycare daycare;
    private final Inventory inv;

    private DaycareMenu(Hatchery plugin, Player viewer, Daycare daycare) {
        this.plugin = plugin;
        this.daycare = daycare;
        MessagesConfig msgs = plugin.getConfigManager().getMessages();
        this.inv = Bukkit.createInventory(viewer, SIZE,
                MessagesConfig.color(msgs.raw("gui.title")));
        rebuild();
    }

    public static void open(Hatchery plugin, Player viewer, Daycare daycare) {
        DaycareMenu m = new DaycareMenu(plugin, viewer, daycare);
        plugin.getMenuManager().track(viewer, m);
        viewer.openInventory(m.inv);
    }

    @Override public Inventory getInventory() { return inv; }

    public Daycare daycare() { return daycare; }

    /** Re-renders the menu: fills every slot with the configured filler,
     *  then overlays the interactive items. */
    public void rebuild() {
        ItemStack filler = makeFiller();
        for (int i = 0; i < SIZE; i++) inv.setItem(i, filler);

        PixelmonHook hook = plugin.getPixelmonHook();
        Pokemon[] pair = PokemonNbtCodec.decodePair(daycare.getPairJson());

        inv.setItem(SLOT_PARENT_A, renderParent(hook, pair[0], "Parent A"));
        inv.setItem(SLOT_PARENT_B, renderParent(hook, pair[1], "Parent B"));
        inv.setItem(SLOT_STATUS,   renderStatus());
        if (daycare.getEggCount() > 0) {
            inv.setItem(SLOT_EGG, renderEggButton());
        }
    }

    private ItemStack makeFiller() {
        String id = plugin.getConfigManager().getMain().getGuiFillerItem();
        Material mat = Material.matchMaterial(id);
        if (mat == null) mat = Material.BLACK_STAINED_GLASS_PANE;
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            meta.setLore(Collections.emptyList());
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private ItemStack renderParent(PixelmonHook hook, Pokemon p, String label) {
        if (p == null) {
            return simple(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§e" + label,
                    Arrays.asList("§7Click to add a Pokemon from your party."));
        }
        ItemStack sprite = hook.spriteItem(p);
        if (sprite == null) sprite = new ItemStack(Material.EGG);
        ItemMeta m = sprite.getItemMeta();
        if (m != null) {
            m.setDisplayName(MessagesConfig.color("§a" + label + ": §f" + hook.pokemonDisplayName(p)));
            List<String> lore = new ArrayList<>();
            lore.add(MessagesConfig.color("§7Types: §f" + String.join(", ", hook.getTypes(p))));
            lore.add(MessagesConfig.color("§7Gender: §f" + (p.getGender() == null ? "?" : p.getGender().name())));
            lore.add("");
            lore.add(MessagesConfig.color("§eShift-click to retrieve."));
            m.setLore(lore);
            sprite.setItemMeta(m);
        }
        return sprite;
    }

    private ItemStack renderStatus() {
        MainConfig main = plugin.getConfigManager().getMain();
        Pokemon[] pair = PokemonNbtCodec.decodePair(daycare.getPairJson());
        List<String> types = plugin.getPixelmonHook().getPairTypes(daycare.getPairJson());
        int envPoints = plugin.getBreedingEngine().scanner().totalPoints(daycare, types);
        MainConfig.SatisfactionLevel sat = main.resolveSatisfaction(envPoints);

        List<String> lore = new ArrayList<>();
        lore.add(MessagesConfig.color("§7Owner: §f" + Bukkit.getOfflinePlayer(daycare.getOwner()).getName()));
        lore.add(MessagesConfig.color("§7Progress: §f" + daycare.getProgressPoints()
                + " / " + main.getBasePointsNeeded()));
        lore.add(MessagesConfig.color("§7Satisfaction: " + sat.color + sat.name
                + " §7(" + envPoints + ")"));
        lore.add(MessagesConfig.color("§7Eggs ready: §f" + daycare.getEggCount()));
        lore.add(MessagesConfig.color("§7Upgrades: §f" + daycare.getUpgradeLevel()
                + " / " + main.getUpgradeMaxLevels()));
        lore.add(MessagesConfig.color("§7Scan radius: §f"
                + plugin.getBreedingEngine().scanner().scanRadiusFor(daycare)));
        if (pair[0] != null && pair[1] != null
                && !plugin.getPixelmonHook().isCompatible(pair[0], pair[1])) {
            lore.add("");
            lore.add(MessagesConfig.color("§cThese Pokemon are not compatible."));
        }
        return simple(Material.BOOK, "§eDaycare Status", lore);
    }

    private ItemStack renderEggButton() {
        return simple(Material.TURTLE_EGG, "§aCollect Egg",
                Arrays.asList(
                        "§7Eggs available: §f" + daycare.getEggCount(),
                        "",
                        "§eClick to collect."));
    }

    private ItemStack simple(Material mat, String name, List<String> lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        if (m != null) {
            m.setDisplayName(MessagesConfig.color(name));
            List<String> coloured = new ArrayList<>();
            for (String l : lore) coloured.add(MessagesConfig.color(l));
            m.setLore(coloured);
            i.setItemMeta(m);
        }
        return i;
    }

    @Override
    public void onClick(Player viewer, InventoryClickEvent e) {
        int slot = e.getRawSlot();
        if (slot == SLOT_PARENT_A) handleParent(viewer, 0, e.isShiftClick());
        else if (slot == SLOT_PARENT_B) handleParent(viewer, 1, e.isShiftClick());
        else if (slot == SLOT_EGG && daycare.getEggCount() > 0) handleEggCollect(viewer);
        // filler / status / out-of-range clicks: no-op (cancelled by listener)
    }

    private void handleParent(Player viewer, int slotIndex, boolean shift) {
        Pokemon[] pair = PokemonNbtCodec.decodePair(daycare.getPairJson());
        Pokemon existing = pair[slotIndex];

        if (existing != null) {
            if (shift) {
                if (!viewer.getUniqueId().equals(daycare.getOwner())) {
                    viewer.sendMessage("§cOnly the daycare owner can retrieve Pokemon.");
                    return;
                }
                if (plugin.getPixelmonHook().hasPokemonInParty(viewer, existing)) {
                    pair[slotIndex] = null;
                    daycare.setPairJson(pair[0] == null && pair[1] == null
                            ? null
                            : PokemonNbtCodec.encodePair(pair[0], pair[1]));
                    plugin.getStorage().saveDaycare(daycare);
                    viewer.sendMessage("§eThat Pokemon was already in your party, so the daycare copy was cleared.");
                    rebuild();
                    return;
                }
                if (plugin.getPixelmonHook().addToParty(viewer, existing)) {
                    pair[slotIndex] = null;
                    daycare.setPairJson(pair[0] == null && pair[1] == null
                            ? null
                            : PokemonNbtCodec.encodePair(pair[0], pair[1]));
                    plugin.getStorage().saveDaycare(daycare);
                    rebuild();
                } else {
                    viewer.sendMessage("§cYour party is full.");
                }
            }
        } else {
            if (!viewer.getUniqueId().equals(daycare.getOwner())) {
                viewer.sendMessage("§cOnly the daycare owner can add Pokemon.");
                return;
            }
            PartyPickerMenu.open(plugin, viewer, daycare, slotIndex);
        }
    }

    private void handleEggCollect(Player viewer) {
        if (!viewer.getUniqueId().equals(daycare.getOwner())) {
            viewer.sendMessage("§cOnly the daycare owner can collect eggs.");
            return;
        }
        Pokemon[] pair = PokemonNbtCodec.decodePair(daycare.getPairJson());
        Pokemon egg = plugin.getPixelmonHook().makeEgg(pair[0], pair[1]);
        if (egg == null) {
            viewer.sendMessage("§cFailed to generate egg.");
            return;
        }
        if (!plugin.getPixelmonHook().addToParty(viewer, egg)) {
            viewer.sendMessage("§cYour party is full — make room first.");
            return;
        }
        daycare.setEggCount(daycare.getEggCount() - 1);
        plugin.getStorage().saveDaycare(daycare);
        String species = plugin.getPixelmonHook().pokemonDisplayName(egg);
        viewer.sendMessage(plugin.getConfigManager().getMessages().get(
                "breeding.egg-collected", "species", species));
        rebuild();
    }

    public void setParent(int slotIndex, Pokemon p) {
        Pokemon[] pair = PokemonNbtCodec.decodePair(daycare.getPairJson());
        pair[slotIndex] = p;
        daycare.setPairJson(PokemonNbtCodec.encodePair(pair[0], pair[1]));
        plugin.getStorage().saveDaycare(daycare);
        rebuild();
    }
}
