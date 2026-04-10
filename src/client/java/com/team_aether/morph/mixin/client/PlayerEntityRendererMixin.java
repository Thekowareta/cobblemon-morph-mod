package com.team_aether.morph.mixin.client;

import com.cobblemon.mod.common.entity.PoseType;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.properties.UncatchableProperty;
import com.mojang.blaze3d.vertex.PoseStack;
import com.team_aether.morph.CobblemonTrackedKeys;
import com.team_aether.morph.render.MorphRenderManager;
import com.team_aether.morph.render.MorphShadowHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(net.minecraft.client.renderer.entity.player.PlayerRenderer.class)
public class PlayerEntityRendererMixin {
    @Inject(
        method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void morph$replaceWithPokemon(
        AbstractClientPlayer player,
        float entityYaw,
        float partialTicks,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        CallbackInfo ci
    ) {
        PokemonEntity morph = MorphRenderManager.INSTANCE.getMorphEntity(player.getUUID());
        if (morph != null) {
            MorphShadowHelper.INSTANCE.prepareShadowRadius(player.getUUID(), morph);
            ci.cancel();
            morph$renderMorph(morph, player, partialTicks, poseStack, buffer, packedLight);
        }
    }

    @Unique
    private void morph$renderMorph(
        PokemonEntity morphEntity,
        AbstractClientPlayer sourcePlayer,
        float partialTicks,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight
    ) {
        morph$updateMorphState(morphEntity, sourcePlayer);
        Minecraft client = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
        @SuppressWarnings("unchecked")
        LivingEntityRenderer<PokemonEntity, ?> pokemonRenderer =
            (LivingEntityRenderer<PokemonEntity, ?>) dispatcher.getRenderer(morphEntity);
        // Vanilla LivingEntityRenderer espera el yaw del cuerpo interpolado (yBodyRotO → yBodyRot), no mezcla cabeza/cuerpo.
        float bodyYaw = Mth.rotLerp(partialTicks, sourcePlayer.yBodyRotO, sourcePlayer.yBodyRot);
        dispatcher.setRenderShadow(false);
        pokemonRenderer.render(morphEntity, bodyYaw, partialTicks, poseStack, buffer, packedLight);
        dispatcher.setRenderShadow(true);
    }

    @Unique
    private void morph$updateMorphState(PokemonEntity morphEntity, AbstractClientPlayer sourcePlayer) {
        PoseType syncedPose = MorphRenderManager.INSTANCE.getMorphPose(sourcePlayer.getUUID());
        boolean isLocal = sourcePlayer == Minecraft.getInstance().player;

        if (morphEntity.getCurrentPoseType() != syncedPose) {
            morphEntity.getEntityData().set(CobblemonTrackedKeys.poseType(), syncedPose);
        }
        morphEntity.getEntityData().set(PokemonEntity.getHIDE_LABEL(), isLocal);
        morphEntity.getEntityData().set(PokemonEntity.getLABEL_LEVEL(), 0);
        morphEntity.setCustomName(sourcePlayer.getDisplayName());
        morphEntity.setInvisible(!isLocal);
        UncatchableProperty.INSTANCE.uncatchable().apply(morphEntity.getPokemon());

        boolean moving = syncedPose == PoseType.WALK || syncedPose == PoseType.SWIM || syncedPose == PoseType.FLY;
        Boolean cur = morphEntity.getEntityData().get(CobblemonTrackedKeys.moving());
        if (cur == null || cur != moving) {
            morphEntity.getEntityData().set(CobblemonTrackedKeys.moving(), moving);
        }

        boolean canFly = morphEntity.getBehaviour().getMoving().getFly().getCanFly();
        boolean canWalk = MorphRenderManager.INSTANCE.getCanWalk(sourcePlayer.getUUID());
        if (canFly) {
            morphEntity.setPokemonWalking(canWalk && syncedPose == PoseType.WALK);
            morphEntity.setPokemonFlying(syncedPose == PoseType.FLY);
        } else {
            morphEntity.setPokemonWalking(syncedPose == PoseType.WALK);
            morphEntity.setPokemonFlying(syncedPose == PoseType.FLY);
        }

        morphEntity.xo = morphEntity.getX();
        morphEntity.yo = morphEntity.getY();
        morphEntity.zo = morphEntity.getZ();
        morphEntity.setPos(sourcePlayer.getX(), sourcePlayer.getY(), sourcePlayer.getZ());
        morphEntity.yRotO = sourcePlayer.yRotO;
        morphEntity.setYRot(sourcePlayer.getYRot());
        morphEntity.setDeltaMovement(sourcePlayer.getDeltaMovement());
        morphEntity.attackAnim = sourcePlayer.attackAnim;
        morphEntity.yBodyRotO = sourcePlayer.yBodyRotO;
        morphEntity.yBodyRot = sourcePlayer.yBodyRot;
        LivingEntityAccessor morphAcc = (LivingEntityAccessor) morphEntity;
        LivingEntityAccessor playerAcc = (LivingEntityAccessor) sourcePlayer;
        morphAcc.setHeadYaw(playerAcc.getHeadYaw());
        morphAcc.setHeadYawO(playerAcc.getHeadYawO());
        morphAcc.setBodyYaw(sourcePlayer.yBodyRot);

        boolean flyingPose = syncedPose == PoseType.FLY || syncedPose == PoseType.HOVER;
        if (flyingPose) {
            morphEntity.setXRot(0f);
            morphEntity.xRotO = 0f;
        } else {
            morphEntity.setXRot(sourcePlayer.getXRot());
            morphEntity.xRotO = sourcePlayer.xRotO;
        }

        morph$syncLimb(morphEntity, sourcePlayer);
    }

    @Unique
    private void morph$syncLimb(PokemonEntity morphEntity, AbstractClientPlayer sourcePlayer) {
        WalkAnimationStateAccessor pa = (WalkAnimationStateAccessor) sourcePlayer.walkAnimation;
        WalkAnimationStateAccessor ma = (WalkAnimationStateAccessor) morphEntity.walkAnimation;
        ma.setSpeed(pa.getSpeed());
        ma.setSpeedOld(pa.getSpeedOld());
        ma.setPosition(pa.getPosition());
    }
}
