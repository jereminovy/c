package pl.soudim.autorepairminer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AutoRepairMinerClient implements ClientModInitializer {
    private static final long REPAIR_INTERVAL_MS = 5L * 60L * 1000L;

    private KeyBinding toggleKey;
    private boolean miningEnabled = false;
    private long nextRepairAt = 0L;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autorepairminer.toggle",
                GLFW.GLFW_KEY_K,
                "category.autorepairminer"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        while (toggleKey.wasPressed()) {
            miningEnabled = !miningEnabled;

            if (miningEnabled) {
                nextRepairAt = System.currentTimeMillis() + REPAIR_INTERVAL_MS;
            } else {
                client.options.attackKey.setPressed(false);
                client.options.useKey.setPressed(false);
            }

            if (client.player != null) {
                String status = miningEnabled ? "§aAUTO KOPANIE: WŁĄCZONE" : "§cAUTO KOPANIE: WYŁĄCZONE";
                client.player.sendMessage(Text.literal(status), true);
            }
        }

        if (!miningEnabled || client.player == null || client.world == null) {
            return;
        }

        // Kopie stale LPM i PPM podczas działania moda.
        client.options.attackKey.setPressed(true);
        client.options.useKey.setPressed(true);

        long now = System.currentTimeMillis();
        if (now >= nextRepairAt) {
            client.player.networkHandler.sendChatCommand("repair");
            nextRepairAt = now + REPAIR_INTERVAL_MS;
        }
    }
}
