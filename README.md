# ModeManager

A Minecraft Spigot plugin that allows players to switch between survival and creative modes while maintaining separate inventories and preventing cross-mode item transfers.

[![SpigotMC](https://img.shields.io/badge/SpigotMC-ModeManager-orange)]()
[![Donate](https://img.shields.io/badge/Donate-PayPal-blue.svg)](https://www.paypal.com/paypalme/mckenzio)

## Features

* 🔄 Seamless switching between survival and creative modes
* 🎒 Separate inventories for each gamemode
* 🛡️ Prevents item transfers between gamemodes
* 🚫 Blocks dropping items in creative mode
* 🏗️ Creative-built blocks are protected from survival mining
* ⚙️ Configurable permissions for mode switching
* 📢 Customizable messages for all plugin actions
* 📋 Comprehensive logging of mode changes and transfer attempts
* 👮 Admin tools to monitor and manage player mode usage

## Installation

1. Download the latest release from [Spigot]() or [GitHub Releases](https://github.com/McKenzieJDan/ModeManager/releases)
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in the `config.yml` file

## Usage

Players with appropriate permissions can switch modes using simple commands. The plugin handles all inventory management and protection automatically.

### Commands

* `/mode survival` - Switch to survival mode
* `/mode creative` - Switch to creative mode
* `/mode status` - Check your current mode and statistics
* `/mode admin list` - List all players and their current modes (admin only)
* `/mode admin check <player>` - Check a specific player's mode history (admin only)

### Permissions

* `modemanager.use` - Permission to use mode switching
* `modemanager.creative` - Permission to access creative mode
* `modemanager.admin` - Admin permissions for monitoring and management
* `modemanager.admin.list` - Permission to list all players and their modes
* `modemanager.admin.check` - Permission to check a player's mode history
* `modemanager.reload` - Permission to reload the plugin configuration
* `modemanager.debug` - Permission to toggle debug mode
* `modemanager.update` - Permission to receive update notifications

## Configuration

The plugin's configuration file (`config.yml`) is organized into logical sections:

```yaml
# Mode switching settings
mode-switching:
  cooldown-seconds: 30
  broadcast-changes: false
  default-mode: SURVIVAL
  
# Protection settings
protection:
  track-creative-blocks: true
  prevent-creative-drops: true
  prevent-ender-chest-transfers: true
  prevent-creative-container-placement: true
  prevent-creative-container-taking: true
  
# Inventory management
inventories:
  save-armor-contents: true
  save-offhand-items: true
  separate-ender-chest: true
  clear-on-creative: true
```

For detailed configuration options, see the comments in the generated config.yml file.

## Developer API

ModeManager provides an API for other plugins to interact with its functionality. To use the API, you need to get the API instance from the services manager:

```java
// Get the API from the services manager
RegisteredServiceProvider<ModeManagerAPI> provider = 
    Bukkit.getServicesManager().getRegistration(ModeManagerAPI.class);
    
if (provider != null) {
    ModeManagerAPI api = provider.getProvider();
    
    // Now you can use the API methods
    if (api.isPluginEnabled()) {
        // Check a player's mode
        GameMode mode = api.getPlayerMode(player);
        
        // Change a player's mode
        api.changePlayerMode(player, GameMode.CREATIVE, "API call from MyPlugin");
        
        // Check if a block was placed in creative mode
        boolean isCreative = api.isCreativeBlock(location);
        
        // Get a player's mode history
        List<ModeChangeRecord> history = api.getPlayerModeHistory(player);
    }
}
```

### Available API Methods

* `isPluginEnabled()` - Check if the plugin is enabled
* `getPlayerMode(Player)` - Get a player's current mode
* `changePlayerMode(Player, GameMode, String)` - Change a player's mode
* `isPlayerInCooldown(Player)` - Check if a player is in cooldown
* `getPlayerRemainingCooldown(Player)` - Get a player's remaining cooldown time
* `isCreativeBlock(Location)` - Check if a block was placed in creative mode
* `getCreativeBlockPlacer(Location)` - Get who placed a creative block
* `getPlayerModeHistory(Player)` - Get a player's mode history

## Requirements

- Spigot/Paper 1.21.4
- Java 21+

## Support

If you find this plugin helpful, consider [buying me a coffee](https://www.paypal.com/paypalme/mckenzio) ☕

## License

[MIT License](LICENSE)

Made with ❤️ by [McKenzieJDan](https://github.com/McKenzieJDan)