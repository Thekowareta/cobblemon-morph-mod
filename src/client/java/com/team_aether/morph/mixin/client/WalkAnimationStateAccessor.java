package com.team_aether.morph.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(WalkAnimationState.class)
public interface WalkAnimationStateAccessor {
    @Accessor("speed")
    float getSpeed();

    @Accessor("speed")
    void setSpeed(float v);

    @Accessor("speedOld")
    float getSpeedOld();

    @Accessor("speedOld")
    void setSpeedOld(float v);

    @Accessor("position")
    float getPosition();

    @Accessor("position")
    void setPosition(float v);
}
