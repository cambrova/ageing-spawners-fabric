# Ageing Spawners for Fabric

A Fabric port of [Ageing Spawners](https://github.com/Mrbysco/AgeingSpawners) by Mrbysco, which only exists for NeoForge. For Minecraft 1.21.1, 1.21.11, 26.2 and 26.3.

Spawners break after spawning a set number of mobs, so a spawner farm doesn't run forever.

- `config/ageingspawners.properties`:
  - `spawnerMode`: `BLACKLIST` (every mob ages spawners, except those on the blacklist) or `WHITELIST` (only listed mobs do). Default `BLACKLIST`.
  - `blacklistMaxSpawnCount` / `whitelistMaxSpawnCount`: how many spawns a spawner survives. Default 20.
  - `whitelist`: e.g. `minecraft:zombie;50,minecraft:pig` (a count after `;` overrides the default for that mob).
  - `blacklist`: e.g. `minecraft:blaze,minecraft:silverfish`.
  - `playerPlacedOnly`: only spawners placed by a player age.
  - `enabled`.
- Only mobs that actually make it into the world count, and only spawner blocks age (not spawner minecarts).
- The count is stored on the spawner, so it survives restarts and goes away with the spawner.
- Differences from the original: the default mode is `BLACKLIST`, so spawners age out of the box (the original defaults to an empty whitelist, which ages nothing until configured); the on/off switch is a config option instead of a game rule.

Tests: GameTests run a spawner that spawns every tick next to a player: a listed mob must break it after exactly 3 spawns, an unlisted one must leave it standing, and the count must survive saving and loading the spawner. They run on all four Minecraft versions.

MIT, like the original.
