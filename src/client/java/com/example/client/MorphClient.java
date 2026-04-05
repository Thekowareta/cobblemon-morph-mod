package com.example.client;

import com.example.MorphNetwork;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MorphClient implements ClientModInitializer {

    // Guarda qué especie mostrar por cada UUID de jugador
    private static final Map<UUID, String> clientMorphs = new HashMap<>();

    @Override
    public void onInitializeClient() {
        // Escuchar paquetes del servidor
        ClientPlayNetworking.registerGlobalReceiver(
                MorphNetwork.MorphPayload.ID,
                (payload, context) -> {
                    context.client().execute(() -> {
                        UUID playerUuid = context.client().player.getUuid();
                        String speciesName = payload.speciesName();

                        if (speciesName.isEmpty()) {
                            clientMorphs.remove(playerUuid);
                        } else {
                            clientMorphs.put(playerUuid, speciesName);
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
}