package com.team_aether.morph.render

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.properties.UncatchableProperty
import com.team_aether.morph.CobblemonTrackedKeys
import com.team_aether.morph.MorphDevMod
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.EntityDimensions
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
@Environment(EnvType.CLIENT)
object MorphRenderManager {
    private val morphPuppets = ConcurrentHashMap.newKeySet<PokemonEntity>()
    private val morphedPlayers = ConcurrentHashMap<UUID, ClientMorphData>()

    private class ClientMorphData(
        val puppet: PokemonEntity,
        val dimensions: EntityDimensions,
        var currentPose: PoseType,
        val canFly: Boolean,
        val canWalk: Boolean,
    ) {
        fun dispose() {
            MorphRenderManager.morphPuppets.remove(puppet)
            puppet.discard()
        }
    }

    fun isMorphPuppet(entity: PokemonEntity): Boolean = morphPuppets.contains(entity)

    fun updateMorph(
        playerUuid: UUID,
        speciesName: String,
        additionalProperties: String,
        width: Float,
        height: Float,
        initialPose: PoseType,
        canFly: Boolean,
        canWalk: Boolean,
    ) {
        if (speciesName.isBlank()) {
            clearMorph(playerUuid)
            return
        }
        morphedPlayers[playerUuid]?.dispose()
        val puppet = createPuppetEntity(speciesName, additionalProperties) ?: run {
            clearMorph(playerUuid)
            return
        }
        morphPuppets.add(puppet)
        val dimensions = EntityDimensions.scalable(width, height)
        val data = ClientMorphData(puppet, dimensions, initialPose, canFly, canWalk)
        morphedPlayers[playerUuid] = data
        try {
            val ed = puppet.entityData
            ed.set(CobblemonTrackedKeys.poseType(), initialPose)
            val moving = initialPose == PoseType.WALK || initialPose == PoseType.SWIM || initialPose == PoseType.FLY
            ed.set(CobblemonTrackedKeys.moving(), moving)
        } catch (e: Exception) {
            MorphDevMod.LOGGER.warn("Failed initial puppet entity data", e)
        }
    }

    fun updateMorphPose(playerUuid: UUID, newPose: PoseType) {
        val data = morphedPlayers[playerUuid] ?: return
        if (data.currentPose == newPose) return
        data.currentPose = newPose
        try {
            val ed = data.puppet.entityData
            ed.set(CobblemonTrackedKeys.poseType(), newPose)
            val moving = newPose == PoseType.WALK || newPose == PoseType.SWIM || newPose == PoseType.FLY
            ed.set(CobblemonTrackedKeys.moving(), moving)
        } catch (e: Exception) {
            MorphDevMod.LOGGER.warn("Failed puppet pose update", e)
        }
    }

    fun getMorphEntity(playerUuid: UUID): PokemonEntity? = morphedPlayers[playerUuid]?.puppet

    fun getMorphDimensions(playerUuid: UUID): EntityDimensions? = morphedPlayers[playerUuid]?.dimensions

    fun getMorphPose(playerUuid: UUID): PoseType = morphedPlayers[playerUuid]?.currentPose ?: PoseType.STAND

    fun getCanFly(playerUuid: UUID): Boolean = morphedPlayers[playerUuid]?.canFly ?: false

    fun getCanWalk(playerUuid: UUID): Boolean = morphedPlayers[playerUuid]?.canWalk ?: true

    fun getActiveMorphs(): Collection<PokemonEntity> = morphedPlayers.values.map { it.puppet }

    fun clearMorph(playerUuid: UUID) {
        morphedPlayers.remove(playerUuid)?.dispose()
        MorphShadowHelper.clearShadowRadius(playerUuid)
    }

    fun clear() {
        morphedPlayers.keys.forEach { MorphShadowHelper.clearShadowRadius(it) }
        morphedPlayers.values.forEach { it.dispose() }
        morphedPlayers.clear()
    }

    private fun createPuppetEntity(speciesName: String, additionalProperties: String): PokemonEntity? {
        return try {
            val client = Minecraft.getInstance()
            val level = client.level ?: return null
            val baseProperties = PokemonProperties.parse(speciesName.lowercase())
            val pokemon = baseProperties.create()
            if (additionalProperties.isNotBlank()) {
                val add = PokemonProperties.parse(additionalProperties.trim())
                add.apply(pokemon)
            }
            val aspectKeywords = listOf(
                "shiny", "partner", "regionalpartner", "partnerevo",
                "alolan", "galarian", "hisuian", "paldean",
                "mega", "mega-x", "mega-y", "gmax",
            )
            val forced = aspectKeywords.filter { additionalProperties.contains(it, ignoreCase = true) }.toMutableSet()
            pokemon.forcedAspects = forced
            pokemon.shiny = false
            val entityProps = PokemonProperties.parse("${speciesName.lowercase()} no_ai")
            val puppet = entityProps.createEntity(level) ?: return null
            puppet.pokemon = pokemon
            try {
                puppet.entityData.set(CobblemonTrackedKeys.aspects(), pokemon.aspects)
                puppet.entityData.set(CobblemonTrackedKeys.labelLevel(), pokemon.level)
            } catch (e: Exception) {
                MorphDevMod.LOGGER.warn("Aspects/level on puppet", e)
            }
            puppet
        } catch (e: Exception) {
            MorphDevMod.LOGGER.error("Failed puppet for {}", speciesName, e)
            null
        }
    }
}
