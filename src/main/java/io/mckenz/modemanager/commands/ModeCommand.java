package io.mckenz.modemanager.commands;

import io.mckenz.modemanager.ModeManager;
import io.mckenz.modemanager.data.ModeChangeRecord;
import io.mckenz.modemanager.data.PlayerModeData;
import io.mckenz.modemanager.services.ModeService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
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
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
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
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("modemanager.use")) {
            plugin.getMessageUtil().sendMessage(player, "no-permission");
            return true;
        }
        
        PlayerModeData data = plugin.getPlayerDataManager().getPlayerData(player);
        
        player.sendMessage(ChatColor.GOLD + "=== Mode Status ===");
        player.sendMessage(ChatColor.GRAY + "Current mode: " + ChatColor.YELLOW + data.getCurrentMode().name());
        
        // Show cooldown if applicable
        int cooldown = plugin.getConfig().getInt("mode-switching.cooldown-seconds", 30);
        if (data.isInCooldown(cooldown)) {
            long remainingCooldown = data.getRemainingCooldown(cooldown);
            player.sendMessage(ChatColor.GRAY + "Cooldown: " + ChatColor.WHITE + remainingCooldown + " seconds");
        } else {
            player.sendMessage(ChatColor.GRAY + "Cooldown: " + ChatColor.YELLOW + "Ready");
        }
        
        // Show recent mode changes
        player.sendMessage(ChatColor.GRAY + "Recent mode changes:");
        List<ModeChangeRecord> history = data.getModeHistory();
        int count = Math.min(5, history.size());
        for (int i = history.size() - 1; i >= history.size() - count; i--) {
            ModeChangeRecord record = history.get(i);
            player.sendMessage(ChatColor.GRAY + "- " + record.getFormattedTimestamp() + ": " + 
                              ChatColor.YELLOW + record.getGameMode().name() + 
                              ChatColor.GRAY + " (" + record.getReason() + ")");
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
            sender.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.WHITE + "/mode admin [list|check <player>|force <player> <mode> [reason]]");
            return true;
        }
        
        String adminSubCommand = args[1].toLowerCase();
        
        switch (adminSubCommand) {
            case "list":
                return handleAdminListCommand(sender);
                
            case "check":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.WHITE + "/mode admin check <player>");
                    return true;
                }
                return handleAdminCheckCommand(sender, args[2]);
                
            case "force":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.WHITE + "/mode admin force <player> <mode> [reason]");
                    return true;
                }
                String reason = args.length > 4 ? String.join(" ", Arrays.copyOfRange(args, 4, args.length)) : "";
                return handleAdminForceCommand(sender, args[2], args[3], reason);
                
            default:
                sender.sendMessage(ChatColor.GRAY + "Unknown admin command. Use " + ChatColor.WHITE + "/mode admin [list|check <player>|force <player> <mode> [reason]]");
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
        
        sender.sendMessage(ChatColor.GOLD + "=== Player Modes ===");
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerModeData data = plugin.getPlayerDataManager().getPlayerData(player);
            GameMode mode = data.getCurrentMode();
            
            sender.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.GRAY + ": " + ChatColor.YELLOW + mode.name());
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
            sender.sendMessage(ChatColor.GRAY + "Player not found: " + ChatColor.YELLOW + playerName);
            return true;
        }
        
        PlayerModeData data = plugin.getPlayerDataManager().getPlayerData(target);
        
        sender.sendMessage(ChatColor.GOLD + "=== Mode History for " + target.getName() + " ===");
        sender.sendMessage(ChatColor.GRAY + "Current mode: " + ChatColor.YELLOW + data.getCurrentMode().name());
        
        // Show cooldown if applicable
        int cooldown = plugin.getConfig().getInt("mode-switching.cooldown-seconds", 30);
        if (data.isInCooldown(cooldown)) {
            long remainingCooldown = data.getRemainingCooldown(cooldown);
            sender.sendMessage(ChatColor.GRAY + "Cooldown: " + ChatColor.WHITE + remainingCooldown + " seconds");
        } else {
            sender.sendMessage(ChatColor.GRAY + "Cooldown: " + ChatColor.YELLOW + "Ready");
        }
        
        // Show mode history
        sender.sendMessage(ChatColor.GRAY + "Mode history:");
        List<ModeChangeRecord> history = data.getModeHistory();
        for (int i = history.size() - 1; i >= 0; i--) {
            ModeChangeRecord record = history.get(i);
            sender.sendMessage(ChatColor.GRAY + "- " + record.getFormattedTimestamp() + ": " + 
                              ChatColor.YELLOW + record.getGameMode().name() + 
                              ChatColor.GRAY + " (" + record.getReason() + ")");
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
            sender.sendMessage(ChatColor.GRAY + "Player not found: " + ChatColor.YELLOW + playerName);
            return true;
        }
        
        GameMode mode;
        try {
            mode = GameMode.valueOf(modeName.toUpperCase());
            
            // Only allow SURVIVAL and CREATIVE modes
            if (mode != GameMode.SURVIVAL && mode != GameMode.CREATIVE) {
                sender.sendMessage(ChatColor.GRAY + "Only SURVIVAL and CREATIVE modes are supported.");
                return true;
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.GRAY + "Invalid game mode: " + ChatColor.YELLOW + modeName);
            return true;
        }
        
        String adminName = sender instanceof Player ? ((Player) sender).getName() : "Console";
        boolean success = modeService.forcePlayerMode(target, mode, reason, adminName);
        
        if (success) {
            sender.sendMessage(ChatColor.GRAY + "Forced " + ChatColor.YELLOW + target.getName() + ChatColor.GRAY + " into " + ChatColor.YELLOW + mode.name() + ChatColor.GRAY + " mode.");
        } else {
            sender.sendMessage(ChatColor.GRAY + "Failed to force " + ChatColor.YELLOW + target.getName() + ChatColor.GRAY + " into " + ChatColor.YELLOW + mode.name() + ChatColor.GRAY + " mode.");
        }
        
        return true;
    }
    
    /**
     * Show the help message
     * 
     * @param sender The command sender
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== ModeManager Commands ===");
        
        if (sender.hasPermission("modemanager.use")) {
            sender.sendMessage(ChatColor.WHITE + "/mode survival " + ChatColor.GRAY + "- Switch to survival mode");
            
            if (sender.hasPermission("modemanager.creative")) {
                sender.sendMessage(ChatColor.WHITE + "/mode creative " + ChatColor.GRAY + "- Switch to creative mode");
            }
            
            sender.sendMessage(ChatColor.WHITE + "/mode status " + ChatColor.GRAY + "- Check your current mode and statistics");
        }
        
        if (sender.hasPermission("modemanager.admin")) {
            sender.sendMessage(ChatColor.WHITE + "/mode admin list " + ChatColor.GRAY + "- List all players and their current modes");
            sender.sendMessage(ChatColor.WHITE + "/mode admin check <player> " + ChatColor.GRAY + "- Check a specific player's mode history");
            
            if (sender.hasPermission("modemanager.admin.force")) {
                sender.sendMessage(ChatColor.WHITE + "/mode admin force <player> <mode> [reason] " + ChatColor.GRAY + "- Force a player into a specific mode");
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