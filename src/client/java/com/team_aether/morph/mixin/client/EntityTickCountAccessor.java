package com.team_aether.morph.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public interface EntityTickCountAccessor {
    @Accessor("tickCount")
    int morph$getTickCount();

    @Accessor("tickCount")
    void morph$setTickCount(int value);
}
