package com.example.armorpresets;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Server-owned storage; caller must persist changes. Never expose mutable stacks. */
public final class PresetVault {
    public static final int PRESET_COUNT = 6;
    public static final int SLOTS_PER_PRESET = 6;
    private static final EquipmentSlot[] EQUIPMENT = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
        EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
    };
    private final ItemStack[][] presets = new ItemStack[PRESET_COUNT][SLOTS_PER_PRESET];
    private final String[] names = {"セット1", "セット2", "セット3", "セット4", "セット5", "セット6"};

    public PresetVault() {
        for (int p = 0; p < PRESET_COUNT; p++)
            for (int s = 0; s < SLOTS_PER_PRESET; s++) presets[p][s] = ItemStack.EMPTY;
    }
    private static void checkPreset(int p) {
        if (p < 0 || p >= PRESET_COUNT) throw new IllegalArgumentException("Invalid preset: " + p);
    }
    private static void checkSlot(int s) {
        if (s < 0 || s >= SLOTS_PER_PRESET) throw new IllegalArgumentException("Invalid slot: " + s);
    }
    public String getName(int p) { checkPreset(p); return names[p]; }
    public void setName(int p, String name) {
        checkPreset(p);
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Empty name");
        names[p] = name.length() > 32 ? name.substring(0, 32) : name;
    }
    public ItemStack getStored(int p, int s) {
        checkPreset(p); checkSlot(s);
        return presets[p][s].copy();
    }
    /** Restore only from trusted server-side saved data, not from client packets. */
    public void restoreSlot(int p, int s, ItemStack stack) {
        checkPreset(p); checkSlot(s);
        presets[p][s] = stack == null ? ItemStack.EMPTY : stack.copy();
    }
    /** Swap real stacks: no creative duplication, no item-id reconstruction. */
    public void swap(Player player, int p) {
        checkPreset(p);
        for (int s = 0; s < SLOTS_PER_PRESET; s++) exchangeSlot(player, p, s);
    }
    public void exchangeSlot(Player player, int p, int s) {
        checkPreset(p); checkSlot(s);
        EquipmentSlot equipment = EQUIPMENT[s];
        ItemStack worn = player.getItemBySlot(equipment).copy();
        ItemStack stored = presets[p][s];
        player.setItemSlot(equipment, stored.copy());
        presets[p][s] = worn;
        player.getInventory().setChanged();
    }
}
