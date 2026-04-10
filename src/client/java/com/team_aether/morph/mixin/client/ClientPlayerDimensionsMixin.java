package com.team_aether.morph.mixin.client;

import com.team_aether.morph.render.MorphRenderManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntity.class)
public abstract class ClientPlayerDimensionsMixin {
    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void morph$getClientDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Player player) {
            EntityDimensions morph = MorphRenderManager.INSTANCE.getMorphDimensions(player.getUUID());
            if (morph != null) {
                cir.setReturnValue(morph);
            }
        }
    }
}
