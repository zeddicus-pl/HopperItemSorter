package com.github.zeddicuspl.hopperItemSorter;

import com.github.zeddicuspl.hopperItemSorter.helper.FilterDisplayBlockHelper;
import com.github.zeddicuspl.hopperItemSorter.helper.FilterItemHelper;
import com.github.zeddicuspl.hopperItemSorter.helper.FilterHopperHelper;
import com.github.zeddicuspl.hopperItemSorter.helper.ItemMoveHelper;
import com.github.zeddicuspl.hopperItemSorter.tasks.UnWarnHopperFilter;
import com.github.zeddicuspl.hopperItemSorter.tasks.UnblockHopperFilter;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

public final class HopperItemSorter extends JavaPlugin {
    public final FilterHopperHelper hopperHelper;
    public final FilterItemHelper itemHelper;
    public final ItemMoveHelper moveHelper;
    public final FilterDisplayBlockHelper displayBlockHelper;
    public final UnblockHopperFilter unblockHopperFilter;
    public final UnWarnHopperFilter unwarnHopperFilter;
    private boolean debugHoppers = false;
    private boolean debugEntities = false;
    private boolean debugInventories = false;
    private final HopperItemSorterListener listener;

    public HopperItemSorter() {
        hopperHelper = new FilterHopperHelper(this);
        itemHelper = new FilterItemHelper(this);
        moveHelper = new ItemMoveHelper(this);
        displayBlockHelper = new FilterDisplayBlockHelper(this);
        unblockHopperFilter = new UnblockHopperFilter(this);
        unwarnHopperFilter = new UnWarnHopperFilter(this);
        listener = new HopperItemSorterListener(this);
    }

    public NamespacedKey getKey(String key) {
        return new NamespacedKey(this, key);
    }

    public String getCacheKey(Block block) {
        return block.getWorld().getName() + '_' + block.getLocation().getBlockX() + '_'
                + block.getLocation().getBlockY() + '_' + block.getLocation().getBlockZ();
    }

    public void debug(String message, String type) {
        if ((debugHoppers && type.equals("hop"))
            || (debugEntities && type.equals("ent"))
            || (debugInventories && type.equals("inv"))
        ) {
            getServer().getConsoleSender().sendMessage(getServer().getCurrentTick() + ": " + message);
        }

    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(listener, this);
        itemHelper.addRecipe();
        // a timer that runs each tick and performs "unblocking" of filter hoppers, if needed
        getServer().getScheduler().runTaskTimer(this, unblockHopperFilter::run, 0, 0);
        getServer().getScheduler().runTaskTimer(this, unwarnHopperFilter::run, 0, 20);
        saveDefaultConfig();
        debugHoppers = getConfig().getBoolean("debugHoppers", false);
        debugEntities = getConfig().getBoolean("debugEntities", false);
        debugInventories = getConfig().getBoolean("debugInventories", false);
    }
}
