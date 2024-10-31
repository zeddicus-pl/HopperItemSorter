package com.github.zeddicuspl.hopperItemSorter;

import com.github.zeddicuspl.hopperItemSorter.helper.BlockerItemHelper;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class HopperItemSorterListener implements Listener {
    private final Map<String, Block> blocksToUnWarn = new HashMap<>();
    private final HopperItemSorter plugin;

    public HopperItemSorterListener(HopperItemSorter plugin) {
        this.plugin = plugin;
    }

    /* Hoppers picking up dropped items */
    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        Location location = event.getInventory().getLocation();
        if (location != null && plugin.hopperHelper.isBlockHopperFilter(location.getBlock())) {
            // hopper filters should pick up an item only if the destination inventory can take it,
            // and the filter allows it
            if (!plugin.moveHelper.checkIfFilterAllowsItem(location.getBlock(), event.getItem().getItemStack())) {
                plugin.debug("IPI: (canceling event) Filter at " + location + " does " + ChatColor.RED + "NOT" + ChatColor.RESET + " allow item " + event.getItem().getItemStack(), "hop");
                event.setCancelled(true);
                return;
            } else {
                if (plugin.moveHelper.checkIfHopperTargetHasSpaceForItem(location.getBlock(), event.getItem().getItemStack())) {
                    // make sure there's empty slot in hopper filter to transport the item
                    if (!freeFirstSlotInFilterHopper(event.getInventory())) {
                        plugin.debug("IPI: (canceling event) Filter at " + location + " is stuck, " + ChatColor.RED + "CANNOT FREE" + ChatColor.RESET + " up first slot from item: " + event.getItem().getItemStack(), "hop");
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    // no space left in the target of filter hopper
                    plugin.debug("IPI: (canceling event) Target of filter hopper at " + location + " is " + ChatColor.RED + " FULL " + ChatColor.RESET + ", cannot pick up item " + event.getItem().getItemStack(), "hop");
                    plugin.displayBlockHelper.warnHopperFilterIsStuck(location.getBlock());
                    event.setCancelled(true);
                    return;
                }
            }
            plugin.debug("IPI: Filter at " + location + " " + ChatColor.GREEN + "allows" + ChatColor.RESET + " item " + event.getItem().getItemStack(), "hop");
        }
    }

    /* Hoppers moving items between inventories */
    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        Location location = event.getDestination().getLocation();

        // blocker items should never be transported between inventories
        if (BlockerItemHelper.isBlockerItem(event.getItem())) {
            event.setCancelled(true);
            return;
        }

        // if the destination inventory is a hopper filter
        if (location != null && plugin.hopperHelper.isBlockHopperFilter(location.getBlock())) {
            // first we check if filter allows for an item,
            // then we do a forward check, to see if the item should be able to leave the hopper filter after it is received now,
            // and if it looks good then we empty one slot of a filter hopper, otherwise block slots of a filter hopper
            boolean shouldMoveItem = plugin.moveHelper.checkIfFilterAllowsItem(location.getBlock(), event.getItem());
            if (!shouldMoveItem) {
                plugin.debug("IMIE: Filter on " + location + " does " + ChatColor.RED + "NOT" + ChatColor.RESET + " allow item " + event.getItem(), "hop");
            }
            if (shouldMoveItem) {
                plugin.debug("IMIE: Filter at " + location + " " + ChatColor.GREEN + "can take" + ChatColor.RESET + " item " + event.getItem(), "hop");
                shouldMoveItem = plugin.moveHelper.checkIfHopperTargetHasSpaceForItem(location.getBlock(), event.getItem());
                if (shouldMoveItem) {
                    shouldMoveItem = freeFirstSlotInFilterHopper(event.getDestination());
                } else {
                    // target of filter hopper is full
                    plugin.displayBlockHelper.warnHopperFilterIsStuck(location.getBlock());
                    plugin.unwarnHopperFilter.unWarnLater(location.getBlock());
                }
            }
            if (!shouldMoveItem) {
                plugin.debug("IMIE: Item " + event.getItem() + " should " + ChatColor.RED + "NOT" + ChatColor.RESET + " be moved to filter at " + location + ", blocking it", "hop");
                if (!blockFirstSlotInFilterHopper(event.getDestination())) {
                    plugin.debug("IMIE (canceling): Filter at " + location + "contains an item, " + ChatColor.RED + "cannot block" + ChatColor.RESET + " first slot with item: " + event.getItem(), "hop");
                    plugin.displayBlockHelper.warnHopperFilterIsStuck(location.getBlock());
                    plugin.unwarnHopperFilter.unWarnLater(location.getBlock());
                };
            }
            for (int i = 1; i < event.getDestination().getSize(); i++) {
                event.getDestination().setItem(i, BlockerItemHelper.getBlockerItem());
            }
            // if there was blocker added to first slot, add this inventory to the list of inventories to unblock later
            if (!shouldMoveItem) {
                plugin.unblockHopperFilter.unblockLater(event.getDestination());
            }
        }
    }

    private boolean freeFirstSlotInFilterHopper(Inventory filterHopperInventory) {
        ItemStack firstSlotItem = filterHopperInventory.getItem(0);
        Location location = filterHopperInventory.getLocation();
        if (BlockerItemHelper.isBlockerItem(firstSlotItem)) {
            plugin.debug("Filter at " + location + " contains blocker, removing it", "hop");
            // removing a block item
            filterHopperInventory.setItem(0, BlockerItemHelper.emptyItem);
        } else {
            plugin.debug("Filter at " + location + " does not contain blocker, slot contains: " + firstSlotItem, "hop");
        }
        return true;
    }

    private boolean blockFirstSlotInFilterHopper(Inventory filterHopperInventory) {
        ItemStack firstSlotItem = filterHopperInventory.getItem(0);
        Location location = filterHopperInventory.getLocation();
        if (firstSlotItem != null && !firstSlotItem.getType().equals(Material.AIR)) {
            plugin.debug("Filter at " + location + " contains item " + firstSlotItem + ", cannot block slot!", "hop");
            return false;
        } else {
            plugin.debug("Filter at " + location + " contains " + firstSlotItem + ", blocking slot", "hop");
            filterHopperInventory.setItem(0, BlockerItemHelper.getBlockerItem());
            return true;
        }
    }

    /* Recipe discovery */
    @EventHandler
    public void onRecipeDiscovered(PlayerRecipeDiscoverEvent event) {
        // Discover recipe for filter item when redstone block is discovered
        if (event.getRecipe().getKey().equals("redstone_block")) {
            plugin.itemHelper.discoverRecipe(event.getPlayer());
        }
    }

    /* Preventing placing filter item on the ground */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.itemHelper.isHoldingHopperFilterItem(event.getItemInHand())) {
            event.setCancelled(true);
        }
    }

    /* Remove filter from hopper when it is broken */
    @EventHandler
    public void onBlockBroken(BlockBreakEvent event) {
        if (plugin.hopperHelper.isBlockHopperFilter(event.getBlock())
            && plugin.itemHelper.removeItemFilterFromHopper(event.getBlock(), event.getPlayer())
        ) {
            event.setCancelled(true);
        }
    }

    /* Remove filter from hopper when it is punched */
    @EventHandler
    public void onBlockDamaged(BlockDamageEvent event) {
        if (plugin.hopperHelper.isBlockHopperFilter(event.getBlock())
            && plugin.itemHelper.removeItemFilterFromHopper(event.getBlock(), event.getPlayer())
        ) {
            event.setCancelled(true);
        }
    }

    /*
     * Clicking on filtering hopper - Open filter inventory when clicking on a filter hopper
     * Clicking on normal hopper - Add filter to hopper when holding filter item in main hand
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // If clicking on a hopper and sneaking, return early to imitate normal chests behavior
        if (!event.getAction().isRightClick() || event.getClickedBlock() == null
            || !event.getClickedBlock().getType().equals(Material.HOPPER) || event.getPlayer().isSneaking()
        ) {
            return;
        }

        // If clicking on a filter hopper, open filter inventory
        if (plugin.hopperHelper.isBlockHopperFilter(event.getClickedBlock())) {
            Inventory inventory = plugin.hopperHelper.getFilterInventoryForHopper(event.getClickedBlock());
            if (inventory == null) {
                return;
            }
            event.getPlayer().openInventory(inventory);
            event.setCancelled(true);
            plugin.unwarnHopperFilter.unWarnLater(event.getClickedBlock());

        // otherwise, if holding filter item, and clicking on a normal hopper, add filter to hopper
        } else if (
                plugin.itemHelper.isHoldingHopperFilterItem(event.getPlayer().getInventory().getItemInMainHand())
                && plugin.itemHelper.addItemFilterToHopper(event.getClickedBlock(), event.getPlayer())
        ) {
            if (!event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
                event.getPlayer().getInventory().getItemInMainHand().subtract(1);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) {
            return;
        }
        InventoryHolder clickedInventoryHolder = clickedInventory.getHolder();
        switch (clickedInventoryHolder) {
            // clicking inside custom inventory
            case Hopper hopper when plugin.hopperHelper.isBlockHopperFilter(hopper.getBlock()) -> {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    plugin.hopperHelper.saveInventory(event.getInventory(), hopper.getBlock());
                }, 1);
            }
            // clicked in player's inventory
            case Player player -> {
                InventoryHolder holder = event.getView().getTopInventory().getHolder();
                if (holder instanceof Hopper hopper && plugin.hopperHelper.isBlockHopperFilter(hopper.getBlock())) {
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        plugin.hopperHelper.saveInventory(event.getInventory(), hopper.getBlock());
                    }, 1);
                }
            }
            case null, default -> {
            }
        }
    }
}
