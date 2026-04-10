package com.team_aether.morph.event

import com.cobblemon.mod.common.entity.PoseType
import com.team_aether.morph.network.UpdatePoseC2SPacket
import com.team_aether.morph.render.MorphRenderManager
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.Minecraft

@Environment(EnvType.CLIENT)
object ClientTickHandler {
    private var lastSentPose: PoseType? = null

    private fun calculatePose(client: Minecraft): PoseType {
        val player = client.player ?: return PoseType.STAND
        val dx = player.deltaMovement.x
        val dz = player.deltaMovement.z
        // Solo velocidad real (no walkAnimation.isMoving: da falso positivo con input sin desplazamiento).
        val horizontalSpeedSq = dx * dx + dz * dz
        val isMovingHorizontally = horizontalSpeedSq > 2.5e-4
        val canFly = MorphRenderManager.getCanFly(player.uuid)
        val canWalk = MorphRenderManager.getCanWalk(player.uuid)
        return when {
            player.isSleeping -> PoseType.SLEEP
            player.abilities.flying || player.isFallFlying -> PoseType.FLY
            player.isInWater || player.isInLava -> PoseType.SWIM
            // Pie o sprint en suelo: WALK (nunca FLY solo por sprint).
            isMovingHorizontally && player.onGround() -> PoseType.WALK
            // En el aire: FLY solo si la especie no camina (evita animación de vuelo al saltar con Charizard, etc.).
            isMovingHorizontally && !player.onGround() && canFly && !canWalk -> PoseType.FLY
            isMovingHorizontally -> PoseType.WALK
            else -> PoseType.STAND
        }
    }

    fun onClientTick(client: Minecraft) {
        val player = client.player ?: return
        MorphRenderManager.getActiveMorphs().forEach { it.tick() }
        if (MorphRenderManager.getMorphEntity(player.uuid) == null) {
            lastSentPose = null
            return
        }
        val currentPose = calculatePose(client)
        if (currentPose != lastSentPose) {
            lastSentPose = currentPose
            ClientPlayNetworking.send(UpdatePoseC2SPacket(currentPose))
            MorphRenderManager.updateMorphPose(player.uuid, currentPose)
        }
    }
}
