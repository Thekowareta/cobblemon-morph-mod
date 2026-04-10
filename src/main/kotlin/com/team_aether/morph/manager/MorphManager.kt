package com.team_aether.morph.manager

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Species
import com.team_aether.morph.MorphDevMod
import com.team_aether.morph.data.MorphData
import com.team_aether.morph.network.MorphSyncPacket
import com.team_aether.morph.network.SyncPoseS2CPacket
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Pose

object MorphManager {
    private val morphedPlayers = java.util.concurrent.ConcurrentHashMap<java.util.UUID, MorphData>()
    private val lastKnownPoses = java.util.concurrent.ConcurrentHashMap<java.util.UUID, PoseType>()

    fun isMorphed(playerUuid: java.util.UUID): Boolean = morphedPlayers.containsKey(playerUuid)

    fun getMorphData(playerUuid: java.util.UUID): MorphData? = morphedPlayers[playerUuid]

    fun getLastKnownPose(playerUuid: java.util.UUID): PoseType =
        lastKnownPoses[playerUuid] ?: PoseType.STAND

    fun applyMorph(player: ServerPlayer, species: Species, additionalProperties: String): Boolean {
        return try {
            val propsText = "${species.name.lowercase()} no_ai"
            val properties = PokemonProperties.parse(propsText)
            val measurementEntity: PokemonEntity = properties.createEntity(player.level()) ?: run {
                MorphDevMod.LOGGER.error("createEntity returned null for {}", species.name)
                return false
            }
            val dimensions = measurementEntity.getDimensions(Pose.STANDING)
            val canFly = measurementEntity.behaviour.moving.`fly`.canFly
            val box = dimensions.makeBoundingBox(player.position())
            if (!player.level().noCollision(player, box)) {
                player.displayClientMessage(
                    Component.literal("Not enough space to morph here!").withStyle(ChatFormatting.RED),
                    false,
                )
                return false
            }
            val data = MorphData(species, dimensions, additionalProperties)
            morphedPlayers[player.uuid] = data
            player.refreshDimensions()
            val initialPose = calculatePose(player)
            lastKnownPoses[player.uuid] = initialPose
            syncToClients(
                player.server,
                player.uuid,
                species.name,
                additionalProperties,
                dimensions.width(),
                dimensions.height(),
                initialPose,
                canFly,
                canWalk = true,
            )
            true
        } catch (e: Exception) {
            MorphDevMod.LOGGER.error("Failed to apply morph {} for player {}", species.name, player.gameProfile.name, e)
            false
        }
    }

    fun clearMorph(player: ServerPlayer) {
        if (!isMorphed(player.uuid)) return
        morphedPlayers.remove(player.uuid)
        lastKnownPoses.remove(player.uuid)
        player.refreshDimensions()
        val clearPacket = MorphSyncPacket(
            player.uuid,
            "",
            "",
            0f,
            0f,
            PoseType.STAND,
            false,
            false,
        )
        for (other in player.server.playerList.players) {
            ServerPlayNetworking.send(other, clearPacket)
        }
    }

    fun handlePoseUpdate(player: ServerPlayer, newPose: PoseType) {
        if (!isMorphed(player.uuid)) return
        val old = lastKnownPoses[player.uuid]
        if (old == newPose) return
        lastKnownPoses[player.uuid] = newPose
        val sync = SyncPoseS2CPacket(player.uuid, newPose)
        for (viewer in PlayerLookup.tracking(player as Entity)) {
            ServerPlayNetworking.send(viewer, sync)
        }
        ServerPlayNetworking.send(player, sync)
    }

    fun onPlayerLogout(player: ServerPlayer) {
        lastKnownPoses.remove(player.uuid)
    }

    private fun calculatePose(player: ServerPlayer): PoseType = when {
        player.isSleeping -> PoseType.SLEEP
        player.abilities.flying || player.isFallFlying -> PoseType.FLY
        player.isInWater || player.isInLava -> PoseType.SWIM
        player.deltaMovement.lengthSqr() > 1.0E-6 -> PoseType.WALK
        else -> PoseType.STAND
    }

    private fun syncToClients(
        server: net.minecraft.server.MinecraftServer,
        playerUuid: java.util.UUID,
        speciesName: String,
        additionalProperties: String,
        width: Float,
        height: Float,
        initialPose: PoseType,
        canFly: Boolean,
        canWalk: Boolean,
    ) {
        val packet = MorphSyncPacket(
            playerUuid,
            speciesName,
            additionalProperties,
            width,
            height,
            initialPose,
            canFly,
            canWalk,
        )
        for (other in server.playerList.players) {
            ServerPlayNetworking.send(other, packet)
        }
    }
}
