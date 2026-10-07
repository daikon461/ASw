package com.example.armorpresets;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientSnapshot {
    private ClientSnapshot() {}
    private static PresetSnapshotPayload latest;
    public static void accept(PresetSnapshotPayload payload) { latest = payload; }
    public static PresetSnapshotPayload get() { return latest; }
    public static void clear() { latest = null; }
}
