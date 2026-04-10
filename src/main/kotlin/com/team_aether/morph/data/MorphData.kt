package com.team_aether.morph.data

import com.cobblemon.mod.common.pokemon.Species
import net.minecraft.world.entity.EntityDimensions

data class MorphData(
    val species: Species,
    val dimensions: EntityDimensions,
    val additionalProperties: String,
)
