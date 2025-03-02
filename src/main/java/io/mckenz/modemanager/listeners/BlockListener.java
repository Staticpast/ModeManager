package io.mckenz.modemanager.listeners;

import io.mckenz.modemanager.ModeManager;
import io.mckenz.modemanager.data.PlayerModeData;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Listener for block events
 */
public class BlockListener implements Listener {
    
    private final ModeManager plugin;
    
    /**
     * Constructor for BlockListener
     * 
     * @param plugin The plugin instance
     */
    public BlockListener(ModeManager plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handle block place events
     * 
     * @param event The block place event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        // Skip if tracking is disabled
        if (!plugin.getConfig().getBoolean("protection.track-creative-blocks", true)) {
            return;
        }
        
        // Track blocks placed in creative mode
        if (player.getGameMode() == GameMode.CREATIVE) {
            plugin.getCreativeBlockManager().addBlock(block.getLocation(), player.getUniqueId());
            plugin.logDebug("Tracked creative block placed by " + player.getName() + " at " + 
                           block.getLocation().getWorld().getName() + " " + 
                           block.getLocation().getBlockX() + "," + 
                           block.getLocation().getBlockY() + "," + 
                           block.getLocation().getBlockZ());
        }
    }
    
    /**
     * Handle block break events
     * 
     * @param event The block break event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        // Skip if tracking is disabled
        if (!plugin.getConfig().getBoolean("protection.track-creative-blocks", true)) {
            return;
        }
        
        // Prevent breaking creative blocks in survival mode
        if (player.getGameMode() == GameMode.SURVIVAL && 
            plugin.getCreativeBlockManager().isCreativeBlock(block.getLocation())) {
            
            event.setCancelled(true);
            plugin.getMessageUtil().sendMessage(player, "creative-block-protected");
            plugin.logDebug("Prevented " + player.getName() + " from breaking creative block at " + 
                           block.getLocation().getWorld().getName() + " " + 
                           block.getLocation().getBlockX() + "," + 
                           block.getLocation().getBlockY() + "," + 
                           block.getLocation().getBlockZ());
        }
        
        // Remove the block from tracking if it's broken in creative mode
        if (player.getGameMode() == GameMode.CREATIVE && 
            plugin.getCreativeBlockManager().isCreativeBlock(block.getLocation())) {
            
            plugin.getCreativeBlockManager().removeBlock(block.getLocation());
            plugin.logDebug("Removed creative block tracking for block broken by " + player.getName());
        }
    }
} 