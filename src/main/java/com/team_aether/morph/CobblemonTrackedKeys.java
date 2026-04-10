package com.team_aether.morph;

import com.cobblemon.mod.common.entity.PoseType;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Kotlin (client) no resuelve bien los estáticos del companion de {@link PokemonEntity}; delegamos en Java.
 */
public final class CobblemonTrackedKeys {
    private CobblemonTrackedKeys() {
    }

    @NotNull
    public static EntityDataAccessor<PoseType> poseType() {
        return PokemonEntity.getPOSE_TYPE();
    }

    @NotNull
    public static EntityDataAccessor<Boolean> moving() {
        return PokemonEntity.getMOVING();
    }

    @NotNull
    public static EntityDataAccessor<Set<String>> aspects() {
        return PokemonEntity.getASPECTS();
    }

    @NotNull
    public static EntityDataAccessor<Integer> labelLevel() {
        return PokemonEntity.getLABEL_LEVEL();
    }
}
