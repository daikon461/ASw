package com.example.armorpresets;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

/** Server-only world SavedData. No equipment stacks are accepted from clients. */
public final class VaultSavedData extends SavedData {
    private static final String ID = "armorpresets_vaults";
    private final Map<UUID, PresetVault> vaults = new HashMap<>();

    public static VaultSavedData get(ServerPlayer player) {
        return player.server.overworld().getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(VaultSavedData::new, VaultSavedData::load), ID);
    }

    public PresetVault forPlayer(UUID id) {
        return vaults.computeIfAbsent(id, unused -> new PresetVault());
    }

    private static VaultSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        VaultSavedData data = new VaultSavedData();
        ListTag players = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < players.size(); i++) {
            CompoundTag entry = players.getCompound(i);
            if (!entry.hasUUID("Id")) continue;
            PresetVault vault = new PresetVault();
            ListTag sets = entry.getList("Sets", Tag.TAG_COMPOUND);
            for (int j = 0; j < sets.size(); j++) {
                CompoundTag set = sets.getCompound(j);
                int index = set.getInt("Index");
                if (index < 0 || index >= PresetVault.PRESET_COUNT) continue;
                if (set.contains("Name", Tag.TAG_STRING) && !set.getString("Name").isBlank())
                    vault.setName(index, set.getString("Name"));
                ListTag slots = set.getList("Slots", Tag.TAG_COMPOUND);
                for (int k = 0; k < slots.size(); k++) {
                    CompoundTag slot = slots.getCompound(k);
                    int s = slot.getInt("Index");
                    if (s < 0 || s >= PresetVault.SLOTS_PER_PRESET) continue;
                    vault.restoreSlot(index, s, ItemStack.parseOptional(registries, slot.getCompound("Stack")));
                }
            }
            data.vaults.put(entry.getUUID("Id"), vault);
        }
        return data;
    }

    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag players = new ListTag();
        for (var playerEntry : vaults.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Id", playerEntry.getKey());
            ListTag sets = new ListTag();
            PresetVault vault = playerEntry.getValue();
            for (int i = 0; i < PresetVault.PRESET_COUNT; i++) {
                CompoundTag set = new CompoundTag();
                set.putInt("Index", i);
                set.putString("Name", vault.getName(i));
                ListTag slots = new ListTag();
                for (int s = 0; s < PresetVault.SLOTS_PER_PRESET; s++) {
                    ItemStack stack = vault.getStored(i, s);
                    if (stack.isEmpty()) continue;
                    CompoundTag slot = new CompoundTag();
                    slot.putInt("Index", s);
                    slot.put("Stack", stack.save(registries));
                    slots.add(slot);
                }
                set.put("Slots", slots);
                sets.add(set);
            }
            entry.put("Sets", sets);
            players.add(entry);
        }
        tag.put("Players", players);
        return tag;
    }
}
