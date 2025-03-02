package io.mckenz.modemanager.data;

import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class to store player mode data
 */
public class PlayerModeData {
    private final UUID playerUuid;
    private GameMode currentMode;
    private ItemStack[] survivalInventory;
    private ItemStack[] survivalArmorContents;
    private ItemStack[] survivalEnderChestContents;
    private ItemStack survivalOffHandItem;
    private ItemStack[] creativeInventory;
    private ItemStack[] creativeArmorContents;
    private ItemStack[] creativeEnderChestContents;
    private ItemStack creativeOffHandItem;
    private Instant lastModeSwitch;
    private final List<ModeChangeRecord> modeHistory;
    
    /**
     * Constructor for PlayerModeData
     * 
     * @param playerUuid The UUID of the player
     * @param initialMode The initial game mode of the player
     */
    public PlayerModeData(UUID playerUuid, GameMode initialMode) {
        this.playerUuid = playerUuid;
        this.currentMode = initialMode;
        this.lastModeSwitch = Instant.now();
        this.modeHistory = new ArrayList<>();
        
        // Add initial mode to history
        this.modeHistory.add(new ModeChangeRecord(initialMode, Instant.now(), "Initial mode"));
    }
    
    /**
     * Get the player UUID
     * 
     * @return The player UUID
     */
    public UUID getPlayerUuid() {
        return playerUuid;
    }
    
    /**
     * Get the current game mode
     * 
     * @return The current game mode
     */
    public GameMode getCurrentMode() {
        return currentMode;
    }
    
    /**
     * Set the current game mode
     * 
     * @param mode The new game mode
     * @param reason The reason for the mode change
     */
    public void setCurrentMode(GameMode mode, String reason) {
        this.currentMode = mode;
        this.lastModeSwitch = Instant.now();
        this.modeHistory.add(new ModeChangeRecord(mode, Instant.now(), reason));
    }
    
    /**
     * Get the survival inventory
     * 
     * @return The survival inventory
     */
    public ItemStack[] getSurvivalInventory() {
        return survivalInventory;
    }
    
    /**
     * Set the survival inventory
     * 
     * @param survivalInventory The survival inventory
     */
    public void setSurvivalInventory(ItemStack[] survivalInventory) {
        this.survivalInventory = survivalInventory;
    }
    
    /**
     * Get the survival armor contents
     * 
     * @return The survival armor contents
     */
    public ItemStack[] getSurvivalArmorContents() {
        return survivalArmorContents;
    }
    
    /**
     * Set the survival armor contents
     * 
     * @param survivalArmorContents The survival armor contents
     */
    public void setSurvivalArmorContents(ItemStack[] survivalArmorContents) {
        this.survivalArmorContents = survivalArmorContents;
    }
    
    /**
     * Get the survival ender chest contents
     * 
     * @return The survival ender chest contents
     */
    public ItemStack[] getSurvivalEnderChestContents() {
        return survivalEnderChestContents;
    }
    
    /**
     * Set the survival ender chest contents
     * 
     * @param survivalEnderChestContents The survival ender chest contents
     */
    public void setSurvivalEnderChestContents(ItemStack[] survivalEnderChestContents) {
        this.survivalEnderChestContents = survivalEnderChestContents;
    }
    
    /**
     * Get the survival off hand item
     * 
     * @return The survival off hand item
     */
    public ItemStack getSurvivalOffHandItem() {
        return survivalOffHandItem;
    }
    
    /**
     * Set the survival off hand item
     * 
     * @param survivalOffHandItem The survival off hand item
     */
    public void setSurvivalOffHandItem(ItemStack survivalOffHandItem) {
        this.survivalOffHandItem = survivalOffHandItem;
    }
    
    /**
     * Get the creative inventory
     * 
     * @return The creative inventory
     */
    public ItemStack[] getCreativeInventory() {
        return creativeInventory;
    }
    
    /**
     * Set the creative inventory
     * 
     * @param creativeInventory The creative inventory
     */
    public void setCreativeInventory(ItemStack[] creativeInventory) {
        this.creativeInventory = creativeInventory;
    }
    
    /**
     * Get the creative armor contents
     * 
     * @return The creative armor contents
     */
    public ItemStack[] getCreativeArmorContents() {
        return creativeArmorContents;
    }
    
    /**
     * Set the creative armor contents
     * 
     * @param creativeArmorContents The creative armor contents
     */
    public void setCreativeArmorContents(ItemStack[] creativeArmorContents) {
        this.creativeArmorContents = creativeArmorContents;
    }
    
    /**
     * Get the creative ender chest contents
     * 
     * @return The creative ender chest contents
     */
    public ItemStack[] getCreativeEnderChestContents() {
        return creativeEnderChestContents;
    }
    
    /**
     * Set the creative ender chest contents
     * 
     * @param creativeEnderChestContents The creative ender chest contents
     */
    public void setCreativeEnderChestContents(ItemStack[] creativeEnderChestContents) {
        this.creativeEnderChestContents = creativeEnderChestContents;
    }
    
    /**
     * Get the creative off hand item
     * 
     * @return The creative off hand item
     */
    public ItemStack getCreativeOffHandItem() {
        return creativeOffHandItem;
    }
    
    /**
     * Set the creative off hand item
     * 
     * @param creativeOffHandItem The creative off hand item
     */
    public void setCreativeOffHandItem(ItemStack creativeOffHandItem) {
        this.creativeOffHandItem = creativeOffHandItem;
    }
    
    /**
     * Get the last mode switch time
     * 
     * @return The last mode switch time
     */
    public Instant getLastModeSwitch() {
        return lastModeSwitch;
    }
    
    /**
     * Get the mode history
     * 
     * @return The mode history
     */
    public List<ModeChangeRecord> getModeHistory() {
        return new ArrayList<>(modeHistory);
    }
    
    /**
     * Check if the player is in cooldown
     * 
     * @param cooldownSeconds The cooldown in seconds
     * @return True if the player is in cooldown, false otherwise
     */
    public boolean isInCooldown(int cooldownSeconds) {
        return Instant.now().isBefore(lastModeSwitch.plusSeconds(cooldownSeconds));
    }
    
    /**
     * Get the remaining cooldown time in seconds
     * 
     * @param cooldownSeconds The cooldown in seconds
     * @return The remaining cooldown time in seconds
     */
    public long getRemainingCooldown(int cooldownSeconds) {
        Instant cooldownEnd = lastModeSwitch.plusSeconds(cooldownSeconds);
        return Math.max(0, cooldownEnd.getEpochSecond() - Instant.now().getEpochSecond());
    }
} 