package dev.hatless.mixin;

import dev.hatless.client.HatlessRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements HatlessRenderState {
    @Unique
    private UUID hatless$playerId;

    @Override
    public UUID hatless$getPlayerId() {
        return hatless$playerId;
    }

    @Override
    public void hatless$setPlayerId(UUID id) {
        this.hatless$playerId = id;
    }
}
