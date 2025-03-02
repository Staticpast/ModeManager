package io.mckenz.modemanager.commands;

import io.mckenz.modemanager.ModeManager;
import io.mckenz.modemanager.data.ModeChangeRecord;
import io.mckenz.modemanager.data.PlayerModeData;
import io.mckenz.modemanager.services.ModeService;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Command handler for the mode command
 */
public class ModeCommand implements CommandExecutor, TabCompleter {
    
    private final ModeManager plugin;
    private final ModeService modeService;
    private final List<String> mainCommands = Arrays.asList("survival", "creative", "status", "admin");
    private final List<String> adminCommands = Arrays.asList("list", "check", "force");
    private final List<String> gameModes = Arrays.asList("survival", "creative");
    
    /**
     * Constructor for ModeCommand
     * 
     * @param plugin The plugin instance
     * @param modeService The mode service
     */
    public ModeCommand(ModeManager plugin, ModeService modeService) {
        this.plugin = plugin;
        this.modeService = modeService;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "survival":
                return handleSurvivalCommand(sender);
                
            case "creative":
                return handleCreativeCommand(sender);
                
            case "status":
                return handleStatusCommand(sender);
                
            case "admin":
                return handleAdminCommand(sender, args);
                
            default:
                showHelp(sender);
                return true;
        }
    }
    
    /**
     * Handle the survival command
     * 
     * @param sender The command sender
     * @return True if the command was handled successfully
     */
    private boolean handleSurvivalCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            plugin.getMessageUtil().sendMessage(sender, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("modemanager.use")) {
            plugin.getMessageUtil().sendMessage(player, "no-permission");
            return true;
        }
        
        return modeService.changePlayerMode(player, GameMode.SURVIVAL, "Command");
    }
    
    /**
     * Handle the creative command
     * 
     * @param sender The command sender
     * @return True if the command was handled successfully
     */
    private boolean handleCreativeCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            plugin.getMessageUtil().sendMessage(sender, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("modemanager.use") || !player.hasPermission("modemanager.creative")) {
            plugin.getMessageUtil().sendMessage(player, "no-permission");
            return true;
        }
        
        return modeService.changePlayerMode(player, GameMode.CREATIVE, "Command");
    }
    
    /**
     * Handle the status command
     * 
     * @param sender The command sender
     * @return True if the command was handled successfully
     */
    private boolean handleStatusCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            plugin.getMessageUtil().sendMessage(sender, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("modemanager.use")) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission");
            return true;
        }
        
        PlayerModeData data = plugin.getPlayerDataManager().getPlayerData(player);
        
        plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "status-header");
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("mode", data.getCurrentMode().name());
        plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "status-current-mode", placeholders);
        
        // Show cooldown if applicable
        int cooldown = plugin.getConfig().getInt("mode-switching.cooldown-seconds", 30);
        if (data.isInCooldown(cooldown)) {
            long remainingCooldown = data.getRemainingCooldown(cooldown);
            placeholders = new HashMap<>();
            placeholders.put("time", String.valueOf(remainingCooldown));
            plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "status-cooldown-active", placeholders);
        } else {
            plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "status-cooldown-ready");
        }
        
        // Show recent mode changes
        plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "status-recent-changes");
        List<ModeChangeRecord> history = data.getModeHistory();
        int count = Math.min(5, history.size());
        for (int i = history.size() - 1; i >= history.size() - count; i--) {
            ModeChangeRecord record = history.get(i);
            Map<String, String> recordPlaceholders = new HashMap<>();
            recordPlaceholders.put("timestamp", record.getFormattedTimestamp());
            recordPlaceholders.put("mode", record.getGameMode().name());
            recordPlaceholders.put("reason", record.getReason());
            
            plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "status-history-entry", recordPlaceholders);
        }
        
        return true;
    }
    
    /**
     * Handle the admin command
     * 
     * @param sender The command sender
     * @param args The command arguments
     * @return True if the command was handled successfully
     */
    private boolean handleAdminCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("modemanager.admin")) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission");
            return true;
        }
        
        if (args.length < 2) {
            plugin.getMessageUtil().sendMessage(sender, "admin-usage");
            return true;
        }
        
        String adminSubCommand = args[1].toLowerCase();
        
        switch (adminSubCommand) {
            case "list":
                return handleAdminListCommand(sender);
                
            case "check":
                if (args.length < 3) {
                    plugin.getMessageUtil().sendMessage(sender, "admin-check-usage");
                    return true;
                }
                return handleAdminCheckCommand(sender, args[2]);
                
            case "force":
                if (args.length < 4) {
                    plugin.getMessageUtil().sendMessage(sender, "admin-force-usage");
                    return true;
                }
                String reason = args.length > 4 ? String.join(" ", Arrays.copyOfRange(args, 4, args.length)) : "";
                return handleAdminForceCommand(sender, args[2], args[3], reason);
                
            default:
                plugin.getMessageUtil().sendMessage(sender, "admin-unknown-command");
                return true;
        }
    }
    
    /**
     * Handle the admin list command
     * 
     * @param sender The command sender
     * @return True if the command was handled successfully
     */
    private boolean handleAdminListCommand(CommandSender sender) {
        if (!sender.hasPermission("modemanager.admin.list")) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission");
            return true;
        }
        
        plugin.getMessageUtil().sendMessage(sender, "admin-list-header");
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerModeData data = plugin.getPlayerDataManager().getPlayerData(player);
            GameMode mode = data.getCurrentMode();
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", player.getName());
            placeholders.put("mode", mode.name());
            
            plugin.getMessageUtil().sendMessage(sender, "admin-list-entry", placeholders);
        }
        
        return true;
    }
    
    /**
     * Handle the admin check command
     * 
     * @param sender The command sender
     * @param playerName The player name to check
     * @return True if the command was handled successfully
     */
    private boolean handleAdminCheckCommand(CommandSender sender, String playerName) {
        if (!sender.hasPermission("modemanager.admin.check")) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission");
            return true;
        }
        
        Player target = Bukkit.getPlayer(playerName);
        
        if (target == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", playerName);
            plugin.getMessageUtil().sendMessage(sender, "player-not-found", placeholders);
            return true;
        }
        
        PlayerModeData data = plugin.getPlayerDataManager().getPlayerData(target);
        
        Map<String, String> headerPlaceholders = new HashMap<>();
        headerPlaceholders.put("player", target.getName());
        plugin.getMessageUtil().sendMessage(sender, "admin-check-header", headerPlaceholders);
        
        Map<String, String> modePlaceholders = new HashMap<>();
        modePlaceholders.put("mode", data.getCurrentMode().name());
        plugin.getMessageUtil().sendMessage(sender, "admin-check-current-mode", modePlaceholders);
        
        // Show cooldown if applicable
        int cooldown = plugin.getConfig().getInt("mode-switching.cooldown-seconds", 30);
        if (data.isInCooldown(cooldown)) {
            long remainingCooldown = data.getRemainingCooldown(cooldown);
            Map<String, String> cooldownPlaceholders = new HashMap<>();
            cooldownPlaceholders.put("time", String.valueOf(remainingCooldown));
            plugin.getMessageUtil().sendMessage(sender, "admin-check-cooldown-active", cooldownPlaceholders);
        } else {
            plugin.getMessageUtil().sendMessage(sender, "admin-check-cooldown-ready");
        }
        
        // Show mode history
        plugin.getMessageUtil().sendMessage(sender, "admin-check-history");
        List<ModeChangeRecord> history = data.getModeHistory();
        for (int i = history.size() - 1; i >= 0; i--) {
            ModeChangeRecord record = history.get(i);
            Map<String, String> recordPlaceholders = new HashMap<>();
            recordPlaceholders.put("timestamp", record.getFormattedTimestamp());
            recordPlaceholders.put("mode", record.getGameMode().name());
            recordPlaceholders.put("reason", record.getReason());
            
            plugin.getMessageUtil().sendMessage(sender, "admin-check-history-entry", recordPlaceholders);
        }
        
        return true;
    }
    
    /**
     * Handle the admin force command
     * 
     * @param sender The command sender
     * @param playerName The player name to force
     * @param modeName The mode to force
     * @param reason The reason for forcing the mode
     * @return True if the command was handled successfully
     */
    private boolean handleAdminForceCommand(CommandSender sender, String playerName, String modeName, String reason) {
        if (!sender.hasPermission("modemanager.admin.force")) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission");
            return true;
        }
        
        Player target = Bukkit.getPlayer(playerName);
        
        if (target == null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", playerName);
            plugin.getMessageUtil().sendMessage(sender, "player-not-found", placeholders);
            return true;
        }
        
        GameMode mode;
        try {
            mode = GameMode.valueOf(modeName.toUpperCase());
            
            // Only allow SURVIVAL and CREATIVE modes
            if (mode != GameMode.SURVIVAL && mode != GameMode.CREATIVE) {
                plugin.getMessageUtil().sendMessage(sender, "invalid-mode");
                return true;
            }
        } catch (IllegalArgumentException e) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("mode", modeName);
            plugin.getMessageUtil().sendMessage(sender, "invalid-mode", placeholders);
            return true;
        }
        
        String adminName = sender instanceof Player ? ((Player) sender).getName() : "Console";
        boolean success = modeService.forcePlayerMode(target, mode, reason, adminName);
        
        if (success) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", target.getName());
            placeholders.put("mode", mode.name());
            plugin.getMessageUtil().sendMessage(sender, "admin-force-success", placeholders);
        } else {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", target.getName());
            placeholders.put("mode", mode.name());
            plugin.getMessageUtil().sendMessage(sender, "admin-force-failed", placeholders);
        }
        
        return true;
    }
    
    /**
     * Show the help message
     * 
     * @param sender The command sender
     */
    private void showHelp(CommandSender sender) {
        plugin.getMessageUtil().sendMessage(sender, "help-header");
        
        if (sender.hasPermission("modemanager.use")) {
            plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-survival");
            
            if (sender.hasPermission("modemanager.creative")) {
                plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-creative");
            }
            
            plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-status");
        }
        
        if (sender.hasPermission("modemanager.admin")) {
            plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-admin-list");
            plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-admin-check");
            
            if (sender.hasPermission("modemanager.admin.force")) {
                plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-admin-force");
            }
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - main commands
            String partialCommand = args[0].toLowerCase();
            
            for (String cmd : mainCommands) {
                if (cmd.startsWith(partialCommand)) {
                    // Check permissions
                    if (cmd.equals("admin") && !sender.hasPermission("modemanager.admin")) {
                        continue;
                    }
                    
                    if (cmd.equals("creative") && !sender.hasPermission("modemanager.creative")) {
                        continue;
                    }
                    
                    if ((cmd.equals("survival") || cmd.equals("creative") || cmd.equals("status")) && 
                        !sender.hasPermission("modemanager.use")) {
                        continue;
                    }
                    
                    completions.add(cmd);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            // Second argument for admin command
            String partialCommand = args[1].toLowerCase();
            
            for (String cmd : adminCommands) {
                if (cmd.startsWith(partialCommand)) {
                    // Check permissions
                    if (cmd.equals("list") && !sender.hasPermission("modemanager.admin.list")) {
                        continue;
                    }
                    
                    if (cmd.equals("check") && !sender.hasPermission("modemanager.admin.check")) {
                        continue;
                    }
                    
                    if (cmd.equals("force") && !sender.hasPermission("modemanager.admin.force")) {
                        continue;
                    }
                    
                    completions.add(cmd);
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("admin") && 
                  (args[1].equalsIgnoreCase("check") || args[1].equalsIgnoreCase("force"))) {
            // Third argument for admin check/force command - player names
            String partialName = args[2].toLowerCase();
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(partialName)) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("force")) {
            // Fourth argument for admin force command - game modes
            String partialMode = args[3].toLowerCase();
            
            for (String mode : gameModes) {
                if (mode.startsWith(partialMode)) {
                    completions.add(mode);
                }
            }
        }
        
        return completions;
    }
} 