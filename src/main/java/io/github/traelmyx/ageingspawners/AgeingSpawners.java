package io.github.traelmyx.ageingspawners;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Spawners break after spawning a set number of mobs. Port of Ageing Spawners by Mrbysco (MIT,
 * https://github.com/Mrbysco/AgeingSpawners), which only exists for NeoForge, to Fabric. The count is kept on the spawner
 * itself, so it survives restarts and disappears with the spawner.
 */
public final class AgeingSpawners {
	public static final String MOD_ID = "ageingspawners";
	public static final Logger LOGGER = LoggerFactory.getLogger("Ageing Spawners");

	public enum Mode { WHITELIST, BLACKLIST }

	public static boolean enabled = true;
	public static Mode mode = Mode.BLACKLIST;
	public static boolean playerPlacedOnly = false;
	public static int whitelistMaxSpawnCount = 20;
	public static int blacklistMaxSpawnCount = 20;
	/** Mob id -> how many it may spawn (0 = use whitelistMaxSpawnCount). */
	public static final Map<String, Integer> whitelist = new HashMap<>();
	public static final List<String> blacklist = new ArrayList<>();

	private AgeingSpawners() {
	}

	/** How many spawns of this mob the spawner survives, or -1 if this mob doesn't age it. */
	public static int maxSpawns(Entity entity) {
		String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
		if (mode == Mode.BLACKLIST)
			return blacklist.contains(id) ? -1 : blacklistMaxSpawnCount;
		Integer max = whitelist.get(id);
		return max == null ? -1 : max > 0 ? max : whitelistMaxSpawnCount;
	}

	public static void loadConfig(Path configDir) {
		Path file = configDir.resolve(MOD_ID + ".properties");
		Properties props = new Properties();
		if (Files.exists(file)) {
			try (Reader reader = Files.newBufferedReader(file)) {
				props.load(reader);
			} catch (IOException e) {
				LOGGER.warn("Could not read {}, using defaults", file, e);
			}
		}
		load(props);
		try {
			Files.createDirectories(configDir);
			try (Writer writer = Files.newBufferedWriter(file)) {
				props.store(writer, "Ageing Spawners. spawnerMode: BLACKLIST (every mob ages spawners except the blacklist) or WHITELIST (only listed mobs do).\n"
					+ "whitelist: comma-separated 'modid:entity' or 'modid:entity;count', e.g. minecraft:pig;5,minecraft:zombie\n"
					+ "blacklist: comma-separated 'modid:entity'. playerPlacedOnly: only spawners placed by a player age.");
			}
		} catch (IOException e) {
			LOGGER.warn("Could not write {}", file, e);
		}
	}

	public static void load(Properties props) {
		enabled = Boolean.parseBoolean(props.getProperty("enabled", "true").trim());
		try {
			mode = Mode.valueOf(props.getProperty("spawnerMode", "BLACKLIST").trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			mode = Mode.BLACKLIST;
		}
		playerPlacedOnly = Boolean.parseBoolean(props.getProperty("playerPlacedOnly", "false").trim());
		whitelistMaxSpawnCount = positive(props.getProperty("whitelistMaxSpawnCount", "20"), 20);
		blacklistMaxSpawnCount = positive(props.getProperty("blacklistMaxSpawnCount", "20"), 20);
		whitelist.clear();
		for (String entry : props.getProperty("whitelist", "").split(",")) {
			entry = entry.trim();
			if (entry.isEmpty()) continue;
			String[] parts = entry.split(";");
			whitelist.put(parts[0].trim(), parts.length > 1 ? positive(parts[1], 0) : 0);
		}
		blacklist.clear();
		for (String entry : props.getProperty("blacklist", "").split(","))
			if (!entry.trim().isEmpty()) blacklist.add(entry.trim());
		props.setProperty("enabled", Boolean.toString(enabled));
		props.setProperty("spawnerMode", mode.name());
		props.setProperty("playerPlacedOnly", Boolean.toString(playerPlacedOnly));
		props.setProperty("whitelistMaxSpawnCount", Integer.toString(whitelistMaxSpawnCount));
		props.setProperty("blacklistMaxSpawnCount", Integer.toString(blacklistMaxSpawnCount));
		props.setProperty("whitelist", props.getProperty("whitelist", ""));
		props.setProperty("blacklist", props.getProperty("blacklist", ""));
	}

	private static int positive(String value, int def) {
		try {
			return Math.max(0, Integer.parseInt(value.trim()));
		} catch (NumberFormatException e) {
			return def;
		}
	}
}
