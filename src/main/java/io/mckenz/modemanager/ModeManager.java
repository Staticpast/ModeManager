package io.mckenz.modemanager;

import io.mckenz.modemanager.api.ModeManagerAPI;
import io.mckenz.modemanager.commands.ModeCommand;
import io.mckenz.modemanager.data.CreativeBlockManager;
import io.mckenz.modemanager.data.CreativeItemFrameManager;
import io.mckenz.modemanager.data.ModeChangeRecord;
import io.mckenz.modemanager.data.PlayerDataManager;
import io.mckenz.modemanager.data.PlayerModeData;
import io.mckenz.modemanager.listeners.BlockListener;
import io.mckenz.modemanager.listeners.EntityListener;
import io.mckenz.modemanager.listeners.ItemRestrictionListener;
import io.mckenz.modemanager.listeners.PlayerListener;
import io.mckenz.modemanager.services.ModeService;
import io.mckenz.modemanager.util.MessageUtil;
import io.mckenz.modemanager.util.UpdateChecker;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

/**
 * Main class for the ModeManager plugin
 */
public class ModeManager extends JavaPlugin implements ModeManagerAPI {
    private FileConfiguration config;
    private boolean enabled;
    private boolean debug;
    private UpdateChecker updateChecker;
    private PlayerDataManager playerDataManager;
    private CreativeBlockManager creativeBlockManager;
    private CreativeItemFrameManager creativeItemFrameManager;
    private ModeService modeService;
    private MessageUtil messageUtil;
    private ItemRestrictionListener itemRestrictionListener;
    private EntityListener entityListener;
    
    @Override
    public void onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig();
        loadConfig();
        
        // Initialize managers and services
        playerDataManager = new PlayerDataManager(this);
        creativeBlockManager = new CreativeBlockManager(getDataFolder(), getLogger());
        creativeItemFrameManager = new CreativeItemFrameManager(getDataFolder(), getLogger());
        modeService = new ModeService(this);
        messageUtil = new MessageUtil(this);
        
        // Initialize listeners
        itemRestrictionListener = new ItemRestrictionListener(this);
        entityListener = new EntityListener(this);
        
        // Register events
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(entityListener, this);
        getServer().getPluginManager().registerEvents(itemRestrictionListener, this);
        
        // Register commands
        ModeCommand commandExecutor = new ModeCommand(this, modeService);
        getCommand("mode").setExecutor(commandExecutor);
        getCommand("mode").setTabCompleter(commandExecutor);
        
        // Register API
        getServer().getServicesManager().register(
            ModeManagerAPI.class, 
            this, 
            this, 
            org.bukkit.plugin.ServicePriority.Normal
        );
        
        // Initialize update checker if enabled
        if (config.getBoolean("update-checker.enabled", true)) {
            int resourceId = config.getInt("update-checker.resource-id", 0);
            boolean notifyAdmins = config.getBoolean("update-checker.notify-admins", true);
            
            updateChecker = new UpdateChecker(this, resourceId, notifyAdmins);
            updateChecker.checkForUpdates();
            logDebug("Update checker initialized with resource ID: " + resourceId);
        }
        
        getLogger().info("ModeManager has been enabled!");
        logDebug("Debug mode is enabled");
    }

    /**
     * Loads configuration from config.yml
     */
    public void loadConfig() {
        reloadConfig();
        config = getConfig();
        enabled = config.getBoolean("enabled", true);
        debug = config.getBoolean("debug", false);
        
        // Reload item restrictions if the listener is initialized
        if (itemRestrictionListener != null) {
            itemRestrictionListener.reloadRestrictedItems();
        }
        
        // Reload entity restrictions if the listener is initialized
        if (entityListener != null) {
            entityListener.reloadRestrictedEntities();
        }
        
        logDebug("Configuration loaded");
    }

    /**
     * Logs a debug message if debug mode is enabled
     * 
     * @param message The message to log
     */
    public void logDebug(String message) {
        if (debug) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    @Override
    public void onDisable() {
        // Save all player data
        if (playerDataManager != null) {
            playerDataManager.saveAllPlayerData();
        }
        
        // Save creative blocks
        if (creativeBlockManager != null) {
            creativeBlockManager.saveBlocks();
        }
        
        // Save creative item frames
        if (creativeItemFrameManager != null) {
            creativeItemFrameManager.saveItemFrames();
        }
        
        getLogger().info("ModeManager has been disabled!");
    }

    // API Methods
    
    @Override
    public boolean isPluginEnabled() {
        return isPluginFunctionalityEnabled();
    }
    
    @Override
    public void registerEvents(Plugin plugin, Listener listener) {
        getServer().getPluginManager().registerEvents(listener, plugin);
    }
    
    @Override
    public GameMode getPlayerMode(Player player) {
        return getPlayerMode(player.getUniqueId());
    }
    
    @Override
    public GameMode getPlayerMode(UUID playerUuid) {
        PlayerModeData data = playerDataManager.getPlayerData(playerUuid);
        return data != null ? data.getCurrentMode() : null;
    }
    
    @Override
    public boolean changePlayerMode(Player player, GameMode newMode, String reason) {
        return modeService.changePlayerMode(player, newMode, reason);
    }
    
    @Override
    public boolean isPlayerInCooldown(Player player) {
        PlayerModeData data = playerDataManager.getPlayerData(player);
        int cooldown = config.getInt("mode-switching.cooldown-seconds", 30);
        return data != null && data.isInCooldown(cooldown);
    }
    
    @Override
    public long getPlayerRemainingCooldown(Player player) {
        PlayerModeData data = playerDataManager.getPlayerData(player);
        int cooldown = config.getInt("mode-switching.cooldown-seconds", 30);
        return data != null ? data.getRemainingCooldown(cooldown) : 0;
    }
    
    @Override
    public boolean isCreativeBlock(Location location) {
        return creativeBlockManager.isCreativeBlock(location);
    }
    
    @Override
    public UUID getCreativeBlockPlacer(Location location) {
        return creativeBlockManager.getBlockPlacer(location);
    }
    
    @Override
    public boolean isCreativeItemFrame(ItemFrame itemFrame) {
        return creativeItemFrameManager.isCreativeItemFrame(itemFrame);
    }
    
    @Override
    public UUID getCreativeItemFramePlacer(ItemFrame itemFrame) {
        return creativeItemFrameManager.getItemFramePlacer(itemFrame);
    }
    
    @Override
    public List<ModeChangeRecord> getPlayerModeHistory(Player player) {
        PlayerModeData data = playerDataManager.getPlayerData(player);
        return data != null ? data.getModeHistory() : List.of();
    }
    
    /**
     * Checks if the plugin functionality is enabled
     * 
     * @return True if enabled, false otherwise
     */
    public boolean isPluginFunctionalityEnabled() {
        return enabled;
    }
    
    /**
     * Sets whether the plugin functionality is enabled
     * 
     * @param enabled True to enable, false to disable
     */
    public void setPluginFunctionalityEnabled(boolean enabled) {
        this.enabled = enabled;
        config.set("enabled", enabled);
        saveConfig();
    }
    
    /**
     * Checks if debug mode is enabled
     * 
     * @return True if debug mode is enabled, false otherwise
     */
    public boolean isDebugEnabled() {
        return debug;
    }
    
    /**
     * Sets whether debug mode is enabled
     * 
     * @param debug True to enable debug mode, false to disable
     */
    public void setDebugEnabled(boolean debug) {
        this.debug = debug;
        config.set("debug", debug);
        saveConfig();
    }
    
    /**
     * Gets the update checker instance
     * 
     * @return The update checker instance, or null if update checking is disabled
     */
    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }
    
    /**
     * Gets the player data manager
     * 
     * @return The player data manager
     */
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    
    /**
     * Gets the creative block manager
     * 
     * @return The creative block manager
     */
    public CreativeBlockManager getCreativeBlockManager() {
        return creativeBlockManager;
    }
    
    /**
     * Gets the creative item frame manager
     * 
     * @return The creative item frame manager
     */
    public CreativeItemFrameManager getCreativeItemFrameManager() {
        return creativeItemFrameManager;
    }
    
    /**
     * Gets the mode service
     * 
     * @return The mode service
     */
    public ModeService getModeService() {
        return modeService;
    }
    
    /**
     * Gets the message utility
     * 
     * @return The message utility
     */
    public MessageUtil getMessageUtil() {
        return messageUtil;
    }
}