package gg.hatchery.ui;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import gg.hatchery.Hatchery;
import gg.hatchery.config.MessagesConfig;
import gg.hatchery.daycare.Daycare;
import gg.hatchery.pixelmon.PixelmonHook;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class PartyPickerMenu implements HatcheryMenu {

    public static final String TITLE_MARKER = "§8​Choose Pokemon";
    private static final int BACK_SLOT = 8;

    private final Hatchery plugin;
    private final Daycare daycare;
    private final int parentSlotIndex;
    private final Inventory inv;
    private final Pokemon[] party;

    private PartyPickerMenu(Hatchery plugin, Player viewer, Daycare daycare, int parentSlotIndex) {
        this.plugin = plugin;
        this.daycare = daycare;
        this.parentSlotIndex = parentSlotIndex;
        this.party = plugin.getPixelmonHook().getParty(viewer);
        this.inv = Bukkit.createInventory(viewer, 9,
                MessagesConfig.color("§8") + TITLE_MARKER);
        for (int i = 0; i < 6; i++) {
            inv.setItem(i, renderSlot(i));
        }
        inv.setItem(BACK_SLOT, simple(Material.ARROW, "§7Back", Arrays.asList()));
    }

    public static void open(Hatchery plugin, Player viewer, Daycare daycare, int parentSlotIndex) {
        PartyPickerMenu m = new PartyPickerMenu(plugin, viewer, daycare, parentSlotIndex);
        plugin.getMenuManager().track(viewer, m);
        viewer.openInventory(m.inv);
    }

    @Override public Inventory getInventory() { return inv; }

    private ItemStack renderSlot(int slot) {
        Pokemon p = party != null && slot < party.length ? party[slot] : null;
        if (p == null) {
            return simple(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§8Empty Slot",
                    Arrays.asList("§7Party slot " + (slot + 1) + " is empty."));
        }
        PixelmonHook hook = plugin.getPixelmonHook();
        ItemStack sprite = hook.spriteItem(p);
        if (sprite == null) sprite = new ItemStack(Material.EGG);
        ItemMeta m = sprite.getItemMeta();
        if (m != null) {
            m.setDisplayName(MessagesConfig.color("§a" + hook.pokemonDisplayName(p)));
            m.setLore(Arrays.asList(
                    MessagesConfig.color("§7Types: §f" + String.join(", ", hook.getTypes(p))),
                    MessagesConfig.color("§7Gender: §f"
                            + (p.getGender() == null ? "?" : p.getGender().name())),
                    "",
                    MessagesConfig.color("§eClick to place into daycare.")
            ));
            sprite.setItemMeta(m);
        }
        return sprite;
    }

    @Override
    public void onClick(Player viewer, InventoryClickEvent e) {
        int slot = e.getRawSlot();
        if (slot == BACK_SLOT) {
            DaycareMenu.open(plugin, viewer, daycare);
            return;
        }
        if (slot < 0 || slot >= 6) return;
        Pokemon chosen = party[slot];
        if (chosen == null) return;

        // Remove from party, then reopen DaycareMenu and place the Pokemon.
        if (!plugin.getPixelmonHook().removeFromPartySlot(viewer, slot)) {
            viewer.sendMessage("§cCould not retrieve that Pokemon.");
            return;
        }
        DaycareMenu.open(plugin, viewer, daycare);
        HatcheryMenu open = plugin.getMenuManager().get(viewer);
        if (open instanceof DaycareMenu) {
            ((DaycareMenu) open).setParent(parentSlotIndex, chosen);
        }
    }

    private ItemStack simple(Material mat, String name, java.util.List<String> lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        if (m != null) {
            m.setDisplayName(MessagesConfig.color(name));
            java.util.List<String> coloured = new java.util.ArrayList<>();
            for (String l : lore) coloured.add(MessagesConfig.color(l));
            m.setLore(coloured);
            i.setItemMeta(m);
        }
        return i;
    }
}
