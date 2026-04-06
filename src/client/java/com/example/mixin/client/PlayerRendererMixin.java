package com.example.mixin.client;

import com.example.client.MorphClient;
import com.cobblemon.mod.common.client.render.pokemon.PokemonRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerRendererMixin {

    @Inject(
            at = @At("HEAD"),
            method = "render",
            cancellable = true
    )
    private void onRender(
            AbstractClientPlayerEntity player,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        // ✅ verificaciones van aquí dentro
        if (player == null) return;
        if (!MorphClient.hasMorph(player.getUuid())) return;

        MinecraftClient client = MinecraftClient.getInstance();
        PokemonRenderer pokemonRenderer = (PokemonRenderer) client
                .getEntityRenderDispatcher()
                .getRenderer(MorphClient.getActivePokemonEntity(player.getUuid()));

        if (pokemonRenderer == null) return;

        ci.cancel();
        pokemonRenderer.render(
                MorphClient.getActivePokemonEntity(player.getUuid()),
                yaw,
                tickDelta,
                matrices,
                vertexConsumers,
                light
        );
    }
}