package com.example;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class MorphCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("morph")
                            .then(CommandManager.literal("info")
                                    .executes(MorphCommand::executeInfo))
                            .then(CommandManager.literal("toggle")
                                    .executes(MorphCommand::executeToggle))
            );
        });
    }

    // /morph info — muestra el pokemon de roleplay
    private static int executeInfo(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            source.sendError(Text.literal("Este comando solo puede usarlo un jugador."));
            return 0;
        }

        PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
        Pokemon rolePokemon = party.get(0);

        if (rolePokemon == null) {
            player.sendMessage(Text.literal("§cNo tienes un Pokemon en el slot 1 (roleplay)."));
        } else {
            String name = rolePokemon.getSpecies().getName();
            player.sendMessage(Text.literal("§bPokemon de Roleplay: §f" + name));
        }

        return 1;
    }

    // /morph toggle — alterna invisibilidad del jugador
    private static int executeToggle(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            source.sendError(Text.literal("Este comando solo puede usarlo un jugador."));
            return 0;
        }

        boolean isInvisible = player.isInvisible();

        if (isInvisible) {
            player.setInvisible(false);
            player.sendMessage(Text.literal("§aEres visible de nuevo."));
        } else {
            player.setInvisible(true);
            player.sendMessage(Text.literal("§7Ahora eres invisible."));
        }

        return 1;
    }
}