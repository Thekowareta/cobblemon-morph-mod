package com.example;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class BattleHandler {

    public static void register() {
        CobblemonEvents.BATTLE_STARTED_POST.subscribe(event -> {
            handleBattleStart(event.getBattle());
        });
        CobblemonEvents.BATTLE_VICTORY.subscribe(event -> {
            handleBattleEnd(event.getBattle());
        });
        CobblemonEvents.BATTLE_FLED.subscribe(event -> {
            handleBattleEnd(event.getBattle());
        });
    }

    private static void handleBattleStart(PokemonBattle battle) {
        for (BattleActor actor : battle.getActors()) {
            if (actor instanceof PlayerBattleActor playerActor) {
                ServerPlayerEntity player = playerActor.getEntity();
                if (player != null) {
                    PokemonRenderer.setInBattle(player.getUuid()); // ← aquí dentro
                    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
                    Pokemon rolePokemon = party.get(0);
                    if (rolePokemon == null) {
                        player.sendMessage(Text.literal("§cNecesitas un Pokemon en el slot 1."));
                        return;
                    }
                    player.setInvisible(true);
                    PokemonRenderer.clearMorph(player);
                    MorphNetwork.sendMorphUpdate(player, "");
                    player.sendMessage(Text.literal("§7Entraste a combate — eres invisible."));
                }
            }
        }
    }

    private static void handleBattleEnd(PokemonBattle battle) {
        for (BattleActor actor : battle.getActors()) {
            if (actor instanceof PlayerBattleActor playerActor) {
                ServerPlayerEntity player = playerActor.getEntity();
                if (player != null) {
                    PokemonRenderer.clearBattle(player.getUuid()); // ← aquí dentro
                    player.setInvisible(false);
                    Pokemon rolePokemon = Cobblemon.INSTANCE.getStorage().getParty(player).get(0);
                    if (rolePokemon != null) {
                        PokemonRenderer.setMorph(player, rolePokemon);
                        MorphNetwork.sendMorphUpdate(player,
                                rolePokemon.getSpecies().getName().toLowerCase());
                    }
                    player.sendMessage(Text.literal("§aCombate terminado — eres visible."));
                }
            }
        }
    }
}