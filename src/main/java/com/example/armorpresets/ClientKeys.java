package com.example.armorpresets;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Modifier is checked at dispatch: Left Alt + G / 1..6. */
public final class ClientKeys {
    private ClientKeys() {}
    public static final KeyMapping OPEN = new KeyMapping("key.armorpresets.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.armorpresets");
    public static final KeyMapping[] SWAP = new KeyMapping[6];
    static {
        for (int i = 0; i < 6; i++) SWAP[i] = new KeyMapping("key.armorpresets.swap" + (i + 1), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_1 + i, "key.categories.armorpresets");
    }
    @EventBusSubscriber(modid = ArmorPresetsMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent public static void keys(RegisterKeyMappingsEvent event) {
            event.register(OPEN);
            for (KeyMapping key : SWAP) event.register(key);
        }
    }
    @EventBusSubscriber(modid = ArmorPresetsMod.MOD_ID, value = Dist.CLIENT)
    public static final class Events {
        @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.screen != null) return;
            boolean alt = InputConstants.isKeyDown(mc.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_ALT);
            while (OPEN.consumeClick()) {
                if (alt) mc.setScreen(new PresetScreen());
            }
            for (int i = 0; i < SWAP.length; i++) {
                while (SWAP[i].consumeClick()) if (alt) PacketDistributor.sendToServer(new SwapPresetPayload(i));
            }
        }
    }
}
