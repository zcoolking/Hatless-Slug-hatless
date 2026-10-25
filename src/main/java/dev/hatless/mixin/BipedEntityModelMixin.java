package dev.hatless.mixin;

import dev.hatless.HeadSettings;
import dev.hatless.client.HatlessClientState;
import dev.hatless.client.HatlessRenderState;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Applies head scale / visibility. Because the armor models are BipedEntityModels too,
 * the helmet (and skulls / pumpkins) follow the head automatically.
 */
@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin {

    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart hat;

    @Inject(
            method = "setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V",
            at = @At("TAIL"))
    private void hatless$applyHead(BipedEntityRenderState state, CallbackInfo ci) {
        UUID id = ((HatlessRenderState) state).hatless$getPlayerId();
        if (id == null) return; // not a player

        HeadSettings s = HatlessClientState.get(id);
        if (s == null) s = HeadSettings.DEFAULT; // player without the mod: always reset to vanilla

        hatless$apply(this.head, s);
        // In newer versions the hat layer is a child of the head and inherits its scale.
        // Only scale it separately if it is a standalone part.
        if (!this.head.hasChild("hat")) {
            hatless$apply(this.hat, s);
        }
    }

    private static void hatless$apply(ModelPart part, HeadSettings s) {
        part.xScale = s.scaleX();
        part.yScale = s.scaleY();
        part.zScale = s.scaleZ();
        part.visible = !s.hidden();
    }
}
