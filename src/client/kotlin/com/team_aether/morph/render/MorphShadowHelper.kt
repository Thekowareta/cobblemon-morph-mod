package com.team_aether.morph.render

import com.cobblemon.mod.common.client.entity.PokemonClientDelegate
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import java.util.UUID
import kotlin.math.min

@Environment(EnvType.CLIENT)
object MorphShadowHelper {
    private val shadowRadiusMap = mutableMapOf<UUID, Float>()

    fun prepareShadowRadius(playerUuid: UUID, morphEntity: PokemonEntity) {
        val bb = morphEntity.boundingBox
        val minDimension = min(
            (bb.maxX - bb.minX).toFloat(),
            (bb.maxZ - bb.minZ).toFloat(),
        )
        val delegate = morphEntity.delegate as? PokemonClientDelegate ?: return
        val modifier = delegate.entityScaleModifier
        val scaleDiv = morphEntity.bbWidth.coerceAtLeast(0.01f)
        shadowRadiusMap[playerUuid] = minDimension / 1.5f * modifier / scaleDiv
    }

    fun getShadowRadius(playerUuid: UUID): Float? = shadowRadiusMap[playerUuid]

    fun clearShadowRadius(playerUuid: UUID) {
        shadowRadiusMap.remove(playerUuid)
    }
}
