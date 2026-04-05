package com.example;

import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PokemonRenderer {

    // Guarda qué Pokémon tiene activo cada jugador por su UUID
    private static final Map<UUID, Pokemon> activeMorphs = new HashMap<>();

    public static void setMorph(ServerPlayerEntity player, Pokemon pokemon) {
        activeMorphs.put(player.getUuid(), pokemon);
    }

    public static void clearMorph(ServerPlayerEntity player) {
        activeMorphs.remove(player.getUuid());
    }

    public static Pokemon getMorph(UUID playerUuid) {
        return activeMorphs.get(playerUuid);
    }

    public static boolean hasMorph(UUID playerUuid) {
        return activeMorphs.containsKey(playerUuid);
    }
}