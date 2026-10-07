# Armor Preset Switcher — Stage 6 (experimental)

Minecraft 1.21.1 / NeoForge 21.1.209 / Java 21.

## New in Stage 6
- The server now sends read-only ItemStack snapshots for the 36 vault slots.
- Japanese GUI draws stored equipment icons and item tooltips, and updates preset labels.
- GitHub Actions build workflow added at `.github/workflows/build.yml`.
- Dependency versions pinned (not verified against a successful build).

## Usage
- Left Alt + G opens the GUI; Left Alt + 1–6 requests a preset swap.
- Six vault presets each hold helmet, chestplate, leggings, boots, main hand and off hand.
- Clicking a slot's exchange button swaps that equipped item with the stored one.
- No client-to-server ItemStacks are accepted.

## Warnings / unfinished
- **Not compiled or tested in Minecraft.** GitHub Actions must be run and any API errors corrected.
- This is a read-only item icon preview plus exchange buttons, **not yet a drag-and-drop inventory menu**.
- Hotbar key conflicts, equipment slot restrictions, death behavior, multiplayer and recovery after server crashes need testing.
- Back up your world before use; never test on a valuable survival save.
- A real Gradle wrapper is not included; CI uses setup-gradle 8.10.2.
