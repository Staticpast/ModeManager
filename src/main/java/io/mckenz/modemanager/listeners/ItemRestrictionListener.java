package io.mckenz.modemanager.listeners;

import io.mckenz.modemanager.ModeManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Listener for handling item restrictions in creative mode
 */
public class ItemRestrictionListener implements Listener {
    
    private final ModeManager plugin;
    private final Set<Material> restrictedItems;
    
    /**
     * Constructor for ItemRestrictionListener
     * 
     * @param plugin The plugin instance
     */
    public ItemRestrictionListener(ModeManager plugin) {
        this.plugin = plugin;
        this.restrictedItems = loadRestrictedItems();
    }
    
    /**
     * Load restricted items from config
     * 
     * @return Set of restricted materials
     */
    private Set<Material> loadRestrictedItems() {
        Set<Material> items = new HashSet<>();
        
        // Skip if restrictions are disabled
        if (!plugin.getConfig().getBoolean("protection.restrict-creative-items.enabled", true)) {
            return items;
        }
        
        List<String> configItems = plugin.getConfig().getStringList("protection.restrict-creative-items.restricted-items");
        
        for (String itemName : configItems) {
            try {
                Material material = Material.valueOf(itemName.toUpperCase());
                items.add(material);
                plugin.logDebug("Added restricted item: " + material.name());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material name in restricted items list: " + itemName);
            }
        }
        
        return items;
    }
    
    /**
     * Reload restricted items from config
     */
    public void reloadRestrictedItems() {
        restrictedItems.clear();
        restrictedItems.addAll(loadRestrictedItems());
        plugin.logDebug("Reloaded restricted items list. Total items: " + restrictedItems.size());
    }
    
    /**
     * Check if an item is restricted
     * 
     * @param material The material to check
     * @return True if the item is restricted, false otherwise
     */
    private boolean isItemRestricted(Material material) {
        // Check if the item is in the restricted items set
        return restrictedItems.contains(material);
    }
    
    /**
     * Handle player interact events
     * 
     * @param event The player interact event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Skip if restrictions are disabled
        if (!plugin.getConfig().getBoolean("protection.restrict-creative-items.enabled", true)) {
            return;
        }
        
        // Skip if player is not in creative mode
        if (player.getGameMode() != GameMode.CREATIVE) {
            return;
        }
        
        // Skip if player has bypass permission
        if (player.hasPermission("modemanager.bypass.itemrestrictions")) {
            return;
        }
        
        // Check if the item is restricted
        ItemStack item = event.getItem();
        if (item != null && isItemRestricted(item.getType())) {
            // Cancel the event for all restricted items
            event.setCancelled(true);
            
            // Send message to player
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("item", formatItemName(item.getType().name()));
            plugin.getMessageUtil().sendMessage(player, "creative-item-restricted", placeholders);
            
            plugin.logDebug("Prevented " + player.getName() + " from using restricted item: " + item.getType().name());
        }
    }
    
    /**
     * Handle block place events
     * 
     * @param event The block place event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        // Skip if restrictions are disabled
        if (!plugin.getConfig().getBoolean("protection.restrict-creative-items.enabled", true)) {
            return;
        }
        
        // Skip if player is not in creative mode
        if (player.getGameMode() != GameMode.CREATIVE) {
            return;
        }
        
        // Skip if player has bypass permission
        if (player.hasPermission("modemanager.bypass.itemrestrictions")) {
            return;
        }
        
        // Check if the item is restricted
        ItemStack item = event.getItemInHand();
        if (isItemRestricted(item.getType())) {
            // Cancel the event for all restricted items
            event.setCancelled(true);
            
            // Send message to player
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("item", formatItemName(item.getType().name()));
            plugin.getMessageUtil().sendMessage(player, "creative-item-restricted", placeholders);
            
            plugin.logDebug("Prevented " + player.getName() + " from placing restricted item: " + item.getType().name());
        }
    }
    
    /**
     * Handle bucket empty events
     * 
     * @param event The player bucket empty event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        
        // Skip if restrictions are disabled
        if (!plugin.getConfig().getBoolean("protection.restrict-creative-items.enabled", true)) {
            return;
        }
        
        // Skip if player is not in creative mode
        if (player.getGameMode() != GameMode.CREATIVE) {
            return;
        }
        
        // Skip if player has bypass permission
        if (player.hasPermission("modemanager.bypass.itemrestrictions")) {
            return;
        }
        
        // Check if the bucket is restricted
        Material bucketType = event.getBucket();
        if (isItemRestricted(bucketType)) {
            // Cancel the event
            event.setCancelled(true);
            
            // Send message to player
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("item", formatItemName(bucketType.name()));
            plugin.getMessageUtil().sendMessage(player, "creative-item-restricted", placeholders);
            
            plugin.logDebug("Prevented " + player.getName() + " from emptying restricted bucket: " + bucketType.name());
        }
    }
    
    /**
     * Handle bucket fill events
     * 
     * @param event The player bucket fill event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        
        // Skip if restrictions are disabled
        if (!plugin.getConfig().getBoolean("protection.restrict-creative-items.enabled", true)) {
            return;
        }
        
        // Skip if player is not in creative mode
        if (player.getGameMode() != GameMode.CREATIVE) {
            return;
        }
        
        // Skip if player has bypass permission
        if (player.hasPermission("modemanager.bypass.itemrestrictions")) {
            return;
        }
        
        // Check if the bucket is restricted
        Material bucketType = Material.BUCKET;
        if (isItemRestricted(bucketType)) {
            // Cancel the event
            event.setCancelled(true);
            
            // Send message to player
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("item", formatItemName(bucketType.name()));
            plugin.getMessageUtil().sendMessage(player, "creative-item-restricted", placeholders);
            
            plugin.logDebug("Prevented " + player.getName() + " from filling bucket: " + bucketType.name());
        }
    }
    
    /**
     * Format item name for display
     * 
     * @param materialName The material name
     * @return Formatted item name
     */
    private String formatItemName(String materialName) {
        String[] parts = materialName.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        
        for (String part : parts) {
            if (part.length() > 0) {
                result.append(Character.toUpperCase(part.charAt(0)))
                      .append(part.substring(1))
                      .append(" ");
            }
        }
        
        return result.toString().trim();
    }
} 