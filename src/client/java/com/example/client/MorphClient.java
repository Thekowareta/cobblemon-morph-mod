package com.example.client;

import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.example.MorphNetwork;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MorphClient implements ClientModInitializer {

    private static final Map<UUID, String> clientMorphs = new HashMap<>();
    private static final Map<UUID, PokemonEntity> clientPokemonEntities = new HashMap<>();

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
                MorphNetwork.MorphPayload.ID,
                (payload, context) -> {
                    context.client().execute(() -> {
                        UUID playerUuid = context.client().player.getUuid();
                        String speciesName = payload.speciesName();

                        if (speciesName.isEmpty()) {
                            clientMorphs.remove(playerUuid);
                            clientPokemonEntities.remove(playerUuid);
                        } else {
                            clientMorphs.put(playerUuid, speciesName);

                            Species species = PokemonSpecies.INSTANCE.getByName(speciesName);
                            if (species != null && context.client().world != null) {
                                Pokemon pokemon = new Pokemon();
                                pokemon.setSpecies(species);
                                PokemonEntity entity = new PokemonEntity(
                                        context.client().world,
                                        pokemon,
                                        CobblemonEntities.POKEMON
                                );
                                clientPokemonEntities.put(playerUuid, entity);
                            }
                        }
                    });
                }
        );
    }

    public static String getMorph(UUID playerUuid) {
        return clientMorphs.get(playerUuid);
    }

    public static boolean hasMorph(UUID playerUuid) {
        return clientMorphs.containsKey(playerUuid);
    }

    public static PokemonEntity getActivePokemonEntity(UUID playerUuid) {
        return clientPokemonEntities.get(playerUuid);
    }
}