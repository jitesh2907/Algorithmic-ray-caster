# ALGORITHMIC 3D MAZE EXPLORER - Gameplay Stages & Visual Mockups

This document illustrates the visual design, screen composition, and UI presentation across all primary gameplay stages of **ALGORITHMIC 3D MAZE EXPLORER** in hardware-accelerated 3D graphics mode.

---

## Stage 1: Professional Main Menu

Rendered with gothic gold typography over a translucent slate backdrop, with the 3D torch-lit starting chamber slowly rotating in the background:

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                            ALGORITHMIC 3D MAZE EXPLORER                                 |
|                    A First-Person Procedural 3D Dungeon Simulation                      |
|                                                                                         |
| ======================================================================================= |
|                                                                                         |
|                                                                                         |
|                            [1] / [ENTER]  -  START EXPEDITION                           |
|                                                                                         |
|                            [2] / [I]      -  HOW TO PLAY & LORE                         |
|                                                                                         |
|                            [3] / [ESC]    -  QUIT TO DESKTOP                            |
|                                                                                         |
|                                                                                         |
|                                                                                         |
|                                                                                         |
|                                                                                         |
|                                                                                         |
|                   jMonkeyEngine 3.9.0-stable | LWJGL3 OpenGL 3D Engine                  |
+-----------------------------------------------------------------------------------------+
```

---

## Stage 2: Instructions & Survival Guide

Displays dungeon lore, explicit mission objectives, and a comprehensive keyboard/mouse controls guide:

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                       *** EXPEDITION BRIEFING & SURVIVAL GUIDE ***                      |
|                                                                                         |
|   [ DUNGEON LORE ]                                                                      |
|   You are trapped deep inside an ancient algorithmic crypt. The sealed golden           |
|   portal at [13, 13] is the only escape, but it is locked by arcane seals requiring     |
|   3 Sacred Golden Keys.                                                                 |
|                                                                                         |
|   [ MISSION OBJECTIVES ]                                                                |
|   1. EXPLORE: Navigate the 15x15 labyrinth corridors and follow torch sconces.         |
|   2. RECOVER 3 KEYS: Hidden in the East Wing [13, 1], Central Alcove [7, 3], and        |
|      South Wing [1, 13].                                                                |
|   3. COLLECT COINS: 5 Ancient Coins (+100 pts each) are scattered in key corridors.     |
|   4. SURVIVE: The Maze Stalker patrols these halls. If it spots you with line-of-sight   |
|      (DDA), it computes the shortest path (BFS) to pursue and strike you (-20 HP)!      |
|   5. ESCAPE: Reach the glowing Exit Portal at [13, 13] once all 3 keys are acquired!    |
|                                                                                         |
|   [ CONTROLS GUIDE ]                                                                    |
|   - [W / A / S / D]  : First-Person Movement & Smooth Wall-Sliding                      |
|   - [Left Shift]    : Sprint (Accelerates movement speed from 4.0 to 7.0 m/s)           |
|   - [Mouse]         : First-Person Camera Look (Clamped pitch +/- 89 degrees)           |
|   - [ESC]           : Pause Game & Free Cursor / Return from Menus                      |
|   - [R]             : Quick Respawn / Restart Expedition                                |
|                                                                                         |
|                      >>> Press [ENTER], [SPACE], or [ESC] to Return <<<                 |
+-----------------------------------------------------------------------------------------+
```

---

## Stage 3: In-Game Exploration & HUD (Patrol / Safe)

Active gameplay in a dark stone corridor illuminated by iron torch sconces, with the full HUD visible:

```
+-----------------------------------------------------------------------------------------+
| ALGORITHMIC 3D MAZE EXPLORER                            MAZE STALKER: PATROLLING (Safe) |
| [====================] HP: 100 / 100 (100%)                                             |
| OBJECTIVE: Find 3 Golden Keys to Unseal Exit (0 / 3)                                    |
| SCORE: 0 PTS      TIME: 00:24.3                                                         |
| KEYS: [---] [---] [---] (0 / 3)                                                         |
| COINS: 0 / 5                                                                            |
|                                                                                         |
|                                            |                                            |
|                                      +--+  |  +--+                                      |
|                                      |  |  |  |  |                                      |
|                                      |  |  |  |  |                                      |
|                                      +--+  +  +--+                                      |
|                                      (Warm Torch Sconce)                                |
|                                                                                         |
|                                            +                                            |
|                                       (Crosshair)                                       |
|                                                                                         |
|                                                                                         |
|                                      /\ (Golden Key)                                    |
|                                     [++] (Hovering &                                    |
|                                      ||   Rotating)                                     |
|                                                                                         |
|                                                                                         |
| Pos: (4.5, 4.5) | Cell: [1, 1] | Facing: EAST | [ESC] Pause Menu                        |
+-----------------------------------------------------------------------------------------+
```

---

## Stage 4: In-Game Combat & Threat Alert (Chase Mode)

When the Stalker detects the player via DDA line-of-sight, the HUD radar turns crimson and pulses, and red light casts on the walls:

```
+-----------------------------------------------------------------------------------------+
| ALGORITHMIC 3D MAZE EXPLORER                      ! WARNING: MAZE STALKER PURSUING !    |
| [====================] HP: 100 / 100 (100%)              (Pulsing Warning Crimson)      |
| OBJECTIVE: Find 3 Golden Keys to Unseal Exit (1 / 3)                                    |
| SCORE: 1,100 PTS   TIME: 01:08.7                                                        |
| KEYS: [KEY 1] [---] [---] (1 / 3)                                                       |
| COINS: 1 / 5                                                                            |
|                                                                                         |
|                                                                                         |
|                                      \  /\  /                                           |
|                                       \(**)/  <-- (Maze Stalker with glowing red        |
|                                       /|  |\       eyes & crimson point light aura)     |
|                                      / |__| \                                           |
|                                                                                         |
|                                            +                                            |
|                                                                                         |
|                                                                                         |
|                                                                                         |
|                           +100 PTS! Ancient Coin Recovered (1 / 5)                      |
|                                                                                         |
| Pos: (22.5, 10.5) | Cell: [7, 3] | Facing: NORTH | [ESC] Pause Menu                     |
+-----------------------------------------------------------------------------------------+
```

---

## Stage 5: In-Game Pause Menu

Triggered via `[ESC]`, freezing simulation time and freeing the mouse cursor:

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                *** EXPEDITION PAUSED ***                                |
|                               The labyrinth stands still...                             |
|                                                                                         |
|                                                                                         |
|                            [1] / [ESC]  -  RESUME EXPEDITION                            |
|                                                                                         |
|                            [2] / [I]    -  INSTRUCTIONS & CONTROLS                      |
|                                                                                         |
|                            [3] / [R]    -  RESTART EXPEDITION                           |
|                                                                                         |
|                            [4] / [M]    -  RETURN TO MAIN MENU                          |
|                                                                                         |
|                            [Q]          -  QUIT TO DESKTOP                              |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```

---

## Stage 6: Sealed vs Unsealed Exit Portal Gateway

### Locked (Keys < 3):
A towering golden stone archway at `[13, 13]` with a glowing crimson barrier:
```
           +=========================================+
           |       ANCIENT RUNIC PORTAL GATEWAY      |
           +=========================================+
            | |                                   | |
            | |      ***********************      | |
            | |      *                     *      | |
            | |      *   SEALED BARRIER    *      | |
            | |      *  (Crimson Warning)  *      | |
            | |      *                     *      | |
            | |      *  Find 3 Golden Keys *      | |
            | |      *   to Break Arcane   *      | |
            | |      *        Seals        *      | |
            | |      ***********************      | |
            | |                                   | |
```

### Unlocked (Keys == 3):
Transmutes into a radiant emerald/cyan energy vortex:
```
           +=========================================+
           |       ANCIENT RUNIC PORTAL GATEWAY      |
           +=========================================+
            | |                                   | |
            | |      ~~~~~~~~~~~~~~~~~~~~~~~      | |
            | |     ~  *   *       *    *   ~     | |
            | |    ~  *  RADIANT EMERALD *   ~    | |
            | |    ~      ESCAPE VORTEX       ~   | |
            | |    ~    *   (UNLOCKED)   *    ~   | |
            | |     ~     *     *     *     ~     | |
            | |      ~~~~~~~~~~~~~~~~~~~~~~~      | |
            | |                                   | |
```

---

## Stage 7: Victory Screen & Mission Debriefing

Triggered when stepping through the unlocked portal at `[13, 13]`:

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                          *** DUNGEON ESCAPED - VICTORY! ***                             |
|             All 3 Golden Keys United - The Ancient Portal Sealed Behind You!            |
|                                                                                         |
|                     Final Score:       3,500 Points                                     |
|                                                                                         |
|                     Completion Time:   01:42.8                                          |
|                                                                                         |
|                     Golden Keys:       3 / 3 Found                                      |
|                                                                                         |
|                     Ancient Coins:     5 / 5 Recovered                                  |
|                                                                                         |
|                     Remaining Health:  80 / 100 HP                                      |
|                                                                                         |
|                                                                                         |
|                     RANK: S-TIER (MASTER DUNGEON SPEEDRUNNER)                           |
|                                                                                         |
|                                                                                         |
|                               [R]   -  PLAY AGAIN (Restart)                             |
|                               [M]   -  RETURN TO MAIN MENU                              |
|                               [ESC] -  QUIT TO DESKTOP                                  |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```

---

## Stage 8: Defeat (Game Over Screen)

Triggered if player health reaches 0:

```
+-----------------------------------------------------------------------------------------+
|                                                                                         |
|                                 *** YOU PERISHED ***                                    |
|               The Maze Stalker tracked your scent through the darkness...               |
|                                                                                         |
|                     Score Acquired:    1,600 Points                                     |
|                                                                                         |
|                     Time Survived:     00:54.2                                          |
|                                                                                         |
|                     Keys Recovered:    1 / 3                                            |
|                                                                                         |
|                     Coins Gathered:    3 / 5                                            |
|                                                                                         |
|                                                                                         |
|                                                                                         |
|                                                                                         |
|                           [R]   -  RESPAWN / RESTART EXPEDITION                         |
|                           [M]   -  RETURN TO MAIN MENU                                  |
|                           [ESC] -  QUIT TO DESKTOP                                      |
|                                                                                         |
+-----------------------------------------------------------------------------------------+
```

