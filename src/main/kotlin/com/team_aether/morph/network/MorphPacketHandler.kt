package com.team_aether.morph.network

import com.team_aether.morph.MorphDevMod
import com.team_aether.morph.manager.MorphManager
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

object MorphPacketHandler {
    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(ClearMorphPacket.TYPE) { _: ClearMorphPacket, context ->
            context.server().execute {
                val player = context.player() ?: return@execute
                MorphManager.clearMorph(player)
                player.displayClientMessage(
                    Component.literal("Morph cleared!").withStyle(ChatFormatting.GREEN),
                    false,
                )
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(UpdatePoseC2SPacket.TYPE) { packet: UpdatePoseC2SPacket, context ->
            context.server().execute {
                val player = context.player() ?: return@execute
                MorphManager.handlePoseUpdate(player, packet.pose)
                if (player.tickCount % 20 == 0) {
                    MorphDevMod.LOGGER.debug("Broadcast pose {} from {}", packet.pose, player.gameProfile.name)
                }
            }
        }
    }
}
