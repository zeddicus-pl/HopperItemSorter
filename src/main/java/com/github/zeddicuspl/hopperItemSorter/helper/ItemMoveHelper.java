package com.github.zeddicuspl.hopperItemSorter.helper;

import com.github.zeddicuspl.hopperItemSorter.HopperItemSorter;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Hopper;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ItemMoveHelper {
    private final HopperItemSorter plugin;

    public ItemMoveHelper(HopperItemSorter plugin) {
        this.plugin = plugin;
    }

    public boolean checkIfFilterAllowsItem(Block hopper, ItemStack item) {
        Inventory filterInventory = plugin.hopperHelper.getFilterInventoryForHopper(hopper);
        if (filterInventory == null) {
            return true;
        }
        return !filterInventory.all(item.getType()).isEmpty();
    }

    public boolean checkIfHopperTargetHasSpaceForItem(Block hopper, ItemStack item) {
        Inventory target = findTargetInventoryOfHopper(hopper);
        if (target == null) {
            return false;
        }
        Inventory tmp;
        if (target.getType().equals(InventoryType.CHEST)) {
            tmp = Bukkit.createInventory(null, target.getSize());
        } else {
            tmp = Bukkit.createInventory(null, target.getType());
        }
        tmp.setContents(target.getContents());
        HashMap<Integer, ItemStack> overflow = tmp.addItem(item);
        return overflow.isEmpty();
    }

    private Inventory findTargetInventoryOfHopper(Block block) {
         BlockFace facing = ((Hopper) block.getBlockData()).getFacing();
        Block targetBlock = block.getRelative(facing);
        if (targetBlock.getState() instanceof InventoryHolder) {
            return ((InventoryHolder) targetBlock.getState()).getInventory();
        }
        return null;
    }
}
