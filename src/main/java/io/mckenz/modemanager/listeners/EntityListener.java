package io.mckenz.modemanager.listeners;

import io.mckenz.modemanager.ModeManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/**
 * Listener for entity events, specifically for item frames
 */
public class EntityListener implements Listener {
    
    private final ModeManager plugin;
    
    /**
     * Constructor for EntityListener
     * 
     * @param plugin The plugin instance
     */
    public EntityListener(ModeManager plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handle player interact with entity events
     * This is triggered when a player places an item in an item frame
     * 
     * @param event The player interact entity event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        
        // Skip if tracking is disabled
        if (!plugin.getConfig().getBoolean("protection.track-creative-item-frames", true)) {
            return;
        }
        
        // Only track item frames
        if (!(entity instanceof ItemFrame)) {
            return;
        }
        
        ItemFrame itemFrame = (ItemFrame) entity;
        
        // Track items placed in item frames in creative mode
        if (player.getGameMode() == GameMode.CREATIVE) {
            // We need to check in the next tick if the item frame has an item
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (itemFrame.getItem() != null && !itemFrame.getItem().getType().isAir()) {
                    plugin.getCreativeItemFrameManager().addItemFrame(itemFrame, player.getUniqueId());
                    plugin.logDebug("Tracked creative item placed in item frame by " + player.getName() + 
                                   " at " + itemFrame.getLocation().getWorld().getName() + " " + 
                                   itemFrame.getLocation().getBlockX() + "," + 
                                   itemFrame.getLocation().getBlockY() + "," + 
                                   itemFrame.getLocation().getBlockZ());
                }
            });
        }
    }
    
    /**
     * Handle entity damage by entity events
     * This is triggered when a player removes an item from an item frame
     * 
     * @param event The entity damage by entity event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Skip if tracking is disabled
        if (!plugin.getConfig().getBoolean("protection.track-creative-item-frames", true)) {
            return;
        }
        
        // Only handle item frames
        if (!(event.getEntity() instanceof ItemFrame)) {
            return;
        }
        
        // Only handle damage by players
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        ItemFrame itemFrame = (ItemFrame) event.getEntity();
        Player player = (Player) event.getDamager();
        
        // Prevent removing items from item frames in survival mode if they were placed in creative
        if (player.getGameMode() == GameMode.SURVIVAL && 
            plugin.getCreativeItemFrameManager().isCreativeItemFrame(itemFrame)) {
            
            // Only cancel if the item frame has an item (to allow breaking the frame itself)
            if (itemFrame.getItem() != null && !itemFrame.getItem().getType().isAir()) {
                event.setCancelled(true);
                plugin.getMessageUtil().sendMessage(player, "creative-item-frame-protected");
                plugin.logDebug("Prevented " + player.getName() + " from removing item from creative item frame at " + 
                               itemFrame.getLocation().getWorld().getName() + " " + 
                               itemFrame.getLocation().getBlockX() + "," + 
                               itemFrame.getLocation().getBlockY() + "," + 
                               itemFrame.getLocation().getBlockZ());
            }
        }
        
        // Remove the item frame from tracking if it's broken in creative mode
        if (player.getGameMode() == GameMode.CREATIVE && 
            plugin.getCreativeItemFrameManager().isCreativeItemFrame(itemFrame)) {
            
            plugin.getCreativeItemFrameManager().removeItemFrame(itemFrame);
            plugin.logDebug("Removed creative item frame tracking for item frame broken by " + player.getName());
        }
    }
    
    /**
     * Handle hanging break by entity events
     * This is triggered when a player breaks an item frame
     * 
     * @param event The hanging break by entity event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        // Skip if tracking is disabled
        if (!plugin.getConfig().getBoolean("protection.track-creative-item-frames", true)) {
            return;
        }
        
        // Only handle item frames
        if (!(event.getEntity() instanceof ItemFrame)) {
            return;
        }
        
        // Only handle breaks by players
        if (!(event.getRemover() instanceof Player)) {
            return;
        }
        
        ItemFrame itemFrame = (ItemFrame) event.getEntity();
        Player player = (Player) event.getRemover();
        
        // Prevent breaking item frames in survival mode if they were placed in creative
        if (player.getGameMode() == GameMode.SURVIVAL && 
            plugin.getCreativeItemFrameManager().isCreativeItemFrame(itemFrame)) {
            
            event.setCancelled(true);
            plugin.getMessageUtil().sendMessage(player, "creative-item-frame-protected");
            plugin.logDebug("Prevented " + player.getName() + " from breaking creative item frame at " + 
                           itemFrame.getLocation().getWorld().getName() + " " + 
                           itemFrame.getLocation().getBlockX() + "," + 
                           itemFrame.getLocation().getBlockY() + "," + 
                           itemFrame.getLocation().getBlockZ());
        }
    }
} 