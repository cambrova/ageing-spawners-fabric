package io.github.traelmyx.ageingspawners.test;

import io.github.traelmyx.ageingspawners.AgeingSpawners;
import io.github.traelmyx.ageingspawners.SpawnerAge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Properties;

/**
 * A spawner set to spawn every tick, one entity at a time, with a player standing next to it. Armor stands are listed
 * with a limit of 3 and must break their spawner after 3 spawns; snow golems are not listed and their spawner must keep
 * going. Both have no spawn rules, so light and ground don't matter.
 */
public final class SpawnerTests {
	private static final BlockPos SPAWNER = new BlockPos(3, 2, 3);

	private SpawnerTests() {
	}

	static {
		Properties props = new Properties();
		props.setProperty("spawnerMode", "WHITELIST");
		props.setProperty("whitelist", "minecraft:armor_stand;3");
		AgeingSpawners.load(props);
	}

	private static ServerPlayer player(GameTestHelper helper) {
		var server = helper.getLevel().getServer();
		var cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "test-spawner"), false);
		var player = new ServerPlayer(server, helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
		var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
		new io.netty.channel.embedded.EmbeddedChannel(connection);
		server.getPlayerList().placeNewPlayer(connection, player, cookie);
		player.setGameMode(GameType.CREATIVE);
		Vec3 at = helper.absoluteVec(new Vec3(1.5, 1, 1.5));
		player.setPos(at.x, at.y, at.z);
		return player;
	}

	/** Places a spawner for this entity type that tries to spawn one every tick. */
	private static void spawner(GameTestHelper helper, String id) {
		for (int x = 0; x < 8; x++) for (int z = 0; z < 8; z++) helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
		helper.setBlock(SPAWNER, Blocks.SPAWNER);
		var level = helper.getLevel();
		BlockPos pos = helper.absolutePos(SPAWNER);
		SpawnerBlockEntity spawner = (SpawnerBlockEntity) level.getBlockEntity(pos);
		//? if >=1.21.11 {
		/*EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.withDefaultNamespace(id));
		*///?} else
		EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(net.minecraft.resources.ResourceLocation.withDefaultNamespace(id));
		spawner.getSpawner().setEntityId(type, level, level.getRandom(), pos);
		CompoundTag tag = spawner.saveWithFullMetadata(level.registryAccess());
		tag.putShort("Delay", (short) 1);
		tag.putShort("MinSpawnDelay", (short) 1);
		tag.putShort("MaxSpawnDelay", (short) 2);
		tag.putShort("SpawnCount", (short) 1);
		tag.putShort("MaxNearbyEntities", (short) 100);
		tag.putShort("RequiredPlayerRange", (short) 32);
		level.setBlockEntity(BlockEntity.loadStatic(pos, level.getBlockState(pos), tag, level.registryAccess()));
	}

	/** Just this test's 8x8x8 area; tests run side by side. */
	private static AABB area(GameTestHelper helper) {
		return new AABB(helper.absoluteVec(Vec3.ZERO), helper.absoluteVec(new Vec3(8, 8, 8)));
	}

	private static int count(GameTestHelper helper, String id) {
		int n = 0;
		for (Entity e : helper.getLevel().getEntities((Entity) null, area(helper), e -> true))
			if (BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath().equals(id)) n++;
		return n;
	}

	private static void cleanup(GameTestHelper helper, ServerPlayer player, String id) {
		for (Entity e : helper.getLevel().getEntities((Entity) null, area(helper), e -> true))
			if (BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getPath().equals(id)) e.discard();
		helper.getLevel().getServer().getPlayerList().remove(player);
	}

	/** A listed mob breaks its spawner once the spawner has spawned 3 of them. */
	public static void listedMobAgesSpawner(GameTestHelper helper) {
		spawner(helper, "armor_stand");
		ServerPlayer player = player(helper);
		helper.runAfterDelay(60, () -> {
			int spawned = count(helper, "armor_stand");
			boolean gone = !helper.getLevel().getBlockState(helper.absolutePos(SPAWNER)).is(Blocks.SPAWNER);
			System.out.printf("[ageingspawners] listed mob: %d spawned, spawner %s%n", spawned, gone ? "broke" : "still there");
			cleanup(helper, player, "armor_stand");
			helper.assertTrue(gone && spawned == 3, "spawned " + spawned + ", spawner broken: " + gone + " (expected 3, broken)");
			helper.succeed();
		});
	}

	/** A mob that isn't listed leaves its spawner alone, however many it spawns. */
	public static void unlistedMobLeavesSpawner(GameTestHelper helper) {
		spawner(helper, "snow_golem");
		ServerPlayer player = player(helper);
		helper.runAfterDelay(60, () -> {
			int spawned = count(helper, "snow_golem");
			boolean there = helper.getLevel().getBlockState(helper.absolutePos(SPAWNER)).is(Blocks.SPAWNER);
			System.out.printf("[ageingspawners] unlisted mob: %d spawned, spawner %s%n", spawned, there ? "still there" : "broke");
			cleanup(helper, player, "snow_golem");
			helper.assertTrue(there && spawned > 3, "spawned " + spawned + ", spawner still there: " + there);
			helper.succeed();
		});
	}

	/** The count is saved with the spawner, so a reload doesn't reset it. */
	public static void countSurvivesReload(GameTestHelper helper) {
		spawner(helper, "armor_stand");
		var level = helper.getLevel();
		BlockPos pos = helper.absolutePos(SPAWNER);
		var spawner = (SpawnerBlockEntity) level.getBlockEntity(pos);
		((SpawnerAge) spawner.getSpawner()).ageingspawners$setSpawned(2);
		((SpawnerAge) spawner.getSpawner()).ageingspawners$setPlayerPlaced(true);
		CompoundTag tag = spawner.saveWithFullMetadata(level.registryAccess());
		var reloaded = (SpawnerBlockEntity) BlockEntity.loadStatic(pos, level.getBlockState(pos), tag, level.registryAccess());
		var age = (SpawnerAge) reloaded.getSpawner();
		level.removeBlock(pos, false);
		helper.assertTrue(age.ageingspawners$spawned() == 2 && age.ageingspawners$playerPlaced(), "after reload: " + age.ageingspawners$spawned() + " spawned, player placed " + age.ageingspawners$playerPlaced());
		helper.succeed();
	}
}
