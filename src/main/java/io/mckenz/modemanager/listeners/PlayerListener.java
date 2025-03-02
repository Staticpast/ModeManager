package io.mckenz.modemanager.listeners;

import io.mckenz.modemanager.ModeManager;
import io.mckenz.modemanager.data.PlayerModeData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listener for player events
 */
public class PlayerListener implements Listener {
    
    private final ModeManager plugin;
    private final Map<UUID, GameMode> deathModes = new HashMap<>();
    
    /**
     * Constructor for PlayerListener
     * 
     * @param plugin The plugin instance
     */
    public PlayerListener(ModeManager plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handle player join events
     * 
     * @param event The player join event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Load player data
        PlayerModeData data = plugin.getPlayerDataManager().getPlayerData(player);
        
        // Set the player's game mode to match their stored mode
        if (player.getGameMode() != data.getCurrentMode()) {
            player.setGameMode(data.getCurrentMode());
            plugin.logDebug("Set " + player.getName() + "'s game mode to " + data.getCurrentMode() + " on join");
        }
    }
    
    /**
     * Handle player quit events
     * 
     * @param event The player quit event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Save player data
        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
        
        // Remove player data from memory
        plugin.getPlayerDataManager().removePlayerData(player.getUniqueId());
        
        // Remove from death modes map if present
        deathModes.remove(player.getUniqueId());
        
        plugin.logDebug("Saved and removed " + player.getName() + "'s data on quit");
    }
    
    /**
     * Handle player game mode change events
     * 
     * @param event The player game mode change event
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        GameMode newMode = event.getNewGameMode();
        
        // Skip if the player doesn't have permission to use the plugin
        if (!player.hasPermission("modemanager.use")) {
            return;
        }
        
        // Skip if the new mode is not survival or creative
        if (newMode != GameMode.SURVIVAL && newMode != GameMode.CREATIVE) {
            return;
        }
        
        // Skip if the player doesn't have permission to use creative mode
        if (newMode == GameMode.CREATIVE && !player.hasPermission("modemanager.creative")) {
            event.setCancelled(true);
            plugin.getMessageUtil().sendMessage(player, "no-permission");
            return;
        }
        
        // If the event wasn't triggered by our plugin, handle it
        PlayerModeData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data.getCurrentMode() != newMode) {
            // Cancel the event and let our service handle it
            event.setCancelled(true);
            plugin.getModeService().changePlayerMode(player, newMode, "External command");
        }
    }
    
    /**
     * Handle player drop item events
     * 
     * @param event The player drop item event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        // Prevent dropping items in creative mode if configured
        if (player.getGameMode() == GameMode.CREATIVE && 
            plugin.getConfig().getBoolean("protection.prevent-creative-drops", true)) {
            
            event.setCancelled(true);
            plugin.getMessageUtil().sendMessage(player, "creative-drop-blocked");
            plugin.logDebug("Prevented " + player.getName() + " from dropping items in creative mode");
        }
    }
    
    /**
     * Handle player death events
     * 
     * @param event The player death event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerModeData data = plugin.getPlayerDataManager().getPlayerData(player);
        GameMode currentMode = data.getCurrentMode();
        
        // Store the player's mode at death for respawn handling
        deathModes.put(player.getUniqueId(), currentMode);
        
        // Clear drops if the player is in creative mode
        if (currentMode == GameMode.CREATIVE) {
            event.getDrops().clear();
            plugin.logDebug("Cleared drops for " + player.getName() + " who died in creative mode");
        }
        
        // If we're preserving creative inventory on death, save the current inventory
        if (plugin.getConfig().getBoolean("protection.preserve-creative-inventory-on-death", true)) {
            if (currentMode == GameMode.CREATIVE) {
                // Save creative inventory
                data.setCreativeInventory(player.getInventory().getContents());
                
                // Save armor if enabled
                if (plugin.getConfig().getBoolean("inventories.save-armor-contents", true)) {
                    data.setCreativeArmorContents(player.getInventory().getArmorContents());
                }
                
                // Save offhand if enabled
                if (plugin.getConfig().getBoolean("inventories.save-offhand-items", true)) {
                    data.setCreativeOffHandItem(player.getInventory().getItemInOffHand());
                }
                
                plugin.logDebug("Saved " + player.getName() + "'s creative inventory on death");
            } else if (currentMode == GameMode.SURVIVAL) {
                // We don't need to save survival inventory as Minecraft handles this naturally
                // But we'll log it for debugging
                plugin.logDebug(player.getName() + " died in survival mode, inventory will be handled by Minecraft");
            }
        }
    }
    
    /**
     * Handle player respawn events
     * 
     * @param event The player respawn event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Skip if we don't have a record of the player's death mode
        if (!deathModes.containsKey(playerId)) {
            return;
        }
        
        GameMode deathMode = deathModes.get(playerId);
        deathModes.remove(playerId); // Clean up
        
        // If the player died in creative mode and we're preserving inventory
        if (deathMode == GameMode.CREATIVE && 
            plugin.getConfig().getBoolean("protection.preserve-creative-inventory-on-death", true)) {
            
            // Schedule a task to restore inventory after respawn
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                PlayerModeData data = plugin.getPlayerDataManager().getPlayerData(player);
                
                // Make sure the player is still in creative mode
                if (player.getGameMode() == GameMode.CREATIVE) {
                    // Restore creative inventory
                    if (data.getCreativeInventory() != null) {
                        player.getInventory().setContents(data.getCreativeInventory());
                    }
                    
                    // Restore armor if enabled
                    if (plugin.getConfig().getBoolean("inventories.save-armor-contents", true) && 
                        data.getCreativeArmorContents() != null) {
                        player.getInventory().setArmorContents(data.getCreativeArmorContents());
                    }
                    
                    // Restore offhand if enabled
                    if (plugin.getConfig().getBoolean("inventories.save-offhand-items", true) && 
                        data.getCreativeOffHandItem() != null) {
                        player.getInventory().setItemInOffHand(data.getCreativeOffHandItem());
                    }
                    
                    // Update the player's inventory
                    player.updateInventory();
                    
                    // Send message to player
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("mode", "creative");
                    plugin.getMessageUtil().sendMessage(player, "inventory-restored", placeholders);
                    
                    plugin.logDebug("Restored " + player.getName() + "'s creative inventory after respawn");
                }
            }, 1L); // Run 1 tick after respawn
        }
    }
    
    /**
     * Handle inventory click events
     * 
     * @param event The inventory click event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // Skip if the player is not in creative mode
        if (player.getGameMode() != GameMode.CREATIVE) {
            return;
        }
        
        // Prevent placing items in containers in creative mode if configured
        if (event.getClickedInventory() != null && 
            event.getClickedInventory().getType() != InventoryType.PLAYER && 
            event.getClickedInventory().getType() != InventoryType.CREATIVE && 
            plugin.getConfig().getBoolean("protection.prevent-creative-container-placement", true)) {
            
            event.setCancelled(true);
            plugin.getMessageUtil().sendMessage(player, "creative-container-blocked");
            plugin.logDebug("Prevented " + player.getName() + " from placing items in a container in creative mode");
        }
        
        // Prevent taking items from containers in creative mode if configured
        if (event.getClickedInventory() != null && 
            event.getClickedInventory().getType() != InventoryType.PLAYER && 
            event.getClickedInventory().getType() != InventoryType.CREATIVE && 
            plugin.getConfig().getBoolean("protection.prevent-creative-container-taking", true)) {
            
            event.setCancelled(true);
            plugin.getMessageUtil().sendMessage(player, "creative-container-blocked");
            plugin.logDebug("Prevented " + player.getName() + " from taking items from a container in creative mode");
        }
        
        // Special handling for ender chest if separate inventories are enabled
        if (event.getClickedInventory() != null && 
            event.getClickedInventory().getType() == InventoryType.ENDER_CHEST && 
            plugin.getConfig().getBoolean("inventories.separate-ender-chest", true)) {
            
            // No need to cancel, as we've already set up separate ender chest inventories
            plugin.logDebug(player.getName() + " accessed their creative mode ender chest");
        }
    }
} 