package com.example;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class CobblemonMorphMod implements ModInitializer {
	public static final String MOD_ID = "cobblemon-morph";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Cobblemon Morph cargado correctamente!");

		MorphNetwork.register();
		MorphCommand.register();
		BattleHandler.register();

		// Activar morph al unirse al mundo
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				var party = Cobblemon.INSTANCE.getStorage().getParty(player);
				Pokemon rolePokemon = party.get(0);
				String current = PokemonRenderer.getMorphName(player);

				if (rolePokemon == null) continue;

				String speciesName = rolePokemon.getSpecies().getName().toLowerCase();
				if (speciesName.equals(current)) continue;
				if (PokemonRenderer.isInBattle(player.getUuid())) continue;

				PokemonRenderer.setMorph(player, rolePokemon);
				MorphNetwork.sendMorphUpdate(player, speciesName);
				player.setInvisible(true);
			}
		});
	}
}