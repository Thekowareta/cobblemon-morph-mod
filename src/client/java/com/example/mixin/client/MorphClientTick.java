package com.example.client;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.network.ClientPlayerEntity;

public class MorphClientTick {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayerEntity player = client.player;
            if (player == null) return;

            PokemonEntity entity = MorphClient.getActivePokemonEntity(player.getUuid());
            if (entity == null) return;

            // ✅ orden correcto
            entity.setVelocity(player.getVelocity());
            entity.tick(); // primero tickear

            // luego sincronizar posición encima de lo que hizo el tick
            entity.setPos(player.getX(), player.getY(), player.getZ());
            entity.prevX = player.prevX;
            entity.lastRenderX = player.lastRenderX;
            entity.prevY = player.prevY;
            entity.lastRenderY = player.lastRenderY;
            entity.prevZ = player.prevZ;
            entity.lastRenderZ = player.lastRenderZ;

            entity.setYaw(player.getYaw());
            entity.setPitch(player.getPitch());
            entity.prevPitch = player.prevPitch;
            entity.headYaw = player.headYaw;
            entity.prevHeadYaw = player.prevHeadYaw;
            entity.bodyYaw = player.bodyYaw;
            entity.prevBodyYaw = player.prevBodyYaw;

            entity.age++;


        });
    }
}