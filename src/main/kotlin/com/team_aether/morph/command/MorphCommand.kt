package com.team_aether.morph.command

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.command.argument.PokemonPropertiesArgumentType
import com.cobblemon.mod.common.pokemon.Species
import com.mojang.brigadier.CommandDispatcher
import com.team_aether.morph.MorphConstants
import com.team_aether.morph.manager.MorphManager
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

object MorphCommand {
    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher: CommandDispatcher<CommandSourceStack>, _, _ ->
            dispatcher.register(
                Commands.literal("morph")
                    .then(
                        Commands.literal("clear")
                            .executes { ctx ->
                                val player = ctx.source.playerOrException
                                MorphManager.clearMorph(player)
                                player.displayClientMessage(
                                    Component.literal("Morph cleared!").withStyle(ChatFormatting.GREEN),
                                    false,
                                )
                                1
                            },
                    )
                    .then(
                        Commands.argument(MorphConstants.COMMAND_PROPERTIES, PokemonPropertiesArgumentType.properties())
                            .executes { ctx ->
                                applyMorph(ctx.source.playerOrException, ctx)
                            },
                    ),
            )
        }
    }

    private fun applyMorph(player: ServerPlayer, ctx: com.mojang.brigadier.context.CommandContext<CommandSourceStack>): Int {
        val props = PokemonPropertiesArgumentType.getPokemonProperties(ctx, MorphConstants.COMMAND_PROPERTIES)
        if (!props.hasSpecies()) {
            player.displayClientMessage(
                Component.literal("Invalid Pokémon properties.").withStyle(ChatFormatting.RED),
                false,
            )
            return 0
        }
        val speciesName = props.species ?: return 0
        val species: Species = PokemonSpecies.getByName(speciesName) ?: run {
            player.displayClientMessage(
                Component.literal("Pokémon not found: $speciesName").withStyle(ChatFormatting.RED),
                false,
            )
            return 0
        }
        val dex = Cobblemon.playerDataManager.getPokedexData(player)
        val id = ResourceLocation.parse(species.resourceIdentifier.toString())
        val knowledge = dex.getKnowledgeForSpecies(id)
        if (knowledge == PokedexEntryProgress.NONE) {
            player.displayClientMessage(
                Component.literal("You haven't encountered ${species.name} yet!").withStyle(ChatFormatting.RED),
                false,
            )
            return 0
        }
        val original = props.originalString
        val additional = original.substringAfter(' ', "").trim()
        val success = MorphManager.applyMorph(player, species, additional)
        return if (success) {
            val aspectInfo = if (additional.isNotBlank()) " ($additional)" else ""
            player.displayClientMessage(
                Component.literal("Morphed into ${species.name}$aspectInfo!").withStyle(ChatFormatting.GREEN),
                false,
            )
            1
        } else {
            player.displayClientMessage(
                Component.literal("Failed to morph into ${species.name}.").withStyle(ChatFormatting.RED),
                false,
            )
            0
        }
    }
}
