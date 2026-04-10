package com.example;

import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PokemonRenderer {

    private static final Map<UUID, Pokemon> activeMorphs = new HashMap<>();
    private static final Set<UUID> inBattle = new HashSet<>();

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

    public static String getMorphName(ServerPlayerEntity player) {
        Pokemon p = activeMorphs.get(player.getUuid());
        return p != null ? p.getSpecies().getName().toLowerCase() : "";
    }

    public static void setInBattle(UUID uuid) { inBattle.add(uuid); }
    public static void clearBattle(UUID uuid) { inBattle.remove(uuid); }
    public static boolean isInBattle(UUID uuid) { return inBattle.contains(uuid); }
}