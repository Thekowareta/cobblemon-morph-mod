package com.team_aether.morph.mixin.client;

import com.cobblemon.mod.common.api.entity.PokemonSideDelegate;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.team_aether.morph.render.MorphRenderManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(PokemonEntity.class)
public abstract class PokemonEntityMixin {
    @Shadow
    public abstract PokemonSideDelegate getDelegate();

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void morph$tick(CallbackInfo ci) {
        PokemonEntity self = (PokemonEntity) (Object) this;
        if (MorphRenderManager.INSTANCE.isMorphPuppet(self)) {
            ci.cancel();
            EntityTickCountAccessor ticks = (EntityTickCountAccessor) (Object) self;
            ticks.morph$setTickCount(ticks.morph$getTickCount() + 1);
            getDelegate().tick(self);
            ((LivingEntitySwingInvoker) self).morph$callUpdateSwingTime();
        }
    }

    @Inject(method = "canBattle", at = @At("HEAD"), cancellable = true)
    private void morph$canBattle(Player player, CallbackInfoReturnable<Boolean> cir) {
        PokemonEntity self = (PokemonEntity) (Object) this;
        if (MorphRenderManager.INSTANCE.isMorphPuppet(self)) {
            cir.setReturnValue(false);
        }
    }
}
