package io.mckenz.modemanager.util;

import io.mckenz.modemanager.ModeManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for handling messages
 */
public class MessageUtil {
    private final ModeManager plugin;
    
    /**
     * Constructor for MessageUtil
     * 
     * @param plugin The plugin instance
     */
    public MessageUtil(ModeManager plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Send a message to a player
     * 
     * @param sender The command sender
     * @param messageKey The message key in the config
     */
    public void sendMessage(CommandSender sender, String messageKey) {
        sendMessage(sender, messageKey, new HashMap<>());
    }
    
    /**
     * Send a message to a player with placeholders
     * 
     * @param sender The command sender
     * @param messageKey The message key in the config
     * @param placeholders The placeholders to replace
     */
    public void sendMessage(CommandSender sender, String messageKey, Map<String, String> placeholders) {
        String message = getMessage(messageKey);
        
        if (message.isEmpty()) {
            return;
        }
        
        // Replace placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        
        // Check if this message should be sent to the action bar
        boolean useActionBar = plugin.getConfig().getBoolean("messages.action-bar-messages." + messageKey, false);
        
        if (useActionBar && sender instanceof Player) {
            sendActionBar((Player) sender, colorize(message));
        } else {
            sender.sendMessage(colorize(message));
        }
    }
    
    /**
     * Send an action bar message to a player
     * 
     * @param player The player
     * @param message The message to send
     */
    public void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                                   net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
    }
    
    /**
     * Send an action bar message to a player with placeholders
     * 
     * @param player The player
     * @param messageKey The message key in the config
     * @param placeholders The placeholders to replace
     */
    public void sendActionBarMessage(Player player, String messageKey, Map<String, String> placeholders) {
        String message = plugin.getConfig().getString("messages." + messageKey, "");
        
        if (message.isEmpty()) {
            plugin.logDebug("Message key not found: " + messageKey);
            return;
        }
        
        // Replace placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        
        sendActionBar(player, colorize(message));
    }
    
    /**
     * Get a message from the config
     * 
     * @param messageKey The message key in the config
     * @return The message
     */
    public String getMessage(String messageKey) {
        String message = plugin.getConfig().getString("messages." + messageKey, "");
        
        if (message.isEmpty()) {
            plugin.logDebug("Message key not found: " + messageKey);
            return "";
        }
        
        // Add prefix if the message doesn't already have it and it's not an action bar message
        boolean isActionBarMessage = plugin.getConfig().getBoolean("messages.action-bar-messages." + messageKey, false);
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        if (!message.startsWith(prefix) && !prefix.isEmpty() && !isActionBarMessage) {
            message = prefix + message;
        }
        
        return message;
    }
    
    /**
     * Colorize a message
     * 
     * @param message The message to colorize
     * @return The colorized message
     */
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * Broadcast a message to all players
     * 
     * @param messageKey The message key in the config
     * @param placeholders The placeholders to replace
     */
    public void broadcastMessage(String messageKey, Map<String, String> placeholders) {
        String message = getMessage(messageKey);
        
        if (message.isEmpty()) {
            return;
        }
        
        // Replace placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        
        String finalMessage = colorize(message);
        
        // Check if this message should be sent to the action bar
        boolean useActionBar = plugin.getConfig().getBoolean("messages.action-bar-messages." + messageKey, false);
        
        if (useActionBar) {
            plugin.getServer().getOnlinePlayers().forEach(player -> sendActionBar(player, finalMessage));
            plugin.getServer().getConsoleSender().sendMessage(finalMessage); // Console still gets regular message
        } else {
            plugin.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(finalMessage));
            plugin.getServer().getConsoleSender().sendMessage(finalMessage);
        }
    }
    
    /**
     * Send a message to a player without adding the prefix
     * 
     * @param sender The command sender
     * @param messageKey The message key in the config
     */
    public void sendMessageWithoutPrefix(CommandSender sender, String messageKey) {
        sendMessageWithoutPrefix(sender, messageKey, new HashMap<>());
    }
    
    /**
     * Send a message to a player with placeholders but without adding the prefix
     * 
     * @param sender The command sender
     * @param messageKey The message key in the config
     * @param placeholders The placeholders to replace
     */
    public void sendMessageWithoutPrefix(CommandSender sender, String messageKey, Map<String, String> placeholders) {
        String message = plugin.getConfig().getString("messages." + messageKey, "");
        
        if (message.isEmpty()) {
            plugin.logDebug("Message key not found: " + messageKey);
            return;
        }
        
        // Replace placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        
        // Check if this message should be sent to the action bar
        boolean useActionBar = plugin.getConfig().getBoolean("messages.action-bar-messages." + messageKey, false);
        
        if (useActionBar && sender instanceof Player) {
            sendActionBar((Player) sender, colorize(message));
        } else {
            sender.sendMessage(colorize(message));
        }
    }
} 