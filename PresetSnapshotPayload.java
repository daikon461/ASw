package com.example.armorpresets;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.api.distmarker.Dist;

/** Read-only snapshot. No items are sent to the server by the client. */
public record PresetSnapshotPayload(List<String> names, List<Integer> occupied, List<ItemStack> items) implements CustomPacketPayload {
    public static final Type<PresetSnapshotPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ArmorPresetsMod.MOD_ID, "snapshot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PresetSnapshotPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.stringUtf8(32).apply(ByteBufCodecs.list(6)), PresetSnapshotPayload::names,
        ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list(36)), PresetSnapshotPayload::occupied,
        ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(36)), PresetSnapshotPayload::items,
        PresetSnapshotPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static PresetSnapshotPayload from(PresetVault vault) {
        List<String> names = new ArrayList<>();
        List<Integer> slots = new ArrayList<>();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            names.add(vault.getName(i));
            for (int j = 0; j < 6; j++) {
                ItemStack stack = vault.getStored(i, j);
                slots.add(stack.isEmpty() ? 0 : 1);
                items.add(stack);
            }
        }
        return new PresetSnapshotPayload(names, slots, items);
    }
    public static void handle(PresetSnapshotPayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientSnapshot.accept(msg));
    }
}
