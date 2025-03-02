package io.mckenz.modemanager.api;

import io.mckenz.modemanager.data.ModeChangeRecord;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

/**
 * API interface for ModeManager functionality
 * This allows other plugins to interact with the ModeManager plugin
 */
public interface ModeManagerAPI {
    
    /**
     * Checks if the plugin functionality is enabled
     * 
     * @return True if enabled, false otherwise
     */
    boolean isPluginEnabled();
    
    /**
     * Registers a listener for plugin events
     * This is the recommended way to listen for events
     * 
     * @param plugin The plugin registering the listener
     * @param listener The listener to register
     */
    void registerEvents(Plugin plugin, Listener listener);
    
    /**
     * Gets the current mode of a player
     * 
     * @param player The player to check
     * @return The player's current mode
     */
    GameMode getPlayerMode(Player player);
    
    /**
     * Gets the current mode of a player
     * 
     * @param playerUuid The UUID of the player to check
     * @return The player's current mode, or null if the player has no data
     */
    GameMode getPlayerMode(UUID playerUuid);
    
    /**
     * Changes a player's mode
     * 
     * @param player The player to change mode for
     * @param newMode The new mode to set
     * @param reason The reason for the change
     * @return True if the mode was changed successfully, false otherwise
     */
    boolean changePlayerMode(Player player, GameMode newMode, String reason);
    
    /**
     * Checks if a player is in cooldown
     * 
     * @param player The player to check
     * @return True if the player is in cooldown, false otherwise
     */
    boolean isPlayerInCooldown(Player player);
    
    /**
     * Gets the remaining cooldown time for a player
     * 
     * @param player The player to check
     * @return The remaining cooldown time in seconds, or 0 if not in cooldown
     */
    long getPlayerRemainingCooldown(Player player);
    
    /**
     * Checks if a block was placed in creative mode
     * 
     * @param location The location of the block
     * @return True if the block was placed in creative mode, false otherwise
     */
    boolean isCreativeBlock(Location location);
    
    /**
     * Gets the UUID of the player who placed a block in creative mode
     * 
     * @param location The location of the block
     * @return The UUID of the player who placed the block, or null if the block was not placed in creative mode
     */
    UUID getCreativeBlockPlacer(Location location);
    
    /**
     * Checks if an item frame has an item placed in creative mode
     * 
     * @param itemFrame The item frame to check
     * @return True if the item frame has an item placed in creative mode, false otherwise
     */
    boolean isCreativeItemFrame(ItemFrame itemFrame);
    
    /**
     * Gets the UUID of the player who placed an item in an item frame in creative mode
     * 
     * @param itemFrame The item frame to check
     * @return The UUID of the player who placed the item, or null if the item was not placed in creative mode
     */
    UUID getCreativeItemFramePlacer(ItemFrame itemFrame);
    
    /**
     * Gets a player's mode history
     * 
     * @param player The player to get history for
     * @return A list of mode change records, or an empty list if the player has no history
     */
    List<ModeChangeRecord> getPlayerModeHistory(Player player);
}