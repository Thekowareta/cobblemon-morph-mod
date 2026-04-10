package com.team_aether.morph.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("yHeadRot")
    float getHeadYaw();

    @Accessor("yHeadRot")
    void setHeadYaw(float v);

    @Accessor("yHeadRotO")
    float getHeadYawO();

    @Accessor("yHeadRotO")
    void setHeadYawO(float v);

    @Accessor("yBodyRot")
    void setBodyYaw(float v);
}
