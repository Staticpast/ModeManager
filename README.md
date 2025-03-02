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

1. Download the latest release from [Spigot]() or [GitHub Releases](https://github.com/McKenzieJDan/WeatherVoting/releases)
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in the `config.yml` file

## Usage

Players with appropriate permissions can switch modes using simple commands. The plugin handles all inventory management and protection automatically.f

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

## Configuration

The plugin's configuration file (`config.yml`) is organized into logical sections:

```yaml
# Mode switching settings
mode-switching:
  cooldown-seconds: 30
  broadcast-changes: false
  
# Protection settings
protection:
  track-creative-blocks: true
  prevent-creative-drops: true
  prevent-ender-chest-transfers: true
  
# Inventory management
inventories:
  save-armor-contents: true
  save-offhand-items: true
  separate-ender-chest: true
```

For detailed configuration options, see the comments in the generated config.yml file.

## Requirements

- Spigot/Paper 1.21.4
- Java 21+

## Support

If you find this plugin helpful, consider [buying me a coffee](https://www.paypal.com/paypalme/mckenzio) ☕

## License

[MIT License](LICENSE)

Made with ❤️ by [McKenzieJDan](https://github.com/McKenzieJDan)