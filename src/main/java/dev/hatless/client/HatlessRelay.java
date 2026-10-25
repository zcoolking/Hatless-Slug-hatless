package dev.hatless.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.hatless.HatlessMod;
import dev.hatless.HeadSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Talks to hatless.php on the website: pushes our settings and polls the settings of
 * the players around us. Works on any server, with or without Hatless installed there.
 */
public final class HatlessRelay {

    private static final int POLL_EVERY_TICKS = 100; // 5 seconds
    private static final int MAX_LOOKUP = 60;

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static final AtomicBoolean POLLING = new AtomicBoolean(false);
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();
    private static volatile boolean pendingPush = false;
    private static int ticks = 0;

    private HatlessRelay() {}

    private static boolean enabled() {
        String u = HatlessClientState.relayUrl();
        return u != null && !u.isBlank() && !u.contains("YOUR-SITE");
    }

    /** Asks for our settings to be uploaded as soon as the player exists in a world. */
    public static void requestPush() {
        pendingPush = true;
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null || !enabled()) return;
        if (pendingPush) {
            pendingPush = false;
            push();
        }
        if (++ticks % POLL_EVERY_TICKS == 0) poll(client);
    }

    private static String url(String action, String extra) {
        // "i=1" is a commonly reported way to skip InfinityFree's browser check for API calls.
        return HatlessClientState.relayUrl() + "?action=" + action + extra + "&i=1";
    }

    private static void warnOnce(String key, String message) {
        if (WARNED.add(key)) HatlessMod.LOGGER.warn("[Hatless relay] {}", message);
    }

    private static void push() {
        UUID me = HatlessClientState.localUuid();
        if (me == null) return;
        HeadSettings s = HatlessClientState.local();
        JsonObject o = new JsonObject();
        o.addProperty("uuid", me.toString());
        o.addProperty("token", HatlessClientState.token());
        o.addProperty("hidden", s.hidden());
        o.addProperty("size", s.size());
        o.addProperty("width", s.width());
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url("set", "")))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Hatless/1.1")
                    .POST(HttpRequest.BodyPublishers.ofString(o.toString()))
                    .build();
            HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(r -> checkResponse("set", r))
                    .exceptionally(e -> {
                        warnOnce("push-fail", "Could not reach the relay: " + e.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            warnOnce("push-bad", "Invalid relayUrl in hatless.json: " + e.getMessage());
        }
    }

    private static boolean checkResponse(String what, HttpResponse<String> r) {
        String body = r.body() == null ? "" : r.body().stripLeading();
        if (body.startsWith("<")) {
            warnOnce("html", "The relay returned a web page instead of JSON (host bot protection?). Check relayUrl.");
            return false;
        }
        if (r.statusCode() != 200) {
            warnOnce("status-" + what + "-" + r.statusCode(), "Relay answered " + r.statusCode() + " for " + what + ": " + body);
            return false;
        }
        return true;
    }

    private static void poll(MinecraftClient client) {
        UUID me = HatlessClientState.localUuid();
        List<UUID> asked = new ArrayList<>();
        for (PlayerEntity p : client.world.getPlayers()) {
            if (!p.getUuid().equals(me) && asked.size() < MAX_LOOKUP) asked.add(p.getUuid());
        }
        if (asked.isEmpty() || !POLLING.compareAndSet(false, true)) return;

        StringBuilder ids = new StringBuilder();
        for (UUID id : asked) {
            if (ids.length() > 0) ids.append(',');
            ids.append(id);
        }
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url("get", "&uuids=" + ids)))
                    .timeout(Duration.ofSeconds(8))
                    .header("User-Agent", "Hatless/1.1")
                    .GET()
                    .build();
            HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(r -> {
                        try {
                            if (checkResponse("get", r)) apply(r.body(), asked);
                        } finally {
                            POLLING.set(false);
                        }
                    })
                    .exceptionally(e -> {
                        POLLING.set(false);
                        warnOnce("poll-fail", "Could not reach the relay: " + e.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            POLLING.set(false);
            warnOnce("poll-bad", "Invalid relayUrl in hatless.json: " + e.getMessage());
        }
    }

    private static void apply(String body, List<UUID> asked) {
        try {
            JsonObject root = JsonParser.parseString(body).getAsJsonObject();
            JsonObject players = root.has("players") && root.get("players").isJsonObject()
                    ? root.getAsJsonObject("players") : new JsonObject();
            for (UUID id : asked) {
                String key = id.toString().replace("-", "");
                if (players.has(key) && players.get(key).isJsonObject()) {
                    JsonObject p = players.getAsJsonObject(key);
                    HatlessClientState.putWeb(id, new HeadSettings(
                            p.has("hidden") && p.get("hidden").getAsBoolean(),
                            p.has("size") ? p.get("size").getAsFloat() : 1.0f,
                            p.has("width") ? p.get("width").getAsFloat() : 1.0f));
                } else {
                    HatlessClientState.removeWeb(id);
                }
            }
        } catch (Exception e) {
            warnOnce("parse", "Could not read the relay answer: " + e.getMessage());
        }
    }
}
