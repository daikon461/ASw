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
    private int activePreset = -1;
    public int getActivePreset() { return activePreset; }
    public void restoreActivePreset(int value) { activePreset = value >= 0 && value < PRESET_COUNT ? value : -1; }
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
    /** Returns equipped items to their original preset, never to the destination preset. */
    public boolean swap(Player player, int target) {
        checkPreset(target);
        if (activePreset == target) return true;
        // On first use, currently equipped items have no known home. Move them to
        // inventory, refusing the operation if there is insufficient empty space.
        if (activePreset == -1) {
            int occupied = 0;
            for (EquipmentSlot slot : EQUIPMENT)
                if (!player.getItemBySlot(slot).isEmpty()) occupied++;
            int free = 0;
            for (int i = 0; i < player.getInventory().items.size(); i++)
                if (player.getInventory().items.get(i).isEmpty()) free++;
            // Main hand is a hotbar slot, which becomes free when its item is removed.
            if (!player.getMainHandItem().isEmpty()) free++;
            if (free < occupied) return false;
            ItemStack[] previous = new ItemStack[SLOTS_PER_PRESET];
            for (int i = 0; i < SLOTS_PER_PRESET; i++) {
                previous[i] = player.getItemBySlot(EQUIPMENT[i]).copy();
                player.setItemSlot(EQUIPMENT[i], ItemStack.EMPTY);
            }
            for (ItemStack stack : previous)
                if (!stack.isEmpty()) player.getInventory().add(stack);
        } else {
            // First return the currently worn set to its own reserved slots.
            for (int i = 0; i < SLOTS_PER_PRESET; i++)
                presets[activePreset][i] = player.getItemBySlot(EQUIPMENT[i]).copy();
        }
        for (int i = 0; i < SLOTS_PER_PRESET; i++) {
            player.setItemSlot(EQUIPMENT[i], presets[target][i].copy());
            presets[target][i] = ItemStack.EMPTY;
        }
        activePreset = target;
        player.getInventory().setChanged();
        return true;
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
