package io.mckenz.modemanager.services;

import io.mckenz.modemanager.ModeManager;
import io.mckenz.modemanager.data.PlayerModeData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Service class for handling mode switching
 */
public class ModeService {
    private final ModeManager plugin;
    
    /**
     * Constructor for ModeService
     * 
     * @param plugin The plugin instance
     */
    public ModeService(ModeManager plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Change a player's mode
     * 
     * @param player The player
     * @param newMode The new mode
     * @param reason The reason for the change
     * @return True if the mode was changed successfully
     */
    public boolean changePlayerMode(Player player, GameMode newMode, String reason) {
        PlayerModeData data = plugin.getPlayerDataManager().getPlayerData(player);
        
        // Check if the player is already in the requested mode
        if (data.getCurrentMode() == newMode) {
            plugin.getMessageUtil().sendMessage(player, "already-in-mode", Map.of("mode", newMode.name()));
            return true;
        }
        
        // Check cooldown
        int cooldown = plugin.getConfig().getInt("mode-switching.cooldown-seconds", 30);
        if (data.isInCooldown(cooldown)) {
            long remainingCooldown = data.getRemainingCooldown(cooldown);
            plugin.getMessageUtil().sendMessage(player, "cooldown", Map.of("time", String.valueOf(remainingCooldown)));
            return false;
        }
        
        // Save current inventory
        savePlayerInventory(player, data);
        
        // Change the mode
        GameMode oldMode = data.getCurrentMode();
        data.setCurrentMode(newMode, reason);
        
        // Restore inventory for the new mode
        restorePlayerInventory(player, data);
        
        // Set the player's game mode
        player.setGameMode(newMode);
        
        // Send message to the player
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("mode", newMode.name());
        plugin.getMessageUtil().sendMessage(player, "mode-changed", placeholders);
        
        // Broadcast if enabled
        if (plugin.getConfig().getBoolean("mode-switching.broadcast-changes", false)) {
            placeholders.put("player", player.getName());
            placeholders.put("old_mode", oldMode.name());
            plugin.getMessageUtil().broadcastMessage("mode-changed-broadcast", placeholders);
        }
        
        // Save player data
        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
        
        plugin.logDebug("Changed " + player.getName() + "'s mode from " + oldMode + " to " + newMode + " (" + reason + ")");
        return true;
    }
    
    /**
     * Force a player's mode change, bypassing cooldowns and permissions
     * 
     * @param player The player
     * @param newMode The new mode
     * @param reason The reason for the change
     * @param adminName The name of the admin who forced the mode change
     * @return True if the mode was changed successfully
     */
    public boolean forcePlayerMode(Player player, GameMode newMode, String reason, String adminName) {
        PlayerModeData data = plugin.getPlayerDataManager().getPlayerData(player);
        
        // Check if the player is already in the requested mode
        if (data.getCurrentMode() == newMode) {
            return true;
        }
        
        // Save current inventory
        savePlayerInventory(player, data);
        
        // Change the mode
        GameMode oldMode = data.getCurrentMode();
        String fullReason = "Forced by " + adminName + (reason != null && !reason.isEmpty() ? ": " + reason : "");
        data.setCurrentMode(newMode, fullReason);
        
        // Restore inventory for the new mode
        restorePlayerInventory(player, data);
        
        // Set the player's game mode
        player.setGameMode(newMode);
        
        // Send message to the player
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("mode", newMode.name());
        placeholders.put("admin", adminName);
        plugin.getMessageUtil().sendMessage(player, "mode-forced", placeholders);
        
        // Broadcast if enabled
        if (plugin.getConfig().getBoolean("mode-switching.broadcast-changes", false)) {
            placeholders.put("player", player.getName());
            placeholders.put("old_mode", oldMode.name());
            plugin.getMessageUtil().broadcastMessage("mode-changed-broadcast", placeholders);
        }
        
        // Save player data
        plugin.getPlayerDataManager().savePlayerData(player.getUniqueId());
        
        plugin.logDebug("Admin " + adminName + " forced " + player.getName() + "'s mode from " + oldMode + " to " + newMode + " (" + reason + ")");
        return true;
    }
    
    /**
     * Save a player's inventory
     * 
     * @param player The player
     * @param data The player's mode data
     */
    private void savePlayerInventory(Player player, PlayerModeData data) {
        GameMode currentMode = data.getCurrentMode();
        
        if (currentMode == GameMode.SURVIVAL) {
            // Save survival inventory
            data.setSurvivalInventory(player.getInventory().getContents());
            
            // Save armor if enabled
            if (plugin.getConfig().getBoolean("inventories.save-armor-contents", true)) {
                data.setSurvivalArmorContents(player.getInventory().getArmorContents());
            }
            
            // Save offhand if enabled
            if (plugin.getConfig().getBoolean("inventories.save-offhand-items", true)) {
                data.setSurvivalOffHandItem(player.getInventory().getItemInOffHand());
            }
            
            // Save ender chest if enabled
            if (plugin.getConfig().getBoolean("inventories.separate-ender-chest", true)) {
                data.setSurvivalEnderChestContents(player.getEnderChest().getContents());
            }
        } else if (currentMode == GameMode.CREATIVE) {
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
            
            // Save ender chest if enabled
            if (plugin.getConfig().getBoolean("inventories.separate-ender-chest", true)) {
                data.setCreativeEnderChestContents(player.getEnderChest().getContents());
            }
        }
    }
    
    /**
     * Restore a player's inventory
     * 
     * @param player The player
     * @param data The player's mode data
     */
    private void restorePlayerInventory(Player player, PlayerModeData data) {
        GameMode newMode = data.getCurrentMode();
        
        // Clear inventory first
        player.getInventory().clear();
        
        if (newMode == GameMode.SURVIVAL) {
            // Restore survival inventory
            if (data.getSurvivalInventory() != null) {
                player.getInventory().setContents(data.getSurvivalInventory());
            }
            
            // Restore armor if enabled
            if (plugin.getConfig().getBoolean("inventories.save-armor-contents", true) && 
                data.getSurvivalArmorContents() != null) {
                player.getInventory().setArmorContents(data.getSurvivalArmorContents());
            }
            
            // Restore offhand if enabled
            if (plugin.getConfig().getBoolean("inventories.save-offhand-items", true) && 
                data.getSurvivalOffHandItem() != null) {
                player.getInventory().setItemInOffHand(data.getSurvivalOffHandItem());
            }
            
            // Restore ender chest if enabled
            if (plugin.getConfig().getBoolean("inventories.separate-ender-chest", true) && 
                data.getSurvivalEnderChestContents() != null) {
                player.getEnderChest().clear();
                player.getEnderChest().setContents(data.getSurvivalEnderChestContents());
            }
        } else if (newMode == GameMode.CREATIVE) {
            // Clear inventory if configured
            if (plugin.getConfig().getBoolean("inventories.clear-on-creative", true)) {
                player.getInventory().clear();
                player.getInventory().setArmorContents(new ItemStack[4]);
                player.getInventory().setItemInOffHand(null);
            } else {
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
            }
            
            // Restore ender chest if enabled
            if (plugin.getConfig().getBoolean("inventories.separate-ender-chest", true) && 
                data.getCreativeEnderChestContents() != null) {
                player.getEnderChest().clear();
                player.getEnderChest().setContents(data.getCreativeEnderChestContents());
            }
        }
        
        // Update the player's inventory
        player.updateInventory();
    }
} 