package com.example.armorpresets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** Six presets, per-slot exchange, and rename. Item changes are server-authoritative. */
public final class PresetScreen extends Screen {
    private static final String[] PARTS = {"頭", "胴", "脚", "足", "主手", "副手"};
    private int selected = 0;
    private EditBox rename;
    private final Button[] presetButtons = new Button[6];
    public PresetScreen() { super(Component.literal("装備プリセット管理")); }
    @Override protected void init() {
        super.init();
        int left = width / 2 - 153;
        int top = height / 2 - 87;
        for (int i = 0; i < 6; i++) {
            final int idx = i;
            presetButtons[i] = addRenderableWidget(Button.builder(Component.literal("セット" + (i+1)), b -> {
                selected = idx;
                rebuildWidgets();
            }).bounds(left + (i%3)*104, top + (i/3)*24, 100, 20).build());
        }
        for (int j = 0; j < 6; j++) {
            final int slot = j;
            addRenderableWidget(Button.builder(Component.literal(PARTS[j] + " を交換"), b -> {
                PacketDistributor.sendToServer(new EditPresetPayload(selected, slot, ""));
            }).bounds(left + (j%3)*104, top + 55 + (j/3)*24, 100, 20).build());
        }
        rename = new EditBox(font, left, top + 112, 180, 20, Component.literal("セット名"));
        rename.setMaxLength(32);
        addRenderableWidget(rename);
        addRenderableWidget(Button.builder(Component.literal("名前を保存"), b -> {
            String name = rename.getValue().strip();
            if (!name.isEmpty()) PacketDistributor.sendToServer(new EditPresetPayload(selected, -1, name));
        }).bounds(left + 185, top + 112, 123, 20).build());
        addRenderableWidget(Button.builder(Component.literal("選択セットを装備"), b -> {
            PacketDistributor.sendToServer(new SwapPresetPayload(selected));
        }).bounds(left, top + 139, 308, 20).build());
        PacketDistributor.sendToServer(new RequestSnapshotPayload());
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        int left = width / 2 - 153;
        int top = height / 2 - 87;
        graphics.drawCenteredString(font, title, width / 2, top - 20, 0xFFFFFF);
        PresetSnapshotPayload snap = ClientSnapshot.get();
        String name = snap != null && snap.names().size() == 6 ? snap.names().get(selected) : "セット" + (selected + 1);
        graphics.drawString(font, "選択中: " + name, left, top + 101, 0xFFFFFF);
        if (snap != null && snap.names().size() == 6) {
            for (int i = 0; i < 6; i++) {
                String label = (i == selected ? "▶ " : "") + snap.names().get(i);
                presetButtons[i].setMessage(Component.literal(label));
            }
        }
        if (snap != null && snap.items().size() == 36) {
            for (int j = 0; j < 6; j++) {
                var stack = snap.items().get(selected * 6 + j);
                if (!stack.isEmpty()) {
                    int x = left + (j % 3) * 104 + 78;
                    int y = top + 57 + (j / 3) * 24;
                    graphics.renderItem(stack, x, y);
                    if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16)
                        graphics.renderTooltip(font, stack, mouseX, mouseY);
                }
            }
        }
        if (snap != null && snap.occupied().size() == 36) {
            int filled = 0;
            for (int i = 0; i < 6; i++) filled += snap.occupied().get(selected*6+i);
            graphics.drawString(font, "保管中: " + filled + "/6 部位", left + 196, top + 101, 0xAAAAAA);
        }
        graphics.drawCenteredString(font, "左Alt + 1～6: 切替 / 左Alt + G: 開く", width / 2, top + 166, 0xCCCCCC);
    }
}
