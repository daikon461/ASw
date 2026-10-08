package com.example.armorpresets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** Obsidian-and-gold themed, six-preset management screen. */
public final class PresetScreen extends Screen {
    private static final String[] PARTS = {"頭", "胴", "脚", "足", "主手", "副手"};
    private static final int GOLD = 0xFFE6BD72;
    private static final int MUTED = 0xFFB6BBC9;
    private static final int PANEL = 0xF21A202B;
    private int selected = 0;
    private EditBox rename;
    private final Button[] presetButtons = new Button[6];
    private int left, top;

    public PresetScreen() { super(Component.literal("装備プリセット管理")); }

    @Override protected void init() {
        super.init();
        left = width / 2 - 172;
        top = height / 2 - 120;
        for (int i = 0; i < 6; i++) {
            final int index = i;
            int x = left + 15 + (i % 3) * 106;
            int y = top + 47 + (i / 3) * 30;
            presetButtons[i] = addRenderableWidget(Button.builder(Component.literal("セット " + (i + 1)), b -> {
                selected = index;
                rename.setValue("");
            }).bounds(x, y, 100, 23).build());
        }
        for (int j = 0; j < 6; j++) {
            final int slot = j;
            int x = left + 15 + (j % 3) * 106;
            int y = top + 142 + (j / 3) * 28;
            addRenderableWidget(Button.builder(Component.literal(PARTS[j] + " を交換"), b -> {
                PacketDistributor.sendToServer(new EditPresetPayload(selected, slot, ""));
                PacketDistributor.sendToServer(new RequestSnapshotPayload());
            }).bounds(x, y, 100, 22).build());
        }
        rename = new EditBox(font, left + 15, top + 211, 172, 20, Component.literal("プリセット名"));
        rename.setMaxLength(32);
        rename.setHint(Component.literal("新しいセット名を入力"));
        addRenderableWidget(rename);
        addRenderableWidget(Button.builder(Component.literal("名前を保存"), b -> {
            String name = rename.getValue().strip();
            if (!name.isEmpty()) {
                PacketDistributor.sendToServer(new EditPresetPayload(selected, -1, name));
                PacketDistributor.sendToServer(new RequestSnapshotPayload());
            }
        }).bounds(left + 193, top + 211, 136, 20).build());
        addRenderableWidget(Button.builder(Component.literal("◆ 選択したセットを装備 ◆"), b -> {
            PacketDistributor.sendToServer(new SwapPresetPayload(selected));
            PacketDistributor.sendToServer(new RequestSnapshotPayload());
        }).bounds(left + 15, top + 238, 314, 22).build());
        PacketDistributor.sendToServer(new RequestSnapshotPayload());
    }

    @Override public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fillGradient(0, 0, width, height, 0xDF080C15, 0xED151A27);
    }

    private void frame(GuiGraphics g, int x, int y, int w, int h, int fill, int edge) {
        g.fill(x, y, x + w, y + h, edge);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        // Paint decorative elements before widgets so text and buttons remain readable.
        renderBackground(g, mouseX, mouseY, delta);
        frame(g, left, top, 344, 278, PANEL, 0xFFAD8952);
        g.fillGradient(left + 2, top + 2, left + 342, top + 34, 0xFF343344, 0xFF222A38);
        g.drawCenteredString(font, "✦  装備プリセット管理  ✦", width / 2, top + 12, GOLD);
        g.drawString(font, "装備セットを選択", left + 15, top + 37, MUTED);
        PresetSnapshotPayload snap = ClientSnapshot.get();
        boolean valid = snap != null && snap.names().size() == 6;
        for (int i = 0; i < 6; i++) {
            int x = left + 12 + (i % 3) * 106;
            int y = top + 44 + (i / 3) * 30;
            frame(g, x, y, 106, 29, i == selected ? 0xFF5C4932 : 0xFF242D3C,
                i == selected ? GOLD : 0xFF536071);
            String name = valid ? snap.names().get(i) : "セット " + (i + 1);
            presetButtons[i].setMessage(Component.literal((i == selected ? "◆ " : "") + name));
        }
        g.fill(left + 14, top + 110, left + 330, top + 111, 0xFF826A49);
        String name = valid ? snap.names().get(selected) : "セット " + (selected + 1);
        g.drawString(font, "選択中：" + name, left + 15, top + 119, GOLD);
        int filled = 0;
        if (snap != null && snap.occupied().size() == 36) {
            for (int j = 0; j < 6; j++) filled += snap.occupied().get(selected * 6 + j);
        }
        g.drawString(font, "保管 " + filled + "/6", left + 270, top + 119, MUTED);
        for (int j = 0; j < 6; j++) {
            int x = left + 12 + (j % 3) * 106;
            int y = top + 139 + (j / 3) * 28;
            frame(g, x, y, 106, 27, 0xFF242D3C, 0xFF566171);
        }
        g.drawString(font, "プリセット名", left + 15, top + 200, MUTED);
        g.fill(left + 14, top + 234, left + 330, top + 235, 0xFF826A49);
        g.drawCenteredString(font, "Ctrl + G : 管理画面  /  テンキー1～6 : 即時切替", width / 2, top + 265, MUTED);
        super.render(g, mouseX, mouseY, delta);
        if (snap != null && snap.items().size() == 36) {
            for (int j = 0; j < 6; j++) {
                var stack = snap.items().get(selected * 6 + j);
                if (stack.isEmpty()) continue;
                int x = left + 12 + (j % 3) * 106 + 83;
                int y = top + 142 + (j / 3) * 28 + 3;
                g.renderItem(stack, x, y);
                if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16)
                    g.renderTooltip(font, stack, mouseX, mouseY);
            }
        }
    }
}
