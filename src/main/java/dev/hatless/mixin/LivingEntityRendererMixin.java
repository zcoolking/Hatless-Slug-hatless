package dev.hatless.mixin;

import dev.hatless.client.HatlessClientState;
import dev.hatless.client.HatlessRenderState;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    /** Remember which player a render state belongs to + add the mod symbol to the name tag. */
    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL"))
    private void hatless$tagPlayer(LivingEntity entity, LivingEntityRenderState state, float tickDelta, CallbackInfo ci) {
        if (!(entity instanceof PlayerEntity player)) return;
        ((HatlessRenderState) state).hatless$setPlayerId(player.getUuid());
        if (state.displayName != null && HatlessClientState.hasMod(player.getUuid())) {
            state.displayName = HatlessClientState.decorate(state.displayName);
        }
    }
}
