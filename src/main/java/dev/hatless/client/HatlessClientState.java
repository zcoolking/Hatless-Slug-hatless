package dev.hatless.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.hatless.HatlessMod;
import dev.hatless.HatlessPayloads;
import dev.hatless.HeadSettings;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side storage: own settings (saved to config), settings of other Hatless players
 * that arrived through the server (REMOTE) and through the web relay (WEB).
 */
public final class HatlessClientState {

    /** Shown next to the names of players who have the mod. */
    public static final String SYMBOL = "\u263B";

    /**
     * Address of hatless.php on your website (no query string).
     * Players can override it in config/hatless.json ("relayUrl"); an empty value turns the relay off.
     * CHANGE THIS to your own site before publishing the mod.
     */
    public static final String DEFAULT_RELAY_URL = "https://YOUR-SITE.infinityfreeapp.com/api/hatless.php";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG = FabricLoader.getInstance().getConfigDir().resolve("hatless.json");
    private static final Map<UUID, HeadSettings> REMOTE = new ConcurrentHashMap<>();
    private static final Map<UUID, HeadSettings> WEB = new ConcurrentHashMap<>();
    private static volatile HeadSettings local = HeadSettings.DEFAULT;
    private static volatile String relayUrl = DEFAULT_RELAY_URL;
    private static volatile String token = "";

    private HatlessClientState() {}

    // ---------- own settings ----------

    public static HeadSettings local() {
        return local;
    }

    public static void setLocal(HeadSettings settings) {
        local = settings;
    }

    public static String relayUrl() {
        return relayUrl;
    }

    public static String token() {
        return token;
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG)) {
                JsonObject o = GSON.fromJson(Files.readString(CONFIG), JsonObject.class);
                local = new HeadSettings(
                        o.has("hidden") && o.get("hidden").getAsBoolean(),
                        o.has("size") ? o.get("size").getAsFloat() : 1.0f,
                        o.has("width") ? o.get("width").getAsFloat() : 1.0f);
                if (o.has("relayUrl")) relayUrl = o.get("relayUrl").getAsString().trim();
                if (o.has("token")) token = o.get("token").getAsString();
            }
        } catch (Exception e) {
            HatlessMod.LOGGER.warn("Could not read hatless.json, using defaults", e);
        }
        if (token == null || token.length() < 16) token = newToken();
        save(); // writes new keys (relayUrl, token) into older config files
    }

    public static void save() {
        try {
            JsonObject o = new JsonObject();
            o.addProperty("hidden", local.hidden());
            o.addProperty("size", local.size());
            o.addProperty("width", local.width());
            o.addProperty("relayUrl", relayUrl);
            o.addProperty("token", token);
            Files.writeString(CONFIG, GSON.toJson(o));
        } catch (Exception e) {
            HatlessMod.LOGGER.warn("Could not save hatless.json", e);
        }
    }

    private static String newToken() {
        byte[] b = new byte[16];
        new SecureRandom().nextBytes(b);
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    /** Sends our settings to the server (if the server has Hatless installed). */
    public static void sendToServer() {
        if (!ClientPlayNetworking.canSend(HatlessPayloads.Update.ID)) return;
        ClientPlayNetworking.send(new HatlessPayloads.Update(local.hidden(), local.size(), local.width()));
    }

    // ---------- other players ----------

    public static void applySync(HatlessPayloads.Sync p) {
        if (p.present()) {
            REMOTE.put(p.player(), new HeadSettings(p.hidden(), p.size(), p.width()));
        } else {
            REMOTE.remove(p.player());
        }
    }

    public static void putWeb(UUID id, HeadSettings s) {
        WEB.put(id, s);
    }

    public static void removeWeb(UUID id) {
        WEB.remove(id);
    }

    public static void clearRemote() {
        REMOTE.clear();
        WEB.clear();
    }

    // ---------- lookups used by the mixins ----------

    public static UUID localUuid() {
        ClientPlayerEntity me = MinecraftClient.getInstance().player;
        return me == null ? null : me.getUuid();
    }

    private static boolean isLocal(UUID id) {
        UUID me = localUuid();
        return me != null && me.equals(id);
    }

    /** Returns the settings to render for this player, or null if the player does not have the mod. */
    public static HeadSettings get(UUID id) {
        if (isLocal(id)) return local;
        HeadSettings s = REMOTE.get(id); // live data from the server wins
        return s != null ? s : WEB.get(id);
    }

    public static boolean hasMod(UUID id) {
        return isLocal(id) || REMOTE.containsKey(id) || WEB.containsKey(id);
    }

    public static Text decorate(Text name) {
        return Text.literal(SYMBOL + " ").formatted(Formatting.AQUA).append(name);
    }
}
