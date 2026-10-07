package com.example.armorpresets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;

/** Only an index crosses the wire: never accept ItemStacks from the client. */
public record SwapPresetPayload(int index) implements CustomPacketPayload {
    public static final Type<SwapPresetPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ArmorPresetsMod.MOD_ID, "swap"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SwapPresetPayload> STREAM_CODEC =
        StreamCodec.composite(ByteBufCodecs.VAR_INT, SwapPresetPayload::index, SwapPresetPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(SwapPresetPayload message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (message.index() < 0 || message.index() >= PresetVault.PRESET_COUNT) return;
            VaultSavedData data = VaultSavedData.get(player);
            PresetService.apply(player, data.forPlayer(player.getUUID()),
                new PresetRequest(PresetRequest.Action.SWAP, message.index()), data::setDirty);
            PacketDistributor.sendToPlayer(player, PresetSnapshotPayload.from(data.forPlayer(player.getUUID())));
        });
    }
}
