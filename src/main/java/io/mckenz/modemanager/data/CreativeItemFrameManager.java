package io.mckenz.modemanager.data;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to track items placed in item frames in creative mode
 */
public class CreativeItemFrameManager {
    private final Map<UUID, UUID> creativeItemFrames = new ConcurrentHashMap<>();
    private final File dataFile;
    private final Logger logger;
    
    /**
     * Constructor for CreativeItemFrameManager
     * 
     * @param dataFolder The data folder
     * @param logger The logger
     */
    public CreativeItemFrameManager(File dataFolder, Logger logger) {
        this.dataFile = new File(dataFolder, "creative-item-frames.yml");
        this.logger = logger;
        loadItemFrames();
    }
    
    /**
     * Add an item frame to the creative item frames list
     * 
     * @param itemFrame The item frame entity
     * @param playerUuid The UUID of the player who placed the item
     */
    public void addItemFrame(ItemFrame itemFrame, UUID playerUuid) {
        creativeItemFrames.put(itemFrame.getUniqueId(), playerUuid);
    }
    
    /**
     * Remove an item frame from the creative item frames list
     * 
     * @param itemFrame The item frame entity
     */
    public void removeItemFrame(ItemFrame itemFrame) {
        creativeItemFrames.remove(itemFrame.getUniqueId());
    }
    
    /**
     * Check if an item frame has an item placed in creative mode
     * 
     * @param itemFrame The item frame entity
     * @return True if the item frame has an item placed in creative mode, false otherwise
     */
    public boolean isCreativeItemFrame(ItemFrame itemFrame) {
        return creativeItemFrames.containsKey(itemFrame.getUniqueId());
    }
    
    /**
     * Get the UUID of the player who placed an item in an item frame in creative mode
     * 
     * @param itemFrame The item frame entity
     * @return The UUID of the player who placed the item, or null if the item was not placed in creative mode
     */
    public UUID getItemFramePlacer(ItemFrame itemFrame) {
        return creativeItemFrames.get(itemFrame.getUniqueId());
    }
    
    /**
     * Save the creative item frames to disk
     */
    public void saveItemFrames() {
        YamlConfiguration config = new YamlConfiguration();
        
        // Convert the map to a format that can be saved
        Map<String, String> saveMap = new HashMap<>();
        for (Map.Entry<UUID, UUID> entry : creativeItemFrames.entrySet()) {
            saveMap.put(entry.getKey().toString(), entry.getValue().toString());
        }
        
        config.set("item-frames", saveMap);
        
        try {
            config.save(dataFile);
            logger.info("Saved " + creativeItemFrames.size() + " creative item frames to disk");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save creative item frames", e);
        }
    }
    
    /**
     * Load the creative item frames from disk
     */
    @SuppressWarnings("unchecked")
    private void loadItemFrames() {
        if (!dataFile.exists()) {
            logger.info("No creative item frames file found, starting with empty list");
            return;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        if (config.getConfigurationSection("item-frames") == null) {
            logger.info("No item frames section found in data file, starting with empty list");
            return;
        }
        
        Map<String, Object> loadedMap = config.getConfigurationSection("item-frames").getValues(false);
        
        for (Map.Entry<String, Object> entry : loadedMap.entrySet()) {
            try {
                UUID itemFrameUuid = UUID.fromString(entry.getKey());
                UUID playerUuid = UUID.fromString(entry.getValue().toString());
                creativeItemFrames.put(itemFrameUuid, playerUuid);
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid UUID in creative item frames file: " + entry.getKey() + " or " + entry.getValue());
            }
        }
        
        logger.info("Loaded " + creativeItemFrames.size() + " creative item frames from disk");
    }
} 