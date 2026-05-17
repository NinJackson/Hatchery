package gg.hatchery.util;

import gg.hatchery.config.MessagesConfig;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private ItemMeta meta;

    public ItemBuilder(Material m) {
        this.item = new ItemStack(m);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack stack) {
        this.item = stack;
        this.meta = stack.getItemMeta();
    }

    public ItemBuilder name(String s) {
        if (meta != null) meta.setDisplayName(MessagesConfig.color(s));
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        if (meta != null) {
            List<String> out = new ArrayList<>();
            for (String l : lines) out.add(MessagesConfig.color(l));
            meta.setLore(out);
        }
        return this;
    }

    public ItemBuilder lore(String... lines) {
        return lore(Arrays.asList(lines));
    }

    public ItemBuilder tag(NamespacedKey key, String value) {
        if (meta != null) {
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) item.setItemMeta(meta);
        return item;
    }

    public static String tagOf(ItemStack stack, NamespacedKey key) {
        if (stack == null || !stack.hasItemMeta()) return null;
        ItemMeta m = stack.getItemMeta();
        return m.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }
}
