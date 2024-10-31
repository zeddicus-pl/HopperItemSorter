package com.github.zeddicuspl.hopperItemSorter.helper;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.zeddicuspl.hopperItemSorter.HopperItemSorter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FilterItemHelper {
    public final String FILTER_ITEM_KEY = "filterItem";
    public final String FILTER_ITEM_RECIPE_KEY = "filterItemRecipe";
    public final UUID FILTER_ITEM_UUID = UUID.fromString("a545a960-6039-4387-9721-5579c0c63d59");
    private final HopperItemSorter plugin;

    public FilterItemHelper(HopperItemSorter plugin) {
        this.plugin = plugin;
    }

    /* Checks if player is holding a filter item */
    public boolean isHoldingHopperFilterItem(ItemStack heldInHand) {
        return heldInHand != null && heldInHand.getType().equals(Material.PLAYER_HEAD) && heldInHand.getPersistentDataContainer()
                .has(plugin.getKey(plugin.itemHelper.FILTER_ITEM_KEY), PersistentDataType.BOOLEAN);
    }

    /* This is an item that players can craft, used to add filter onto hopper. */
    public ItemStack getFilterItem() {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
        itemStack.editMeta(SkullMeta.class, meta -> {
            PlayerProfile profile = Bukkit.createProfile(FILTER_ITEM_UUID, "Player");
            profile.setProperty(new ProfileProperty("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTRkOWQyMTM5MjQ0MzY5YzBlMzhhMTFhMzUyOGY0NjhmMzljMjhlOGIxMzFiYzFiNmZhN2Q0YjVlN2E2ZGQxYyJ9fX0="));
            meta.itemName(Component.text("Hopper Filter").color(TextColor.color(255, 255, 255)));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text().decoration(TextDecoration.ITALIC, false)
                .append(Component.text("Install on hopper (").color(NamedTextColor.GRAY))
                .append(Component.keybind("key.use").color(NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(")")).color(NamedTextColor.GRAY)
                .asComponent()
            );
            lore.add(Component.text().decoration(TextDecoration.ITALIC, false)
                    .append(Component.text("to make it filter items.").color(NamedTextColor.GRAY))
                    .asComponent()
            );
            meta.lore(lore);
            meta.setPlayerProfile(profile);
            meta.getPersistentDataContainer().set(plugin.getKey(FILTER_ITEM_KEY), PersistentDataType.BOOLEAN, true);
        });
        return itemStack;
    }

    /* Adds filter onto hopper */
    public boolean addItemFilterToHopper(Block block, Player player) {
        if (block.getType().equals(Material.HOPPER) && !plugin.hopperHelper.isBlockHopperFilter(block)) {
            // Initialise filter inventory
            plugin.hopperHelper.getFilterInventoryForHopper(block);
            // Add display block
            plugin.displayBlockHelper.placeFilterDisplayBlock(block);
            // Play sound
            if (player != null) {
                player.playSound(player.getLocation(), "block.copper.place", 0.75f, 1.15f);
            }
            return true;
        }
        return false;
    }

    /* Removes filter from hopper */
    public boolean removeItemFilterFromHopper(Block block, Player player) {
        boolean haveRemoved = false;
        if (plugin.hopperHelper.isBlockHopperFilter(block)) {
            Inventory hopperFilterInventory = plugin.hopperHelper.getFilterInventoryForHopper(block);
            Location dropLocation = player != null ? player.getLocation() : block.getLocation();
            World world = player != null ? player.getWorld() : block.getWorld();
            hopperFilterInventory.forEach(item -> {
                if (item != null) {
                    world.dropItem(dropLocation, item);
                }
            });
            hopperFilterInventory.clear();
            // Remove filter inventory
            plugin.hopperHelper.removeFilterInventory(block);
            // Make sure there's no blocker items left in the hopper
            ((Hopper) block.getState()).getInventory().forEach(item -> {
                if (BlockerItemHelper.isBlockerItem(item)) { item.setAmount(0); }
            });
            // Remove display block
            plugin.displayBlockHelper.removeFilterDisplayBlock(block);
            // Give back an item filter to player
            if (player == null || !player.getGameMode().equals(GameMode.CREATIVE)) {
                world.dropItem(dropLocation, plugin.itemHelper.getFilterItem());
            }
            // Play sound
            if (player != null) {
                player.playSound(player.getLocation(), "block.copper.break", 0.75f, 1.15f);
            }
            haveRemoved = true;
        }
        return haveRemoved;
    }

    /* Adds recipe for filter item */
    public void addRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(plugin.getKey(FILTER_ITEM_RECIPE_KEY), getFilterItem());
        recipe.shape("CCC", "CRC", "CCC");
        recipe.setIngredient('C', Material.COPPER_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);
        Bukkit.addRecipe(recipe);
    }

    /* Makes player discover recipe for filter item */
    public void discoverRecipe(Player player) {
        player.discoverRecipe(plugin.getKey(FILTER_ITEM_RECIPE_KEY));
    }
}
