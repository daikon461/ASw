package com.example.armorpresets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestSnapshotPayload() implements CustomPacketPayload {
    public static final Type<RequestSnapshotPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ArmorPresetsMod.MOD_ID, "request_snapshot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestSnapshotPayload> STREAM_CODEC = StreamCodec.unit(new RequestSnapshotPayload());
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(RequestSnapshotPayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player)
                PacketDistributor.sendToPlayer(player, PresetSnapshotPayload.from(VaultSavedData.get(player).forPlayer(player.getUUID())));
        });
    }
}
