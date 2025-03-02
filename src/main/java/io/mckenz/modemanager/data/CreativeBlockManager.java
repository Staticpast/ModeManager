package io.mckenz.modemanager.data;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to track blocks placed in creative mode
 */
public class CreativeBlockManager {
    private final Map<String, UUID> creativeBlocks = new ConcurrentHashMap<>();
    private final File dataFile;
    private final Logger logger;
    
    /**
     * Constructor for CreativeBlockManager
     * 
     * @param dataFolder The data folder
     * @param logger The logger
     */
    public CreativeBlockManager(File dataFolder, Logger logger) {
        this.dataFile = new File(dataFolder, "creative-blocks.yml");
        this.logger = logger;
        loadBlocks();
    }
    
    /**
     * Add a block to the creative blocks list
     * 
     * @param location The location of the block
     * @param playerUuid The UUID of the player who placed the block
     */
    public void addBlock(Location location, UUID playerUuid) {
        String locationKey = locationToString(location);
        creativeBlocks.put(locationKey, playerUuid);
    }
    
    /**
     * Remove a block from the creative blocks list
     * 
     * @param location The location of the block
     */
    public void removeBlock(Location location) {
        String locationKey = locationToString(location);
        creativeBlocks.remove(locationKey);
    }
    
    /**
     * Check if a block was placed in creative mode
     * 
     * @param location The location of the block
     * @return True if the block was placed in creative mode, false otherwise
     */
    public boolean isCreativeBlock(Location location) {
        String locationKey = locationToString(location);
        return creativeBlocks.containsKey(locationKey);
    }
    
    /**
     * Get the UUID of the player who placed a block in creative mode
     * 
     * @param location The location of the block
     * @return The UUID of the player who placed the block, or null if the block was not placed in creative mode
     */
    public UUID getBlockPlacer(Location location) {
        String locationKey = locationToString(location);
        return creativeBlocks.get(locationKey);
    }
    
    /**
     * Save the creative blocks to disk
     */
    public void saveBlocks() {
        YamlConfiguration config = new YamlConfiguration();
        
        // Convert the map to a format that can be saved
        Map<String, String> saveMap = new HashMap<>();
        for (Map.Entry<String, UUID> entry : creativeBlocks.entrySet()) {
            saveMap.put(entry.getKey(), entry.getValue().toString());
        }
        
        config.set("blocks", saveMap);
        
        try {
            config.save(dataFile);
            logger.info("Saved " + creativeBlocks.size() + " creative blocks to disk");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save creative blocks", e);
        }
    }
    
    /**
     * Load the creative blocks from disk
     */
    @SuppressWarnings("unchecked")
    private void loadBlocks() {
        if (!dataFile.exists()) {
            logger.info("No creative blocks file found, starting with empty list");
            return;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        Map<String, Object> loadedMap = config.getConfigurationSection("blocks").getValues(false);
        
        for (Map.Entry<String, Object> entry : loadedMap.entrySet()) {
            creativeBlocks.put(entry.getKey(), UUID.fromString(entry.getValue().toString()));
        }
        
        logger.info("Loaded " + creativeBlocks.size() + " creative blocks from disk");
    }
    
    /**
     * Convert a location to a string
     * 
     * @param location The location
     * @return The string representation of the location
     */
    private String locationToString(Location location) {
        return location.getWorld().getName() + "," + 
               location.getBlockX() + "," + 
               location.getBlockY() + "," + 
               location.getBlockZ();
    }
} 