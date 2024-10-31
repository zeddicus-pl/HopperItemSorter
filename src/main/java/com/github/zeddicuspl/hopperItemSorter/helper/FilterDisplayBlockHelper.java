package com.github.zeddicuspl.hopperItemSorter.helper;

import com.github.zeddicuspl.hopperItemSorter.HopperItemSorter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class FilterDisplayBlockHelper {
    public final String FILTER_DISPLAY_BLOCK_KEY = "filterDisplayBlock";
    private final HopperItemSorter plugin;
    private final Map<String, BlockDisplay> entitiesCache = new HashMap<>();

    public FilterDisplayBlockHelper(HopperItemSorter plugin) {
        this.plugin = plugin;
    }

    /* Returns display entity of a hopper filter, or null if block doesn't contain a filter hopper */
    public BlockDisplay getDisplayEntityOfHopperFilter(Block block) {
        String cacheKey = plugin.getCacheKey(block);
        String debug_cacheContains = ChatColor.DARK_GREEN + "YES" + ChatColor.RESET;
        if (!entitiesCache.containsKey(cacheKey)) {
            debug_cacheContains = ChatColor.DARK_RED + "NO" + ChatColor.RESET;
            BlockDisplay entity = getDisplayEntityOfHopperFilterByLocation(block);
            entitiesCache.put(cacheKey, entity);
        }
        plugin.debug("ENT: cached: " + debug_cacheContains, "ent");
        return entitiesCache.get(cacheKey);
    }

    /* Places a display block on the hopper */
    public void placeFilterDisplayBlock(Block hopper) {
        placeFilterDisplayBlock(hopper, Material.COPPER_TRAPDOOR);
    }

    /* Places a display block of given material on the hopper */
    public void placeFilterDisplayBlock(Block hopper, Material material) {
        Location hopperLocation = hopper.getLocation();
        BlockDisplay displayBlock = (BlockDisplay) hopperLocation.getWorld().spawnEntity(hopperLocation, EntityType.BLOCK_DISPLAY);
        displayBlock.setBlock(material.createBlockData());
        Vector3f translation = new Vector3f(0, 0.5625f, 0);
        AxisAngle4f rotation = new AxisAngle4f(0, 0, 0, 0);
        Vector3f scale = new Vector3f(1, 0.33333f, 1);
        displayBlock.setTransformation(new Transformation(translation, rotation, scale, rotation));
        displayBlock.getPersistentDataContainer().set(plugin.getKey(FILTER_DISPLAY_BLOCK_KEY), PersistentDataType.BOOLEAN, true);
        plugin.debug("ENT: Placed display block " + displayBlock + " at location " + hopperLocation, "ent");
        entitiesCache.put(plugin.getCacheKey(hopper), displayBlock);
    }

    /* Removes display block from the hopper */
    public void removeFilterDisplayBlock(Block block) {
        BlockDisplay entity = getDisplayEntityOfHopperFilter(block);
        if (entity != null) {
            plugin.debug("ENT: Removed display block " + block + " from location " + block.getLocation(), "ent");
            entity.remove();
        } else {
            plugin.debug("ENT: " + ChatColor.RED + "NOT" + ChatColor.RESET + " removing display block, as there's no hopper filter display block here", "ent");
        }
        entitiesCache.remove(plugin.getCacheKey(block));
    }

    /* Warns that filter hopper is stuck */
    public void warnHopperFilterIsStuck(Block block) {
        BlockDisplay entity = getDisplayEntityOfHopperFilter(block);
        if (entity != null) {
            plugin.debug("ENT: Warning block " + block, "ent");
            entity.setBlock(Material.SOUL_FIRE.createBlockData());
        } else {
            plugin.debug("ENT: " + ChatColor.RED + "CANNOT" + ChatColor.RESET + " warning block " + block + ", entity not found", "ent");
        }
    }

    /* Removes warning from a filter hopper */
    public void unwarnHopperFilterIsStuck(Block block) {
        BlockDisplay entity = getDisplayEntityOfHopperFilter(block);
        if (entity != null) {
            plugin.debug("ENT: Un-warning block " + block, "ent");
            entity.setBlock(Material.COPPER_TRAPDOOR.createBlockData());
        } else {
            plugin.debug("ENT: " + ChatColor.RED + "CANNOT" + ChatColor.RESET + " un-warn block " + block + ", entity not found", "ent");
        }
    }

    /* Returns display entity of a hopper filter by location, cached by getDisplayEntityOfHopperFilter() */
    private BlockDisplay getDisplayEntityOfHopperFilterByLocation(Block block) {
        Location location = block.getLocation();
        return location.getWorld().getNearbyEntitiesByType(BlockDisplay.class, location, 0.5).stream()
                .filter(entity -> entity.getPersistentDataContainer().has(plugin.getKey(FILTER_DISPLAY_BLOCK_KEY), PersistentDataType.BOOLEAN))
                .findFirst()
                .orElse(null);
    }
}
