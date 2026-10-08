package com.example.armorpresets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/** Large two-column fantasy vault UI. All item mutations are server-authoritative. */
public final class PresetScreen extends Screen {
    private static final String[] PARTS = {"頭", "胴", "脚", "足", "主手", "副手"};
    private static final int GOLD = 0xFFF1C879, MUTED = 0xFFBBC2CE;
    private static final int EDGE = 0xFF9B7843, PANEL = 0xF51B2230;
    private int selected = 0;
    private int left, top;
    private EditBox rename;
    private final Button[] presets = new Button[6];

    public PresetScreen() { super(Component.literal("装備プリセット保管庫")); }

    private void refresh() { PacketDistributor.sendToServer(new RequestSnapshotPayload()); }

    @Override protected void init() {
        super.init();
        left = (width - 466) / 2;
        top = (height - 310) / 2;
        for (int i = 0; i < 6; i++) {
            final int index = i;
            int x = left + 20, y = top + 65 + i * 32;
            presets[i] = addRenderableWidget(Button.builder(Component.literal("セット " + (i + 1)), b -> {
                selected = index;
                rename.setValue("");
            }).bounds(x, y, 139, 25).build());
        }
        for (int j = 0; j < 6; j++) {
            final int slot = j;
            int x = left + 186 + (j % 3) * 86;
            int y = top + 109 + (j / 3) * 57;
            addRenderableWidget(Button.builder(Component.literal("交換"), b -> {
                PacketDistributor.sendToServer(new EditPresetPayload(selected, slot, ""));
                refresh();
            }).bounds(x + 3, y + 29, 76, 18).build());
        }
        rename = new EditBox(font, left + 187, top + 229, 150, 20, Component.literal("プリセット名"));
        rename.setMaxLength(32);
        rename.setHint(Component.literal("名前を入力"));
        addRenderableWidget(rename);
        addRenderableWidget(Button.builder(Component.literal("保存"), b -> {
            String name = rename.getValue().strip();
            if (!name.isEmpty()) {
                PacketDistributor.sendToServer(new EditPresetPayload(selected, -1, name));
                refresh();
            }
        }).bounds(left + 344, top + 229, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("✦ 選択したセットを装備 ✦"), b -> {
            PacketDistributor.sendToServer(new SwapPresetPayload(selected));
            refresh();
        }).bounds(left + 185, top + 259, 259, 24).build());
        refresh();
    }

    @Override public void renderBackground(GuiGraphics g, int mx, int my, float tick) {
        g.fillGradient(0, 0, width, height, 0xF0060A12, 0xF0192130);
    }

    private void frame(GuiGraphics g, int x, int y, int w, int h, int fill, int border) {
        g.fill(x, y, x + w, y + h, border);
        g.fill(x + 2, y + 2, x + w - 2, y + h - 2, fill);
    }

    @Override public void render(GuiGraphics g, int mx, int my, float tick) {
        renderBackground(g, mx, my, tick);
        frame(g, left, top, 466, 310, PANEL, EDGE);
        frame(g, left + 7, top + 7, 452, 296, 0xFF161C28, 0xFF4E4538);
        g.fillGradient(left + 10, top + 10, left + 456, top + 49, 0xFF423528, 0xFF202D3E);
        g.drawCenteredString(font, "✦  装 備 プ リ セ ッ ト 保 管 庫  ✦", width / 2, top + 19, GOLD);
        g.drawString(font, "MY SETS / 登録セット", left + 22, top + 52, MUTED);
        g.drawString(font, "EQUIPMENT / 装備詳細", left + 187, top + 52, MUTED);
        frame(g, left + 13, top + 61, 153, 199, 0xFF222B39, 0xFF6A563B);
        frame(g, left + 177, top + 61, 275, 228, 0xFF222B39, 0xFF6A563B);
        PresetSnapshotPayload snap = ClientSnapshot.get();
        boolean valid = snap != null && snap.names().size() == 6;
        for (int i = 0; i < 6; i++) {
            int x = left + 17, y = top + 62 + i * 32;
            frame(g, x, y, 145, 31, i == selected ? 0xFF5B4930 : 0xFF2A3545,
                i == selected ? GOLD : 0xFF445365);
            String name = valid ? snap.names().get(i) : "セット " + (i + 1);
            if (name.length() > 13) name = name.substring(0, 12) + "…";
            presets[i].setMessage(Component.literal((i == selected ? "◆ " : "") + (i + 1) + "  " + name));
        }
        String current = valid ? snap.names().get(selected) : "セット " + (selected + 1);
        g.drawString(font, "選択中：" + font.plainSubstrByWidth(current, 180), left + 188, top + 73, GOLD);
        int filled = 0;
        if (snap != null && snap.occupied().size() == 36)
            for (int j = 0; j < 6; j++) filled += snap.occupied().get(selected * 6 + j);
        g.drawString(font, "保管 " + filled + " / 6", left + 379, top + 73, MUTED);
        g.fill(left + 186, top + 94, left + 444, top + 95, EDGE);
        for (int j = 0; j < 6; j++) {
            int x = left + 186 + (j % 3) * 86, y = top + 109 + (j / 3) * 57;
            frame(g, x, y, 82, 51, 0xFF303A4A, 0xFF806A4A);
            g.drawString(font, PARTS[j], x + 6, y + 6, MUTED);
        }
        g.drawString(font, "プリセット名を変更", left + 187, top + 216, MUTED);
        g.drawCenteredString(font, "左Alt + G：管理画面  |  テンキー1～6：即時切替", width / 2, top + 291, MUTED);
        super.render(g, mx, my, tick);
        if (snap != null && snap.items().size() == 36) {
            for (int j = 0; j < 6; j++) {
                var stack = snap.items().get(selected * 6 + j);
                if (stack.isEmpty()) continue;
                int x = left + 186 + (j % 3) * 86 + 57;
                int y = top + 109 + (j / 3) * 57 + 5;
                g.renderItem(stack, x, y);
                if (mx >= x && mx < x + 16 && my >= y && my < y + 16)
                    g.renderTooltip(font, stack, mx, my);
            }
        }
    }
}
