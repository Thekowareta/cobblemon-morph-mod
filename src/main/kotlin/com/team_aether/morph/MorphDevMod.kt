package com.team_aether.morph

import com.team_aether.morph.command.MorphCommand
import com.team_aether.morph.manager.MorphManager
import com.team_aether.morph.network.ClearMorphPacket
import com.team_aether.morph.network.MorphPacketHandler
import com.team_aether.morph.network.MorphSyncPacket
import com.team_aether.morph.network.SyncPoseS2CPacket
import com.team_aether.morph.network.UpdatePoseC2SPacket
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import org.slf4j.LoggerFactory

object MorphDevMod : ModInitializer {
    const val MOD_ID: String = "cobblemon-morph-dev"
    val LOGGER = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        PayloadTypeRegistry.playS2C().register(MorphSyncPacket.TYPE, MorphSyncPacket.CODEC)
        PayloadTypeRegistry.playS2C().register(SyncPoseS2CPacket.TYPE, SyncPoseS2CPacket.CODEC)
        PayloadTypeRegistry.playC2S().register(ClearMorphPacket.TYPE, ClearMorphPacket.CODEC)
        PayloadTypeRegistry.playC2S().register(UpdatePoseC2SPacket.TYPE, UpdatePoseC2SPacket.CODEC)
        MorphPacketHandler.register()
        MorphCommand.register()
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            handler.player?.let { MorphManager.onPlayerLogout(it) }
        }
        LOGGER.info("Cobblemon morph: red, comando y morph manager registrados.")
    }
}
