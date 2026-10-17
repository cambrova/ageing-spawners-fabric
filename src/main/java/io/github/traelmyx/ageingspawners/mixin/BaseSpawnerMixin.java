package io.github.traelmyx.ageingspawners.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.traelmyx.ageingspawners.AgeingSpawners;
import io.github.traelmyx.ageingspawners.SpawnerAge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseSpawner.class)
public abstract class BaseSpawnerMixin implements SpawnerAge {
	@Unique
	private int ageingspawners$spawned;
	@Unique
	private boolean ageingspawners$playerPlaced;

	@Override
	public int ageingspawners$spawned() {
		return ageingspawners$spawned;
	}

	@Override
	public void ageingspawners$setSpawned(int spawned) {
		ageingspawners$spawned = spawned;
	}

	@Override
	public boolean ageingspawners$playerPlaced() {
		return ageingspawners$playerPlaced;
	}

	@Override
	public void ageingspawners$setPlayerPlaced(boolean playerPlaced) {
		ageingspawners$playerPlaced = playerPlaced;
	}

	/** Counts each mob a spawner block really adds to the world, and breaks the spawner when it reaches its limit. */
	@WrapOperation(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;tryAddFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)Z"))
	private boolean ageingspawners$count(ServerLevel level, Entity entity, Operation<Boolean> original, @Local(argsOnly = true) BlockPos pos) {
		boolean added = original.call(level, entity);
		if (!added || !AgeingSpawners.enabled || (AgeingSpawners.playerPlacedOnly && !ageingspawners$playerPlaced))
			return added;
		// only spawner blocks age, not spawner minecarts
		if (!(level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) || spawner.getSpawner() != (Object) this)
			return added;
		int max = AgeingSpawners.maxSpawns(entity);
		if (max < 0)
			return added;
		ageingspawners$spawned++;
		spawner.setChanged();
		if (ageingspawners$spawned >= max)
			level.removeBlock(pos, false);
		return added;
	}

	//? if >=1.21.11 {
	/*@Inject(method = "save", at = @At("RETURN"))
	private void ageingspawners$save(net.minecraft.world.level.storage.ValueOutput output, CallbackInfo ci) {
		output.putInt("AgeingSpawnersSpawned", ageingspawners$spawned);
		output.putBoolean("AgeingSpawnersPlayerPlaced", ageingspawners$playerPlaced);
	}

	@Inject(method = "load", at = @At("RETURN"))
	private void ageingspawners$load(Level level, BlockPos pos, net.minecraft.world.level.storage.ValueInput input, CallbackInfo ci) {
		ageingspawners$spawned = input.getIntOr("AgeingSpawnersSpawned", 0);
		ageingspawners$playerPlaced = input.getBooleanOr("AgeingSpawnersPlayerPlaced", false);
	}
	*///?} else {
	@Inject(method = "save", at = @At("RETURN"))
	private void ageingspawners$save(net.minecraft.nbt.CompoundTag tag, CallbackInfoReturnable<net.minecraft.nbt.CompoundTag> cir) {
		tag.putInt("AgeingSpawnersSpawned", ageingspawners$spawned);
		tag.putBoolean("AgeingSpawnersPlayerPlaced", ageingspawners$playerPlaced);
	}

	@Inject(method = "load", at = @At("RETURN"))
	private void ageingspawners$load(Level level, BlockPos pos, net.minecraft.nbt.CompoundTag tag, CallbackInfo ci) {
		ageingspawners$spawned = tag.getInt("AgeingSpawnersSpawned");
		ageingspawners$playerPlaced = tag.getBoolean("AgeingSpawnersPlayerPlaced");
	}
	//?}
}
