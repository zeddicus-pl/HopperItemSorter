package com.github.zeddicuspl.hopperItemSorter.tasks;

import com.github.zeddicuspl.hopperItemSorter.HopperItemSorter;
import com.github.zeddicuspl.hopperItemSorter.helper.BlockerItemHelper;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class UnblockHopperFilter {
    private final Map<String, Inventory> inventoriesToUnblock = new HashMap<>();
    private final HopperItemSorter plugin;

    public UnblockHopperFilter(HopperItemSorter plugin) {
        this.plugin = plugin;
    }

    public void unblockLater(Inventory inventory) {
        if (inventory.getLocation() == null) {
            return;
        }
        inventoriesToUnblock.put(plugin.getCacheKey(inventory.getLocation().getBlock()), inventory);
    }

    public void run() {
        if (!inventoriesToUnblock.isEmpty()) {
            String key = inventoriesToUnblock.entrySet().iterator().next().getKey();
            Inventory inventory = inventoriesToUnblock.remove(key);
            if (inventory == null) {
                return;
            }
            ItemStack item = inventory.getItem(0);
            if (item != null && !item.isEmpty()) {
                if (BlockerItemHelper.isBlockerItem(item)) {
                    plugin.debug("Runner: removing blocker item from filter at " + inventory.getLocation(), "hop");
                    inventory.setItem(0, BlockerItemHelper.emptyItem);
                } else {
                    plugin.debug("Runner: stray item in filter at " + inventory.getLocation() + ", dropping it", "hop");
                    plugin.getLogger().warning("There's an item stuck in a hopper filter!!!, dropping it");
                    Location location = inventory.getLocation();
                    if (location == null) {
                        plugin.getLogger().warning("Filter hopper location is null, cannot drop item, the ITEM WILL BE LOST!!!");
                        return;
                    }
                    location.getWorld().dropItemNaturally(location, item);
                    inventory.setItem(0, BlockerItemHelper.emptyItem);
                }
            }
        }
    }
}
