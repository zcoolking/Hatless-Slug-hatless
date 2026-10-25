package dev.hatless.client;

import dev.hatless.HatlessPayloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class HatlessClient implements ClientModInitializer {

    /**
     * Default key: the physical key that is labelled "+" on a German keyboard
     * (GLFW calls it RIGHT_BRACKET). Can be rebound in the controls menu.
     */
    private static KeyBinding openKey;

    @Override
    public void onInitializeClient() {
        HatlessClientState.load();

        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hatless.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_BRACKET,
                KeyBinding.Category.create(Identifier.of("hatless", "main"))));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) {
                if (client.currentScreen == null && client.player != null) {
                    client.setScreen(new HeadScreen());
                }
            }
            HatlessRelay.tick(client);
        });

        // Receive settings of other players through a server that has Hatless.
        ClientPlayNetworking.registerGlobalReceiver(HatlessPayloads.Sync.ID,
                (payload, context) -> HatlessClientState.applySync(payload));

        // Announce ourselves when joining (server and web relay), forget everybody when leaving.
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            HatlessClientState.sendToServer();
            HatlessRelay.requestPush();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> HatlessClientState.clearRemote());
    }
}
