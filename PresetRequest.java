package com.example.armorpresets;

/** Validated intent model for future NeoForge C2S payload handler. */
public record PresetRequest(Action action, int presetIndex) {
    public enum Action { SWAP, EXCHANGE_ALL }
    public PresetRequest {
        if (action == null) throw new IllegalArgumentException("Missing action");
        if (presetIndex < 0 || presetIndex >= PresetVault.PRESET_COUNT)
            throw new IllegalArgumentException("Invalid preset index");
    }
}
