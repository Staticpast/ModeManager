package io.mckenz.modemanager.listeners;

import io.mckenz.modemanager.ModeManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Listener for entity events, specifically for item frames and mob spawning
 */
public class EntityListener implements Listener {
    
    private final ModeManager plugin;
    private Set<EntityType> restrictedEntities;
    private boolean restrictAllEntities;
    
    /**
     * Constructor for EntityListener
     * 
     * @param plugin The plugin instance
     */
    public EntityListener(ModeManager plugin) {
        this.plugin = plugin;
        loadRestrictedEntities();
    }
    
    /**
     * Load restricted entity types from config
     */
    public void loadRestrictedEntities() {
        restrictedEntities = new HashSet<>();
        
        // Check if we should restrict all entity types
        Object configValue = plugin.getConfig().get("protection.restricted-entity-types");
        
        if (configValue instanceof String && "ALL".equalsIgnoreCase((String) configValue)) {
            // Restrict all entity types
            restrictAllEntities = true;
            plugin.logDebug("Configured to restrict ALL entity types in creative mode");
            return;
        }
        
        // Otherwise, load specific entity types from config
        restrictAllEntities = false;
        List<String> entityTypeNames;
        
        if (configValue instanceof List) {
            entityTypeNames = plugin.getConfig().getStringList("protection.restricted-entity-types");
        } else {
            // If not properly configured, default to restricting all entity types
            restrictAllEntities = true;
            plugin.logDebug("Invalid restricted-entity-types configuration, defaulting to ALL");
            return;
        }
        
        for (String entityTypeName : entityTypeNames) {
            try {
                EntityType entityType = EntityType.valueOf(entityTypeName.toUpperCase());
                restrictedEntities.add(entityType);
                plugin.logDebug("Added restricted entity type: " + entityType.name());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid entity type in restricted-entity-types list: " + entityTypeName);
            }
        }
        
        plugin.logDebug("Loaded " + restrictedEntities.size() + " restricted entity types");
    }
    
    /**
     * Check if an entity type is restricted
     * 
     * @param entityType The entity type to check
     * @return True if the entity type is restricted, false otherwise
     */
    private boolean isEntityTypeRestricted(EntityType entityType) {
        return restrictAllEntities || restrictedEntities.contains(entityType);
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
    
    /**
     * Handle entity spawn events
     * This is triggered when an entity is spawned in the world
     * 
     * @param event The entity spawn event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        
        // Skip if entity is not restricted
        if (!isEntityTypeRestricted(entity.getType())) {
            return;
        }
        
        // Skip if prevention is disabled
        if (!plugin.getConfig().getBoolean("protection.prevent-creative-mob-spawning", true)) {
            return;
        }
        
        // Check if the entity was spawned by a projectile (like a spawn egg)
        if (entity instanceof ProjectileSource) {
            ProjectileSource source = (ProjectileSource) entity;
            if (source instanceof Player) {
                Player player = (Player) source;
                if (player.getGameMode() == GameMode.CREATIVE && 
                    !player.hasPermission("modemanager.bypass.mobspawning")) {
                    
                    event.setCancelled(true);
                    plugin.getMessageUtil().sendMessage(player, "creative-mob-spawning-blocked");
                    plugin.logDebug("Prevented " + player.getName() + " from spawning mob in creative mode: " + 
                                   entity.getType().name());
                    return;
                }
            }
        }
        
        // Check nearby players (within 5 blocks) who might have spawned this entity
        entity.getWorld().getNearbyEntities(entity.getLocation(), 5, 5, 5).forEach(nearby -> {
            if (nearby instanceof Player) {
                Player player = (Player) nearby;
                if (player.getGameMode() == GameMode.CREATIVE && 
                    !player.hasPermission("modemanager.bypass.mobspawning")) {
                    
                    event.setCancelled(true);
                    
                    // Check if player is holding a spawn egg
                    if (player.getInventory().getItemInMainHand().getType().name().endsWith("_SPAWN_EGG")) {
                        Map<String, String> placeholders = new HashMap<>();
                        String eggType = formatEntityName(player.getInventory().getItemInMainHand().getType().name());
                        placeholders.put("entity", eggType);
                        plugin.getMessageUtil().sendMessage(player, "creative-spawn-egg-blocked", placeholders);
                    } else {
                        plugin.getMessageUtil().sendMessage(player, "creative-mob-spawning-blocked");
                    }
                    
                    plugin.logDebug("Prevented " + player.getName() + " from spawning mob in creative mode: " + 
                                   entity.getType().name());
                    return;
                }
            }
        });
    }
    
    /**
     * Format entity name for display
     * 
     * @param entityName The entity name
     * @return Formatted entity name
     */
    private String formatEntityName(String entityName) {
        // Remove _SPAWN_EGG suffix
        if (entityName.endsWith("_SPAWN_EGG")) {
            entityName = entityName.substring(0, entityName.length() - 11);
        }
        
        String[] parts = entityName.toLowerCase().split("_");
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
    
    /**
     * Reload restricted entity types from config
     */
    public void reloadRestrictedEntities() {
        loadRestrictedEntities();
    }
} 