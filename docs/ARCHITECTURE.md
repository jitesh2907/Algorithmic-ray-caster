# ALGORITHMIC 3D MAZE EXPLORER - Architectural Blueprint & Design Document

## 1. Executive Summary & Technology Stack

**ALGORITHMIC 3D MAZE EXPLORER** is a first-person 3D game built from the ground up on modern Java and **jMonkeyEngine 3.9.0-stable** using the **LWJGL3 (OpenGL)** desktop renderer. It combines discrete algorithmic simulation (DDA ray tracing, BFS graph pathfinding, procedural maze generation) with real-time hardware-accelerated 3D graphics, spatial audio, and dynamic gameplay systems.

### Core Technology Stack
- **Language:** Java 17 LTS (Java 27 runtime verified)
- **Engine Framework:** jMonkeyEngine 3.9.0-stable (`jme3-core`, `jme3-desktop`, `jme3-lwjgl3`, `jme3-plugins`)
- **Build & Dependency System:** Gradle 9.8.2 (Gradle Wrapper preserved & verified)
- **Graphics Pipeline:** LWJGL 3.3.6 / OpenGL 3.2+ Desktop Core Profile (MSAA 4x, VSync enabled)
- **Target OS:** Windows 10/11 Desktop
- **Development Environment:** Google Antigravity IDE

---

## 2. Global System Architecture

```
+---------------------------------------------------------------------------------------------------------+
|                                    MAZE EXPLORER MAIN APPLICATION                                       |
|                                       MazeExplorerApp.java                                              |
+---------------------------------------------------------------------------------------------------------+
       |                                           |                                             |
       v                                           v                                             v
+-----------------------------+     +-----------------------------+               +-----------------------------+
|    GAME STATE MANAGEMENT    |     |      INPUT & CONTROLLERS    |               |         UI & HUD            |
| - GameStateManager          |     | - PlayerController (WASD)   |               | - InGameHUD (guiNode)       |
| - MAIN_MENU                 |     | - FirstPersonCamera (Mouse) |               | - MenuRenderer (All Menus)  |
| - INSTRUCTIONS              |     | - Key/Mouse Input Mapping   |               | - UIActionHandler (Events)  |
| - PLAYING / PAUSED          |     +-----------------------------+               +-----------------------------+
| - VICTORY / GAME_OVER       |                    |                                             ^
+-----------------------------+                    v                                             |
       |                                    +-----------------------------+                              |
       +----------------------------------->|      GAMEPLAY SUBSYSTEM     |------------------------------+
                                            +-----------------------------+
                                             /             |             \
                                            v              v              v
                          +-------------------+  +-------------------+  +-------------------+
                          |  MAZE & GEOMETRY  |  |    ALGORITHMS     |  | ENTITIES & STATS  |
                          | - MazeGrid (2D)   |  | - DDARayCaster3D  |  | - Player State    |
                          | - Maze15x15       |  | - BFSPathfinder   |  | - Enemy AI        |
                          | - Maze3DBuilder   |  | - CollisionSystem |  | - Collectibles    |
                          | - Materials/Lights|  | - Line-of-Sight   |  | - Exit Door       |
                          | - Scene Graph     |  | - AABB / Cylinder |  | - Health & Score  |
                          +-------------------+  +-------------------+  +-------------------+
```

---

## 3. Detailed Module Specifications

### 3.1 Main Application (`com.mazeexplorer.MazeExplorerApp`)
- Extends jME3's `SimpleApplication`.
- Configures display parameters (`AppSettings`): 1280x720 windowed, VSync enabled, 4x MSAA antialiasing.
- Manages the root node (`rootNode`) for 3D spatials, `guiNode` for 2D interface overlays, lighting, and asset manager access.
- Coordinates transitions between `GameStateManager`, `MenuRenderer`, `InGameHUD`, and active gameplay.

### 3.2 Game State Management (`com.mazeexplorer.state`)
- **`GameStateManager`**: Coordinates high-level states (`MAIN_MENU`, `INSTRUCTIONS`, `PLAYING`, `PAUSED`, `VICTORY`, `GAME_OVER`).
- Supports state transition listeners and remembers `previousState` for seamless return navigation from the instructions screen.

### 3.3 Player Controller & Camera Rig (`com.mazeexplorer.player`, `com.mazeexplorer.camera`)
- **`Player`**: Encapsulates continuous position $(X, Y, Z)$, heading angle, bounding cylinder radius ($R = 0.4\text{m}$), eye height ($1.8\text{m}$), and inventory (keys, coins).
- **`PlayerController`**: Listens to WASD keyboard inputs, calculates movement vectors relative to camera orientation, and integrates sprinting ($7.0\text{ m/s}$).
- **`FirstPersonCamera`**: First-person camera rig attached to the player's eye position. Smooth pitch clamping ($[-89^\circ, +89^\circ]$) prevents gimbal lock.

### 3.4 Maze Map & 3D Builder (`com.mazeexplorer.maze`, `com.mazeexplorer.builder`)
- **`MazeGrid`**: Represents discrete tile cells ($0 = \text{Corridor}$, $1 = \text{Wall}$, $2 = \text{Exit Door}$, $3 = \text{Spawn Point}$, $4 = \text{Key Tile}$).
- **`Maze15x15`**: Canonical 15x15 dungeon layout separating logic from 3D representation.
- **`Maze3DBuilder`**: Transforms discrete 2D grid into optimized 3D spatials. Builds procedural wall boxes, continuous floor/ceiling planes, 14 iron torch sconces with point lights, emerald start plinth, and golden exit portal.

### 3.5 Collision System (`com.mazeexplorer.collision`)
- Custom lightweight discrete-continuous collision pipeline avoiding unnecessary heavy physics libraries.
- Checks player bounding cylinder against adjacent maze cell Axis-Aligned Bounding Boxes (AABB).
- Resolves penetration via sliding response vectors along open orthogonal axes.

### 3.6 DDA Ray Caster (`com.mazeexplorer.raycast`)
- 2D/3D Digital Differential Analyzer (DDA) performing discrete grid line traversal in $O(k)$ steps.
- Provides immediate line-of-sight verification between enemies and the player through maze corridors.
- Identifies exact wall tile coordinates and hit surface normal faces ($0$ for X-face, $1$ for Z-face).

### 3.7 BFS Pathfinding & Enemy AI (`com.mazeexplorer.pathfinding`, `com.mazeexplorer.ai`)
- **`BFSPathfinder`**: Breadth-First Search operating on discrete maze grid connectivity. Guarantees the shortest path with $O(V + E)$ efficiency.
- **`Enemy`**: Finite state machine with 3 behavioral states:
  1. *Patrol:* Navigates predefined waypoints.
  2. *Chase:* Initiates dynamic BFS tracking upon line-of-sight confirmation via DDA.
  3. *Attack:* Inflicts $20\text{ HP}$ melee damage when within $1.3\text{m}$.
- **`EnemyController`**: Coordinates state transitions, updates procedural 3D spatial with glowing red flame eyes, and smooths movement.

### 3.8 Collectibles & Exit Door (`com.mazeexplorer.entity`)
- **`Collectible`**: Coins and keys modeled procedurally with idle levitation and continuous rotation animations.
- **`ExitDoor`**: Golden runic archway requiring 3 collected keys to unlock. Transmutes core from sealed crimson barrier to radiant emerald vortex.

### 3.9 Health, Scoring & Statistics (`com.mazeexplorer.stats`)
- **`HealthSystem`**: Manages player HP ($0 - 100$), damage cooldowns, and invulnerability timer ($1.0\text{s}$).
- **`ScoreManager`**: Tracks exploration score, coins, keys, and millisecond-accurate level timer.

### 3.10 UI Presentation & Menus (`com.mazeexplorer.ui.hud`, `com.mazeexplorer.ui.menu`)
- Rendered on jME3's orthogonal `guiNode`.
- **`InGameHUD`**: Real-time health bar, objective banner, score, formatted timer (`MM:SS.S`), key inventory indicators, coin counter, threat radar, crosshair, and telemetry.
- **`MenuRenderer`**: Screen layout and rendering for Main Menu, Instructions, Pause Menu, Victory Screen (with S/A/B/C rank), and Game Over screen.
- **`UIActionHandler`**: Decoupled keyboard navigation and menu event dispatcher.
