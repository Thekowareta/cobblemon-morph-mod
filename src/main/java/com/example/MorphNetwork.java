package com.example;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class MorphNetwork {

    // Identificador único de nuestro paquete
    public static final Identifier MORPH_PACKET_ID =
            Identifier.of("cobblemon-morph", "morph_update");

    // Registro del paquete — se llama al iniciar el mod
    public static void register() {
        PayloadTypeRegistry.playS2C().register(
                MorphPayload.ID,
                MorphPayload.CODEC
        );
    }

    // Enviar al cliente qué Pokémon mostrar (o limpiar el morph)
    public static void sendMorphUpdate(ServerPlayerEntity player, String speciesName) {
        ServerPlayNetworking.send(player, new MorphPayload(speciesName));
    }

    // El paquete en sí — contiene el nombre de la especie
    public record MorphPayload(String speciesName) implements CustomPayload {

        public static final CustomPayload.Id<MorphPayload> ID =
                new CustomPayload.Id<>(MORPH_PACKET_ID);

        public static final PacketCodec<PacketByteBuf, MorphPayload> CODEC =
                PacketCodec.of(
                        (payload, buf) -> buf.writeString(payload.speciesName()),
                        buf -> new MorphPayload(buf.readString())
                );

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}