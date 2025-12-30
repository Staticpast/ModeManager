# ModeManager

A Minecraft Spigot plugin that lets players switch between Survival and Creative modes with separate inventories. Items, blocks, and drops are kept mode-specific.

[![Java](https://img.shields.io/badge/Java-21-red.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-green.svg)](https://www.minecraft.net/)
[![SpigotMC](https://img.shields.io/badge/SpigotMC-ModeManager-orange)](https://www.spigotmc.org/resources/modemanager.122976/)
[![Donate](https://img.shields.io/badge/Donate-PayPal-blue.svg)](https://www.paypal.com/paypalme/mckenzio)

## Features

* 🔄 Switch between Survival and Creative modes with `/mode`
* 🎒 Separate inventories for each mode
* 🛡️ Items can't move between modes
* 🏗️ Creative-built blocks are mine-protected in Survival
* 🚫 Creative mode drops no items on mode switch
* 👮 Admin tools to monitor and manage player mode usage

## Installation

1. Download the latest release from [Spigot](https://www.spigotmc.org/resources/modemanager.122976/) or [GitHub Releases](https://github.com/McKenzieJDan/ModeManager/releases)
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in the `config.yml` file

## Usage

ModeManager allows players to switch between game modes with simple commands. The plugin automatically manages inventories and block protection when switching modes.

### Commands

#### Player Commands
* `/mode survival` - Switch to Survival mode
* `/mode creative` - Switch to Creative mode
* `/mode status` - View your current mode and playtime statistics

#### Admin Commands
* `/mode admin list` - List all players and their current modes
* `/mode admin check <player>` - View a player's mode history
* `/mode admin force <player> <mode> [reason]` - Force a player to a specific mode
* `/mode reload` - Reload plugin configuration
* `/mode debug` - Toggle debug mode
* `/mode update` - Receive update notifications

### Permissions

#### Core Permissions
* `modemanager.use` - Use mode switching (default: all players)
* `modemanager.creative` - Access Creative mode (default: ops only)

#### Admin Permissions
* `modemanager.admin` - Access all admin commands (default: ops only)
* `modemanager.admin.list` - View all players' modes (default: ops only)
* `modemanager.admin.check` - Check a player's history (default: ops only)
* `modemanager.admin.force` - Force mode changes (default: ops only)
* `modemanager.reload` - Reload configuration (default: ops only)
* `modemanager.debug` - Toggle debug mode (default: ops only)
* `modemanager.update` - Receive update notifications (default: ops only)
* `modemanager.bypass.itemrestrictions` - Bypass item restrictions in Creative mode (default: ops only)
* `modemanager.bypass.containerplacement` - Bypass container placement restrictions (default: ops only)
* `modemanager.bypass.containerinteraction` - Bypass container interaction restrictions in Creative mode (default: ops only)
* `modemanager.bypass.mobspawning` - Bypass mob spawning restrictions in Creative mode (default: ops only)

## Configuration

The plugin's configuration file (`config.yml`) is organized into logical sections:

```yaml
# General settings
enabled: true

# Mode switching settings
mode-switching:
  cooldown-seconds: 30
  broadcast-changes: false
  default-mode: SURVIVAL
  
# Protection settings
protection:
  track-creative-blocks: true
  track-creative-item-frames: true
  prevent-creative-drops: true
  prevent-ender-chest-transfers: true
  prevent-creative-container-placement: true
  prevent-creative-container-taking: true
  prevent-creative-container-blocks: true
  prevent-creative-mob-spawning: true
  
  # Restricted items in creative mode
  restrict-creative-items:
    enabled: true
    restricted-items:
      - LAVA_BUCKET
      - WATER_BUCKET
      - BUCKET
      - FLINT_AND_STEEL
      - TNT
      # And more...
  
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
* `registerEvents(Plugin, Listener)` - Register a listener for plugin events
* `getPlayerMode(Player)` - Get a player's current mode
* `getPlayerMode(UUID)` - Get a player's current mode by UUID
* `changePlayerMode(Player, GameMode, String)` - Change a player's mode
* `isPlayerInCooldown(Player)` - Check if a player is in cooldown
* `getPlayerRemainingCooldown(Player)` - Get a player's remaining cooldown time
* `isCreativeBlock(Location)` - Check if a block was placed in creative mode
* `getCreativeBlockPlacer(Location)` - Get who placed a creative block
* `isCreativeItemFrame(ItemFrame)` - Check if an item frame was placed in creative mode
* `getCreativeItemFramePlacer(ItemFrame)` - Get who placed an item in an item frame in creative mode
* `getPlayerModeHistory(Player)` - Get a player's mode history


## Requirements

- Spigot/Paper 1.21.6+
- Java 21+

## Used By

[SuegoFaults](https://suegofaults.com) - A curated adult Minecraft community where ModeManager ensures creative freedom and survival integrity stay perfectly balanced.


## Support

If you find this plugin helpful, consider [buying me a coffee](https://www.paypal.com/paypalme/mckenzio) ☕

## License

[MIT License](LICENSE)

Made with ❤️ by [McKenzieJDan](https://github.com/McKenzieJDan)
