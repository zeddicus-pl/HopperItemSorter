package com.github.zeddicuspl.hopperItemSorter.tasks;

import com.github.zeddicuspl.hopperItemSorter.HopperItemSorter;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;

public class UnWarnHopperFilter {
    private final Map<String, Block> blocksToUnWarn = new HashMap<>();
    private final HopperItemSorter plugin;

    public UnWarnHopperFilter(HopperItemSorter plugin) {
        this.plugin = plugin;
    }

    public void unWarnLater(Block block) {
        blocksToUnWarn.put(plugin.getCacheKey(block), block);
    }

    public void run() {
        if (!blocksToUnWarn.isEmpty()) {
            String key = blocksToUnWarn.entrySet().iterator().next().getKey();
            Block block = blocksToUnWarn.remove(key);
            if (block == null) {
                return;
            }
            plugin.displayBlockHelper.unwarnHopperFilterIsStuck(block);
        }
    }
}
