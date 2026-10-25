package dev.hatless;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Common entrypoint. Runs on dedicated servers, LAN hosts and in singleplayer.
 * The server only relays settings between players that have the mod installed.
 */
public class HatlessMod implements ModInitializer {

    public static final String MOD_ID = "hatless";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Server-side: all players that announced themselves with the mod. */
    private static final Map<UUID, HeadSettings> SERVER_STATE = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(HatlessPayloads.Update.ID, HatlessPayloads.Update.CODEC);
        PayloadTypeRegistry.playS2C().register(HatlessPayloads.Sync.ID, HatlessPayloads.Sync.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(HatlessPayloads.Update.ID, (payload, context) -> {
            ServerPlayerEntity sender = context.player();
            // The record constructor clamps all values, so malicious clients cannot send absurd sizes.
            HeadSettings settings = new HeadSettings(payload.hidden(), payload.size(), payload.width());
            boolean firstTime = SERVER_STATE.put(sender.getUuid(), settings) == null;

            HatlessPayloads.Sync msg = new HatlessPayloads.Sync(
                    sender.getUuid(), true, settings.hidden(), settings.size(), settings.width());

            for (ServerPlayerEntity other : context.server().getPlayerManager().getPlayerList()) {
                if (other == sender || !SERVER_STATE.containsKey(other.getUuid())) continue;
                ServerPlayNetworking.send(other, msg);
            }

            // A player who just announced themselves needs everybody else's current settings.
            if (firstTime) {
                for (Map.Entry<UUID, HeadSettings> e : SERVER_STATE.entrySet()) {
                    if (e.getKey().equals(sender.getUuid())) continue;
                    HeadSettings s = e.getValue();
                    ServerPlayNetworking.send(sender,
                            new HatlessPayloads.Sync(e.getKey(), true, s.hidden(), s.size(), s.width()));
                }
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID id = handler.getPlayer().getUuid();
            if (SERVER_STATE.remove(id) == null) return;
            HatlessPayloads.Sync gone = new HatlessPayloads.Sync(id, false, false, 1.0f, 1.0f);
            for (ServerPlayerEntity other : server.getPlayerManager().getPlayerList()) {
                if (other.getUuid().equals(id) || !SERVER_STATE.containsKey(other.getUuid())) continue;
                ServerPlayNetworking.send(other, gone);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> SERVER_STATE.clear());

        LOGGER.info("Hatless loaded.");
    }
}
