package com.example.armorpresets;

import net.minecraft.server.level.ServerPlayer;

/** Invoke exclusively on the server main thread; persistence must run after mutation. */
public final class PresetService {
    private PresetService() {}
    public static void apply(ServerPlayer player, PresetVault vault, PresetRequest request, Runnable save) {
        if (player == null || vault == null || request == null || save == null)
            throw new IllegalArgumentException("Missing server state");
        // Both actions exchange equipped stacks with a selected vault set.
        // The GUI can later offer per-slot exchange using exchangeSlot().
        vault.swap(player, request.presetIndex());
        save.run();
        player.containerMenu.broadcastChanges();
    }
}
