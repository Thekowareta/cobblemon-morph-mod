package com.example;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobblemonMorphMod implements ModInitializer {
	public static final String MOD_ID = "cobblemon-morph";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Cobblemon Morph cargado correctamente!");
		MorphNetwork.register();
		MorphCommand.register();
		BattleHandler.register();
	}
}