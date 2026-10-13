# BBS Trajectory Addon (`bbs-trajectory-addon`)

An interactive 3D **Whole-Replay Trajectory Transformation Gizmo** add-on for **Blockbuster Studio (BBS 2.8 / BBS Flow)** on Minecraft Fabric 1.20.1.

Compatible with **BBS-POV**, **BBS-Crowd**, **BBS-FS**, and **Refreshed UI**.

---

## Features

- **Whole-Replay Rigid Transformation**:
  - Transform entire recorded replay trajectories (such as an actor walking forward 2 blocks and turning left).
  - Shift position in 3D world space ($\Delta X, \Delta Y, \Delta Z$) and rotate orientation ($\Delta\text{Yaw}$) around a defined pivot without distorting relative path kinematics or keyframe timings.
- **Interactive 3D Viewport Gizmo**:
  - Direct 3-axis translation handles ($X$, $Y$, $Z$ and planar guides) + Yaw rotation ring ($Y$-axis).
  - Smooth 60 FPS live preview during mouse dragging.
  - Commits cleanly on mouse release and pushes an undo point to the BBS Undo history (`Ctrl+Z` to revert).
- **Non-Destructive & Non-Keyframed Toggle**:
  - Activated via an icon toggle button (`Icons.ALL_DIRECTIONS`) in the bottom icon bar of `UIFilmPreview` (`preview.icons`).
  - Context menu options:
    - *Pivot at Start Frame (0)* (default)
    - *Pivot at Current Cursor*
    - *Reset Trajectory Pivot*
- **Collision-Free Gizmo Override**:
  - When the Trajectory toggle is active, it cleanly overrides viewport gizmo selection, preventing accidental manipulation of model bone keyframes, pose keyframes, or anchor tracks.
  - When toggled off, native BBS 2.8 gizmo behavior is 100% restored.

---

## Building from Source

```bash
./gradlew build
```

The compiled mod jar will be in `build/libs/bbs_trajectory-1.0.0-1.20.1.jar`.

---

## Installation

Place `bbs_trajectory-1.0.0-1.20.1.jar` into your Minecraft `mods` folder alongside `bbs-2.8-1.20.1.jar`.
