package dev.hatless.mixin;

import dev.hatless.client.HatlessClientState;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {

    /** Adds the Hatless symbol in the tab list. */
    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
    private void hatless$mark(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        if (HatlessClientState.hasMod(entry.getProfile().id())) {
            cir.setReturnValue(HatlessClientState.decorate(cir.getReturnValue()));
        }
    }
}
