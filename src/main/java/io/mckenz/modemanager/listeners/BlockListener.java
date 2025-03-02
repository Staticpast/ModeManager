package io.mckenz.modemanager.listeners;

import io.mckenz.modemanager.ModeManager;
import io.mckenz.modemanager.data.PlayerModeData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Listener for block events
 */
public class BlockListener implements Listener {
    
    private final ModeManager plugin;
    private final Set<Material> containerMaterials;
    
    /**
     * Constructor for BlockListener
     * 
     * @param plugin The plugin instance
     */
    public BlockListener(ModeManager plugin) {
        this.plugin = plugin;
        this.containerMaterials = initContainerMaterials();
    }
    
    /**
     * Initialize the set of container materials
     * 
     * @return Set of container materials
     */
    private Set<Material> initContainerMaterials() {
        Set<Material> containers = new HashSet<>();
        
        // Add all chest types
        containers.add(Material.CHEST);
        containers.add(Material.TRAPPED_CHEST);
        containers.add(Material.ENDER_CHEST);
        containers.add(Material.BARREL);
        
        // Add all shulker box types
        for (Material material : Material.values()) {
            if (material.name().endsWith("SHULKER_BOX")) {
                containers.add(material);
            }
        }
        
        // Add furnace types
        containers.add(Material.FURNACE);
        containers.add(Material.BLAST_FURNACE);
        containers.add(Material.SMOKER);
        
        // Add other container types
        containers.add(Material.DISPENSER);
        containers.add(Material.DROPPER);
        containers.add(Material.HOPPER);
        containers.add(Material.BREWING_STAND);
        
        // Add additional container types
        containers.add(Material.LECTERN);
        containers.add(Material.COMPOSTER);
        containers.add(Material.CAULDRON);
        containers.add(Material.LAVA_CAULDRON);
        containers.add(Material.WATER_CAULDRON);
        containers.add(Material.POWDER_SNOW_CAULDRON);
        containers.add(Material.BEEHIVE);
        containers.add(Material.BEE_NEST);
        containers.add(Material.CAMPFIRE);
        containers.add(Material.SOUL_CAMPFIRE);
        containers.add(Material.JUKEBOX);
        
        plugin.logDebug("Initialized " + containers.size() + " container materials");
        return containers;
    }
    
    /**
     * Check if a material is a container
     * 
     * @param material The material to check
     * @return True if the material is a container, false otherwise
     */
    private boolean isContainer(Material material) {
        return containerMaterials.contains(material);
    }
    
    /**
     * Handle block place events
     * 
     * @param event The block place event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        // Prevent placing containers in creative mode if configured
        if (player.getGameMode() == GameMode.CREATIVE && 
            plugin.getConfig().getBoolean("protection.prevent-creative-container-blocks", true) &&
            isContainer(block.getType()) &&
            !player.hasPermission("modemanager.bypass.containerplacement")) {
            
            event.setCancelled(true);
            plugin.getMessageUtil().sendMessage(player, "creative-container-placement-blocked");
            plugin.logDebug("Prevented " + player.getName() + " from placing container block in creative mode: " + block.getType().name());
            return;
        }
        
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