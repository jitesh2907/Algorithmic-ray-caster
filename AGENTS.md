# AGENTS.md - Permanent Development Instructions & Architecture Contract

## 1. Project Identification & Scope
- **Project Title:** ALGORITHMIC 3D MAZE EXPLORER
- **Platform & Engine:** jMonkeyEngine 3.9.0-stable (jme3-core, jme3-desktop, jme3-lwjgl3)
- **Target Platform:** Windows / Desktop (Java 17+)
- **Build System:** Gradle (with preserved Gradle Wrapper)
- **Development Environment:** Google Antigravity IDE
- **Core Paradigm:** Genuine hardware-accelerated 3D graphics via LWJGL3 (OpenGL). **Strictly no terminal-based rendering.**

---

## 2. Permanent Project Rules & Invariants

1. **Gradle Starter Project Preservation:** Always inspect the Gradle build files before modification. Never delete or corrupt the Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/*`).
2. **True 3D Rendering:** Use jMonkeyEngine (`SimpleApplication`, `Node`, `Geometry`, `Material`, `Mesh`) for all visual rendering.
3. **Dependency Pinning:** Pin all engine dependencies strictly to `3.9.0-stable`. Never add arbitrary external dependencies without architectural review.
4. **Procedural Geometry First:** Do not rely on external or random 3D models (`.obj`, `.gltf`) initially. Use procedural meshes (`Box`, `Sphere`, `Cylinder`, `Quad`) with standard unshaded/lighting materials.
5. **Architectural Separation of Concerns:**
   - **Game Logic:** Pure Java entities, coordinates, statistics, timers, and state transitions.
   - **Rendering:** Scene graph management (`rootNode`, `guiNode`, spatial transforms, materials).
   - **Input:** Input mapping via jME3 `InputManager`, `KeyTrigger`, `MouseAxisTrigger`, and `ActionListener`/`AnalogListener`.
   - **Algorithms:** Standalone, deterministic mathematical implementations (DDA, BFS, collision).
   - **UI & HUD:** `guiNode` 2D overlays, text bitmaps, and state-driven menus.
6. **Milestone Testing:** Every architectural milestone must compile, pass automated unit tests, and launch cleanly.
7. **Clean Object-Oriented Principles:** Prefer composition over inheritance, use dependency injection where practical, and decouple algorithmic subsystems from engine scene-graph nodes.

---

## 3. Modular System Architecture

```
com.mazeexplorer/
├── MazeExplorerApp.java            # Main application entry point extending SimpleApplication
├── config/
│   └── GameSettings.java          # Screen resolution, FOV, movement speed, mouse sensitivity
├── state/
│   ├── GameStateManager.java      # AppStateManager wrapper managing game flow
│   ├── GameplayAppState.java      # Active gameplay loop & world update state
│   ├── PauseAppState.java         # Pause menu overlay and game freeze
│   └── MainMenuAppState.java      # Title screen, level select, and startup
├── player/
│   ├── Player.java                # Player state (position, health, inventory, velocity)
│   └── PlayerController.java      # WASD movement, jumping, mouse look coordination
├── camera/
│   └── FirstPersonCamera.java     # First-person camera rig attached to player spatial/head
├── maze/
│   ├── MazeGrid.java              # 2D discrete cell representation (walls, paths, doors)
│   └── MazeGenerator.java         # Procedural maze generation (DFS / Prim / Binary Tree)
├── builder/
│   └── Maze3DBuilder.java         # Translates MazeGrid into optimized 3D spatials (BatchNode)
├── collision/
│   ├── CollisionSystem.java       # AABB and cylinder collision checks against maze geometry
│   └── BoundingVolume.java        # Entity bounding spheres / boxes
├── raycast/
│   ├── DDARayCaster3D.java        # 2D/3D Digital Differential Analyzer for line-of-sight checks
│   └── RayHit3D.java              # Ray intersection results (distance, surface normal, tile)
├── pathfinding/
│   ├── BFSPathfinder.java         # Breadth-First Search on 2D maze grid for enemy navigation
│   └── PathNode.java              # Discrete path steps and cost evaluation
├── ai/
│   ├── Enemy.java                 # Enemy patrol, chase, and attack states
│   └── EnemyController.java       # Path execution, speed control, player detection
├── entity/
│   ├── Collectible.java           # Gems, keys, and health orbs with rotation animation
│   └── ExitDoor.java              # Level goal with trigger area and key verification
├── stats/
│   ├── HealthSystem.java          # Player HP, damage cooldowns, death trigger
│   └── ScoreManager.java          # Score counter, timer, collectibles tally
└── ui/
    ├── hud/
    │   └── InGameHUD.java         # Health bar, score, key indicator, crosshair on guiNode
    └── menu/
        ├── MenuRenderer.java      # Screen layout for start/pause/game-over screens
        └── UIActionHandler.java   # Button clicks and keyboard navigation
```

---

## 4. Algorithmic Specifications

1. **DDA Ray Caster (`com.mazeexplorer.raycast`):**
   - High-performance discrete grid ray traversal for instantaneous line-of-sight checks between Enemy and Player.
   - Evaluates whether walls obscure the player before triggering enemy chase behavior.
2. **BFS Pathfinding (`com.mazeexplorer.pathfinding`):**
   - Breadth-First Search guaranteeing shortest path traversal from enemy tile to player tile on the discrete maze grid.
   - Outputs a sequence of waypoints that the enemy smoothly interpolates towards in 3D world space.
3. **Collision Detection (`com.mazeexplorer.collision`):**
   - Lightweight discrete-to-continuous collision system without requiring heavy physics engines.
   - Implements player bounding cylinder against maze wall AABBs with sliding response along open axes.

---

## 5. Development Verification Milestones

- **Milestone 1 (Completed):** Project skeleton, Gradle verification, jMonkeyEngine 3.9.0-stable dependencies, LWJGL3 window launch with procedural test cube and directional light.
- **Milestone 2 (Completed):** 15x15 Maze grid generation, procedural 3D wall and floor instantiation, material definitions, torch sconces and lighting.
- **Milestone 3 (Completed):** First-person player controller, WASD movement, mouse look, discrete collision resolution and wall sliding.
- **Milestone 4 (Completed):** DDA ray-casting line-of-sight and BFS enemy pathfinding AI with patrol, chase, and melee attack states.
- **Milestone 5 (Completed):** Collectibles (3 Keys, 5 Coins), locked Exit Portal with key verification, health and scoring, comprehensive in-game HUD, pause state, and victory/defeat screens.

