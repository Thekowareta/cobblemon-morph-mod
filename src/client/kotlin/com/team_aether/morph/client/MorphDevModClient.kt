package com.team_aether.morph.client

import com.team_aether.morph.MorphDevMod
import com.team_aether.morph.event.ClientTickHandler
import com.team_aether.morph.network.MorphClientPacketHandler
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

object MorphDevModClient : ClientModInitializer {
    override fun onInitializeClient() {
        // Los tipos CustomPayload ya se registran en MorphDevMod (ModInitializer corre también en cliente).
        MorphClientPacketHandler.register()
        ClientTickEvents.END_CLIENT_TICK.register { ClientTickHandler.onClientTick(it) }
        MorphDevMod.LOGGER.info("Cliente morph: receptores de red y tick de pose.")
    }
}
