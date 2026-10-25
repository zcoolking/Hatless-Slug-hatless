package dev.hatless.client;

import java.util.UUID;

/** Duck interface: lets us carry the player UUID through the render pipeline. */
public interface HatlessRenderState {
    UUID hatless$getPlayerId();

    void hatless$setPlayerId(UUID id);
}
