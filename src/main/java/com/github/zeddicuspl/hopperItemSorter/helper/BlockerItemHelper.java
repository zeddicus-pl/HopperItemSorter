package com.github.zeddicuspl.hopperItemSorter.helper;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BlockerItemHelper {
    private static final String FILTER_ITEM_UNIQ_NAME = "BlockerItem6BtenzWJiYIi4SMTe043fDbmeTnzd";
    public static final ItemStack blockerItem = getBlockerItem();
    public static final ItemStack emptyItem = new ItemStack(Material.AIR, 0);

    /* This is a unique item that is used to block hoppers from picking up items. */
    public static ItemStack getBlockerItem() {
        ItemStack itemStack = new ItemStack(Material.STICK, 1);
        itemStack.editMeta(meta -> meta.itemName(Component.text(FILTER_ITEM_UNIQ_NAME)));
        return itemStack;
    }

    /* Checks if item is a blocker item */
    public static boolean isBlockerItem(ItemStack item) {
        return item != null && !item.isEmpty() && item.isSimilar(blockerItem);
    }
}
