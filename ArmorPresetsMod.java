package com.example.armorpresets;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(ArmorPresetsMod.MOD_ID)
public final class ArmorPresetsMod {
    public static final String MOD_ID = "armorpresets";
    public ArmorPresetsMod(IEventBus modBus) {
        modBus.addListener(this::registerPayloads);
    }
    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(SwapPresetPayload.TYPE, SwapPresetPayload.STREAM_CODEC, SwapPresetPayload::handle);
        registrar.playToServer(EditPresetPayload.TYPE, EditPresetPayload.STREAM_CODEC, EditPresetPayload::handle);
        registrar.playToServer(RequestSnapshotPayload.TYPE, RequestSnapshotPayload.STREAM_CODEC, RequestSnapshotPayload::handle);
        registrar.playToClient(PresetSnapshotPayload.TYPE, PresetSnapshotPayload.STREAM_CODEC, PresetSnapshotPayload::handle);
    }
}
