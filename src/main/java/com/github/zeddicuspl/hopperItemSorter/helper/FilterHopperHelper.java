package com.github.zeddicuspl.hopperItemSorter.helper;

import com.github.zeddicuspl.hopperItemSorter.HopperItemSorter;
import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.morepersistentdatatypes.DataType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FilterHopperHelper {
    public final String ITEM_FILTER_INVENTORY_CONTENT = "itemFilterInventoryContent";
    private final Map<String, Inventory> filterInventories = new HashMap<>();
    private final HopperItemSorter plugin;

    public FilterHopperHelper(HopperItemSorter plugin) {
        this.plugin = plugin;
    }

    /* Checks if block is a hopper with filter */
    public boolean isBlockHopperFilter(Block block) {
        return isBlockHopperFilter(block, true);
    }

    /* Checks if block is a hopper with filter */
    public boolean isBlockHopperFilter(Block block, boolean useCache) {
        return block != null && block.getType().equals(Material.HOPPER)
                && plugin.displayBlockHelper.getDisplayEntityOfHopperFilter(block) != null;
    }

    /* Returns filter inventory for filter hopper, or null if block doesn't contain a filter hoper */
    public Inventory getFilterInventoryForHopper(Block block) {
        String cacheKey = plugin.getCacheKey(block);
        String debug_cacheContains = ChatColor.DARK_GREEN + "YES" + ChatColor.RESET;
        if (!filterInventories.containsKey(cacheKey)) {
            debug_cacheContains = ChatColor.DARK_RED + "NO" + ChatColor.RESET;
            filterInventories.put(cacheKey, createFilterInventory(block));
        }
        plugin.debug("INV: cached: " + debug_cacheContains, "inv");
        return filterInventories.get(cacheKey);
    }

    /* Removes filter from a hopper */
    public void removeFilterInventory(Block block) {
        if (getFilterInventoryFromBlock(block) == null) {
            plugin.debug("INV: tried removing inventory from filter, but it's " + ChatColor.DARK_RED + "missing" + ChatColor.RESET + " on " + block, "inv");
            return;
        }
        filterInventories.remove(plugin.getCacheKey(block));
        PersistentDataContainer customBlockData = new CustomBlockData(block, plugin);
        customBlockData.remove(plugin.getKey(ITEM_FILTER_INVENTORY_CONTENT));
        plugin.debug("INV: removed inventory from " + block, "inv");
    }

    /* Persists filter inventory into the customer block data */
    public void saveInventory(Inventory inventory, Block block) {
        PersistentDataContainer customBlockData = new CustomBlockData(block, plugin);
        customBlockData.set(plugin.getKey(ITEM_FILTER_INVENTORY_CONTENT), DataType.ITEM_STACK_ARRAY, inventory.getContents());
        plugin.debug("INV: updated inventory for " + block, "inv");
        plugin.debug("INV: saved items: " + Arrays.toString(inventory.getContents()), "inv");
    }

    /* Reads filter inventory key from the block, that's cached by removeFilterInventory() */
    private @Nullable ItemStack[] getFilterInventoryFromBlock(Block block) {
        PersistentDataContainer customBlockData = new CustomBlockData(block, plugin);
        return customBlockData.get(plugin.getKey(ITEM_FILTER_INVENTORY_CONTENT), DataType.ITEM_STACK_ARRAY);
    }

    /* Create filter inventory */
    private Inventory createFilterInventory(Block block) {
        Inventory inventory = Bukkit.createInventory((Hopper) block.getState(), InventoryType.CHEST, Component.text("Hopper filter"));
        plugin.debug("INV: " + ChatColor.DARK_GREEN + "Creating" + ChatColor.RESET + " inventory for " + block, "inv");
        // put anything that's currently inside the hopper into the filter inventory
        // (this happens when applying filter item to hopper that is not a hopper filter yet)
        if (!plugin.hopperHelper.isBlockHopperFilter(block)) {
            plugin.debug("INV: block " + block + " " + ChatColor.YELLOW + "is not" + ChatColor.RESET
                    + " filter hopper, moving inventory from hopper to filter, items:" + ((Hopper) block.getState()).getInventory(), "inv");
            inventory.setContents(((Hopper) block.getState()).getInventory().getContents());
            // clear hopper inventory
            ((Hopper) block.getState()).getInventory().clear();
        }
        // put anything that's in custom block data storage into the filter inventory
        // (this happens when filter inventory is first used by player after game started, and the block contains stored filter items)
        else {
            ItemStack[] items = getFilterInventoryFromBlock(block);
            if (items != null) {
                plugin.debug("INV: found block data storage items: " + Arrays.toString(items), "inv");
                inventory.setContents(items);
            } else {
                plugin.debug("INV: did " + ChatColor.DARK_RED + "not" + ChatColor.RESET + " find items in block data storage", "inv");
            }
        }
        // persist inventory to the block custom data
        saveInventory(inventory, block);
        return inventory;
    }
}
