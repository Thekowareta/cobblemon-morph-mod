package com.team_aether.morph.mixin;

import com.team_aether.morph.data.MorphData;
import com.team_aether.morph.manager.MorphManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ServerPlayerDimensionsMixin {
    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void morph$getDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Player player) {
            MorphData morphData = MorphManager.INSTANCE.getMorphData(player.getUUID());
            if (morphData != null) {
                cir.setReturnValue(morphData.getDimensions());
            }
        }
    }
}
