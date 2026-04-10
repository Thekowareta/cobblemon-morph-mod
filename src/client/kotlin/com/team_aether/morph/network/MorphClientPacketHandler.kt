package com.team_aether.morph.network

import com.team_aether.morph.MorphDevMod
import com.team_aether.morph.render.MorphRenderManager
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object MorphClientPacketHandler {
    fun register() {
        ClientPlayNetworking.registerGlobalReceiver(MorphSyncPacket.TYPE) { packet, context ->
            context.client().execute {
                MorphRenderManager.updateMorph(
                    packet.playerUuid,
                    packet.speciesName,
                    packet.additionalProperties,
                    packet.width,
                    packet.height,
                    packet.initialPose,
                    packet.canFly,
                    packet.canWalk,
                )
                val level = context.client().level ?: return@execute
                level.getPlayerByUUID(packet.playerUuid)?.refreshDimensions()
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(SyncPoseS2CPacket.TYPE) { packet, context ->
            context.client().execute {
                MorphDevMod.LOGGER.debug("Pose update {} -> {}", packet.playerUuid, packet.pose)
                MorphRenderManager.updateMorphPose(packet.playerUuid, packet.pose)
            }
        }
    }
}
