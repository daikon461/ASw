package com.example.armorpresets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;

/** The client sends only an operation and a bounded name, never inventory contents. */
public record EditPresetPayload(int index, int slot, String name) implements CustomPacketPayload {
    public static final Type<EditPresetPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ArmorPresetsMod.MOD_ID, "edit"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EditPresetPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, EditPresetPayload::index,
        ByteBufCodecs.VAR_INT, EditPresetPayload::slot,
        ByteBufCodecs.stringUtf8(64), EditPresetPayload::name,
        EditPresetPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(EditPresetPayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (msg.index < 0 || msg.index >= 6) return;
            VaultSavedData data = VaultSavedData.get(player);
            PresetVault vault = data.forPlayer(player.getUUID());
            if (msg.slot == -1) {
                String clean = msg.name.strip();
                if (clean.isBlank() || clean.length() > 32) return;
                vault.setName(msg.index, clean);
            } else if (msg.slot >= 0 && msg.slot < 6) {
                vault.exchangeSlot(player, msg.index, msg.slot);
                player.containerMenu.broadcastChanges();
            } else return;
            data.setDirty();
            PacketDistributor.sendToPlayer(player, PresetSnapshotPayload.from(vault));
        });
    }
}
