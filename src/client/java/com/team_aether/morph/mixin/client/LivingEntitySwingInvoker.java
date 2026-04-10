package com.team_aether.morph.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntity.class)
public interface LivingEntitySwingInvoker {
    @Invoker("updateSwingTime")
    void morph$callUpdateSwingTime();
}
