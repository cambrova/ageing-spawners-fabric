package io.github.traelmyx.ageingspawners.test.fabric;

//? if fabric {
/*import io.github.traelmyx.ageingspawners.test.SpawnerTests;
import net.minecraft.gametest.framework.GameTestHelper;
//? if >=1.21.5 {
import net.fabricmc.fabric.api.gametest.v1.GameTest;
//?} else {
/^import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
^///?}

//? if >=1.21.5 {
public class FabricGameTests {
	private static final String EMPTY = "ageingspawners_gametest:empty";

	@GameTest(structure = EMPTY, maxTicks = 120)
	public void listedMobAgesSpawner(GameTestHelper helper) {
		SpawnerTests.listedMobAgesSpawner(helper);
	}

	@GameTest(structure = EMPTY, maxTicks = 120)
	public void unlistedMobLeavesSpawner(GameTestHelper helper) {
		SpawnerTests.unlistedMobLeavesSpawner(helper);
	}

	@GameTest(structure = EMPTY, maxTicks = 120)
	public void countSurvivesReload(GameTestHelper helper) {
		SpawnerTests.countSurvivesReload(helper);
	}
}
//?} else {
/^public class FabricGameTests implements FabricGameTest {
	private static final String EMPTY = "ageingspawners_gametest:empty";

	@GameTest(template = EMPTY, timeoutTicks = 120)
	public void listedMobAgesSpawner(GameTestHelper helper) {
		SpawnerTests.listedMobAgesSpawner(helper);
	}

	@GameTest(template = EMPTY, timeoutTicks = 120)
	public void unlistedMobLeavesSpawner(GameTestHelper helper) {
		SpawnerTests.unlistedMobLeavesSpawner(helper);
	}

	@GameTest(template = EMPTY, timeoutTicks = 120)
	public void countSurvivesReload(GameTestHelper helper) {
		SpawnerTests.countSurvivesReload(helper);
	}
}
^///?}
*///?}
