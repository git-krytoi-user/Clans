package dev.r1nex.clans.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemCreate {
    private final Material material;
    private final int count;
    private final int modelData;
    private final String displayName;

    public ItemCreate(Material material, int count, int modelData, String displayName) {
        this.material = material;
        this.count = count;
        this.modelData = modelData;
        this.displayName = displayName;
    }

    private String chatColor(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public ItemStack getItemStack() {
        ItemStack itemStack = new ItemStack(material, count);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (this.modelData != -1) itemMeta.setCustomModelData(modelData);
        if (displayName != null) itemMeta.displayName(Component.text(chatColor(displayName)));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
