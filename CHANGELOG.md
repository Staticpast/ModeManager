# Changelog

All notable changes to the ModeManager plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.1] - 2024-07-10

### Fixed
- Fixed an issue where players with no permissions, who are in survival mode by default, do not see their UI or game mode change when an admin or the console sets them to creative mode
- Fixed permission handling in the `forcePlayerMode` method to temporarily grant operator status when changing game modes
- Added explicit permission check for creative mode in the `changePlayerMode` method

### Changed
- Updated README with accurate API documentation
- Added SpigotMC resource ID (122976) to the config's update-checker section to enable automatic update notifications
- Improved SpigotMC resource links in documentation

## [1.0.0] - 2024-07-01

### Added
- Initial release of ModeManager
- Seamless switching between survival and creative modes
- Separate inventories for each gamemode
- Prevention of item transfers between gamemodes
- Protection of creative-built blocks from survival mining
- Blocking of item dropping in creative mode
- Admin tools to monitor and manage player mode usage