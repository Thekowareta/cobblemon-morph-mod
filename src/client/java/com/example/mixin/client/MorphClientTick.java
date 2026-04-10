package com.example.client;

import com.cobblemon.mod.common.client.entity.PokemonClientDelegate;

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

            float speed = (float) player.getVelocity().horizontalLength();
            boolean isMoving = speed > 0.01f;

            entity.setVelocity(player.getVelocity());
            entity.setOnGround(player.isOnGround());
            entity.tick();


            // Sincronizar posición después del tick
            double prevX = entity.getX();
            double prevY = entity.getY();
            double prevZ = entity.getZ();

            entity.setPos(player.getX(), player.getY(), player.getZ());
            entity.prevX = prevX;
            entity.prevY = prevY;
            entity.prevZ = prevZ;
            entity.lastRenderX = prevX;
            entity.lastRenderY = prevY;
            entity.lastRenderZ = prevZ;

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