package io.mckenz.modemanager.data;

import io.mckenz.modemanager.ModeManager;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manager class for player mode data
 */
public class PlayerDataManager {
    private final ModeManager plugin;
    private final Map<UUID, PlayerModeData> playerData = new HashMap<>();
    private final File dataFolder;
    
    /**
     * Constructor for PlayerDataManager
     * 
     * @param plugin The plugin instance
     */
    public PlayerDataManager(ModeManager plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        
        // Create the data folder if it doesn't exist
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().severe("Failed to create player data folder");
        }
    }
    
    /**
     * Get player mode data
     * 
     * @param player The player
     * @return The player mode data
     */
    public PlayerModeData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }
    
    /**
     * Get player mode data
     * 
     * @param playerUuid The player UUID
     * @return The player mode data
     */
    public PlayerModeData getPlayerData(UUID playerUuid) {
        PlayerModeData data = playerData.get(playerUuid);
        
        if (data == null) {
            // Try to load from disk
            data = loadPlayerData(playerUuid);
            
            if (data == null) {
                // Create new data with default mode
                GameMode defaultMode = getDefaultGameMode();
                data = new PlayerModeData(playerUuid, defaultMode);
                plugin.logDebug("Created new player data for " + playerUuid + " with default mode " + defaultMode);
            }
            
            playerData.put(playerUuid, data);
        }
        
        return data;
    }
    
    /**
     * Save player mode data
     * 
     * @param playerUuid The player UUID
     */
    public void savePlayerData(UUID playerUuid) {
        PlayerModeData data = playerData.get(playerUuid);
        
        if (data == null) {
            plugin.logDebug("No data to save for player " + playerUuid);
            return;
        }
        
        File playerFile = new File(dataFolder, playerUuid.toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        
        // Save basic data
        config.set("uuid", playerUuid.toString());
        config.set("current-mode", data.getCurrentMode().name());
        config.set("last-mode-switch", data.getLastModeSwitch().getEpochSecond());
        
        // Save inventories
        if (data.getSurvivalInventory() != null) {
            config.set("survival.inventory", data.getSurvivalInventory());
        }
        
        if (data.getSurvivalArmorContents() != null) {
            config.set("survival.armor", data.getSurvivalArmorContents());
        }
        
        if (data.getSurvivalEnderChestContents() != null) {
            config.set("survival.enderchest", data.getSurvivalEnderChestContents());
        }
        
        if (data.getSurvivalOffHandItem() != null) {
            config.set("survival.offhand", data.getSurvivalOffHandItem());
        }
        
        if (data.getCreativeInventory() != null) {
            config.set("creative.inventory", data.getCreativeInventory());
        }
        
        if (data.getCreativeArmorContents() != null) {
            config.set("creative.armor", data.getCreativeArmorContents());
        }
        
        if (data.getCreativeEnderChestContents() != null) {
            config.set("creative.enderchest", data.getCreativeEnderChestContents());
        }
        
        if (data.getCreativeOffHandItem() != null) {
            config.set("creative.offhand", data.getCreativeOffHandItem());
        }
        
        // Save mode history
        int i = 0;
        for (ModeChangeRecord record : data.getModeHistory()) {
            config.set("history." + i + ".mode", record.getGameMode().name());
            config.set("history." + i + ".timestamp", record.getTimestamp().getEpochSecond());
            config.set("history." + i + ".reason", record.getReason());
            i++;
        }
        
        try {
            config.save(playerFile);
            plugin.logDebug("Saved player data for " + playerUuid);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + playerUuid, e);
        }
    }
    
    /**
     * Load player mode data from disk
     * 
     * @param playerUuid The player UUID
     * @return The player mode data, or null if it doesn't exist
     */
    private PlayerModeData loadPlayerData(UUID playerUuid) {
        File playerFile = new File(dataFolder, playerUuid.toString() + ".yml");
        
        if (!playerFile.exists()) {
            plugin.logDebug("No player data file found for " + playerUuid);
            return null;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        
        // Load basic data
        GameMode currentMode = GameMode.valueOf(config.getString("current-mode", getDefaultGameMode().name()));
        Instant lastModeSwitch = Instant.ofEpochSecond(config.getLong("last-mode-switch", Instant.now().getEpochSecond()));
        
        PlayerModeData data = new PlayerModeData(playerUuid, currentMode);
        
        // Load inventories
        if (config.contains("survival.inventory")) {
            data.setSurvivalInventory(config.getList("survival.inventory").toArray(new ItemStack[0]));
        }
        
        if (config.contains("survival.armor")) {
            data.setSurvivalArmorContents(config.getList("survival.armor").toArray(new ItemStack[0]));
        }
        
        if (config.contains("survival.enderchest")) {
            data.setSurvivalEnderChestContents(config.getList("survival.enderchest").toArray(new ItemStack[0]));
        }
        
        if (config.contains("survival.offhand")) {
            data.setSurvivalOffHandItem((ItemStack) config.get("survival.offhand"));
        }
        
        if (config.contains("creative.inventory")) {
            data.setCreativeInventory(config.getList("creative.inventory").toArray(new ItemStack[0]));
        }
        
        if (config.contains("creative.armor")) {
            data.setCreativeArmorContents(config.getList("creative.armor").toArray(new ItemStack[0]));
        }
        
        if (config.contains("creative.enderchest")) {
            data.setCreativeEnderChestContents(config.getList("creative.enderchest").toArray(new ItemStack[0]));
        }
        
        if (config.contains("creative.offhand")) {
            data.setCreativeOffHandItem((ItemStack) config.get("creative.offhand"));
        }
        
        plugin.logDebug("Loaded player data for " + playerUuid);
        return data;
    }
    
    /**
     * Save all player data
     */
    public void saveAllPlayerData() {
        for (UUID playerUuid : playerData.keySet()) {
            savePlayerData(playerUuid);
        }
    }
    
    /**
     * Remove player data from memory
     * 
     * @param playerUuid The player UUID
     */
    public void removePlayerData(UUID playerUuid) {
        playerData.remove(playerUuid);
    }
    
    /**
     * Get the default game mode from config
     * 
     * @return The default game mode
     */
    private GameMode getDefaultGameMode() {
        String defaultMode = plugin.getConfig().getString("mode-switching.default-mode", "SURVIVAL");
        try {
            return GameMode.valueOf(defaultMode);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid default mode in config: " + defaultMode + ". Using SURVIVAL instead.");
            return GameMode.SURVIVAL;
        }
    }
} 