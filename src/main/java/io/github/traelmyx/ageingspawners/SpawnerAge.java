package io.github.traelmyx.ageingspawners;

/** Added to every spawner: how many mobs it has spawned so far, and whether a player placed it. */
public interface SpawnerAge {
	int ageingspawners$spawned();

	void ageingspawners$setSpawned(int spawned);

	boolean ageingspawners$playerPlaced();

	void ageingspawners$setPlayerPlaced(boolean playerPlaced);
}
