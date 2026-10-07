# Stage 7: Return to original preset

- Active preset index is persisted per player in world SavedData.
- Switching from preset A to B returns worn items to A and equips items stored in B.
- Switching to the active preset is a no-op.
- First switch with no active preset moves currently worn items into inventory, conservatively requiring free slots. If full, switching is rejected.
- Per-slot GUI exchange remains legacy behavior and should be refined before release.
- Build and in-game verification are still required.
