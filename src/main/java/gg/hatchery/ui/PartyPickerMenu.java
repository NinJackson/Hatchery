package gg.hatchery.ui;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import gg.hatchery.Hatchery;
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

public class PartyPickerMenu implements HatcheryMenu {

    private static final int SIZE      = 9;
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
        this.inv = Bukkit.createInventory(viewer, SIZE,
                MessagesConfig.color("§8Choose Pokemon"));

        ItemStack filler = makeFiller();
        for (int i = 0; i < SIZE; i++) inv.setItem(i, filler);

        for (int i = 0; i < 6; i++) inv.setItem(i, renderSlot(i));
        inv.setItem(BACK_SLOT, simple(Material.ARROW, "§7Back", Collections.emptyList()));
    }

    public static void open(Hatchery plugin, Player viewer, Daycare daycare, int parentSlotIndex) {
        PartyPickerMenu m = new PartyPickerMenu(plugin, viewer, daycare, parentSlotIndex);
        plugin.getMenuManager().track(viewer, m);
        viewer.openInventory(m.inv);
    }

    @Override public Inventory getInventory() { return inv; }

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

        PixelmonHook hook = plugin.getPixelmonHook();
        Pokemon current = hook.partySlot(viewer, slot);
        if (!hook.isSamePokemon(chosen, current)) {
            viewer.sendMessage("§cThat party slot changed. Please choose the Pokemon again.");
            PartyPickerMenu.open(plugin, viewer, daycare, parentSlotIndex);
            return;
        }

        Pokemon[] pair = PokemonNbtCodec.decodePair(daycare.getPairJson());
        pair[parentSlotIndex] = current;
        String encodedPair = PokemonNbtCodec.encodePair(pair[0], pair[1]);
        if (encodedPair == null) {
            viewer.sendMessage("§cCould not store that Pokemon. Please report this to staff.");
            plugin.getLogger().warning("Refusing daycare placement: failed to serialize Pokemon "
                    + current.getUUID() + " for daycare " + daycare.getId() + ".");
            return;
        }

        if (!hook.removeFromPartySlot(viewer, slot)) {
            viewer.sendMessage("§cCould not retrieve that Pokemon.");
            return;
        }

        try {
            daycare.setPairJson(encodedPair);
            plugin.getStorage().saveDaycare(daycare);
            DaycareMenu.open(plugin, viewer, daycare);
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed to store daycare parent " + current.getUUID()
                    + " for daycare " + daycare.getId() + ": " + t.getMessage());
            if (hook.addToParty(viewer, current)) {
                viewer.sendMessage("§cCould not store that Pokemon; it was returned to your party.");
            } else {
                viewer.sendMessage("§cCould not store that Pokemon, and your party was full when returning it. Contact staff.");
            }
        }
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
}
