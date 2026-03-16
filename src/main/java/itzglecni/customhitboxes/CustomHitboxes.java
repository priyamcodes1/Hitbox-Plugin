package itzglecni.customhitboxes;

import itzglecni.customhitboxes.config.ConfigManager;
import itzglecni.customhitboxes.gui.CustomConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class CustomHitboxes implements ClientModInitializer {
    private static final Category KEY_CATEGORY = Category.create(Identifier.of("customhitboxes", "main"));
    private static final String OPEN_GUI_KEY = "key.customhitboxes.open_config";

    private static KeyBinding openConfigKey;

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            OPEN_GUI_KEY,
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            KEY_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        System.out.println("[Hitbox Plus] Initialized ItzGlecni's Client-Side Hitbox Rendering Engine v1.0.0.");
    }

    private void onClientTick(MinecraftClient client) {
        while (openConfigKey.wasPressed()) {
            if (!ConfigManager.getConfig().openConfigHotkeyEnabled) {
                continue;
            }

            client.setScreen(new CustomConfigScreen(client.currentScreen));
        }
    }
}