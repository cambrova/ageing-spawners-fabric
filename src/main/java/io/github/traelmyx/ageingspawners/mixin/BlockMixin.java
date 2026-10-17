package io.github.traelmyx.ageingspawners.mixin;

import io.github.traelmyx.ageingspawners.SpawnerAge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Remembers spawners a player placed, for the playerPlacedOnly option. */
@Mixin(Block.class)
public abstract class BlockMixin {
	@Inject(method = "setPlacedBy", at = @At("HEAD"))
	private void ageingspawners$placedByPlayer(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, CallbackInfo ci) {
		if (!level.isClientSide() && placer instanceof Player && level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
			((SpawnerAge) spawner.getSpawner()).ageingspawners$setPlayerPlaced(true);
			spawner.setChanged();
		}
	}
}
