package com.team_aether.morph.mixin.client;

import com.team_aether.morph.render.MorphShadowHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @ModifyVariable(method = "renderShadow", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    private static float morph$shadowRadius(float radius, com.mojang.blaze3d.vertex.PoseStack poseStack,
            net.minecraft.client.renderer.MultiBufferSource buffer, Entity entity,
            float opacity, float tickDelta, LevelReader level, float entityShadowRadius) {
        if (entity instanceof Player player) {
            Float morphRadius = MorphShadowHelper.INSTANCE.getShadowRadius(player.getUUID());
            if (morphRadius != null) {
                return morphRadius;
            }
        }
        return radius;
    }
}
