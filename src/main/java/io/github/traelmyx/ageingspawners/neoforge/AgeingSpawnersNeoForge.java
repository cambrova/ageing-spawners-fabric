package io.github.traelmyx.ageingspawners.neoforge;

//? if neoforge {
import io.github.traelmyx.ageingspawners.AgeingSpawners;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;

/** Only built so the tests can run on both loaders; on NeoForge use the original Ageing Spawners. */
@Mod(AgeingSpawners.MOD_ID)
public class AgeingSpawnersNeoForge {
	public AgeingSpawnersNeoForge() {
		AgeingSpawners.loadConfig(FMLPaths.CONFIGDIR.get());
	}
}
//?}
