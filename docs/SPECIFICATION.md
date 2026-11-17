# ALGORITHMIC 3D MAZE EXPLORER - Software Specification Sheet

## 1. Project Identification

- **Project Title:** Algorithmic 3D Maze Explorer
- **Project Category:** Java 3D Application / Algorithmic Game Simulation
- **Programming Language:** Java (Standard Edition, Version 17 LTS / Version 27 verified)
- **Engine Framework:** jMonkeyEngine 3.9.0-stable (`jme3-core`, `jme3-desktop`, `jme3-lwjgl3`, `jme3-plugins`)
- **Rendering API:** LWJGL 3.3.6 (OpenGL 3.2+ Core Profile)
- **Build System:** Gradle (with preserved Gradle Wrapper)
- **Target Platform:** Windows 10/11 Desktop
- **Development Environment:** Google Antigravity IDE

---

## 2. Project Abstract

**Algorithmic 3D Maze Explorer** is a genuine hardware-accelerated first-person 3D game engineered to demonstrate the union of fundamental computer science algorithms with real-time 3D graphics rendering. Operating entirely within a true OpenGL 3D desktop window, the engine synthesizes discrete mathematical algorithms—such as **Digital Differential Analyzer (DDA)** line-of-sight ray traversal and **Breadth-First Search (BFS)** graph pathfinding—with continuous physical interactions, procedural mesh generation, dynamic lighting, and responsive user interface state machines.

The application places the user in an atmospheric 15x15 subterranean labyrinth. The player must navigate dark corridors illuminated by iron torch sconces, collect three sacred Golden Keys and five Ancient Coins, evade an autonomous patrolling entity ("The Maze Stalker") that computes pursuit trajectories via BFS, and reach the sealed Exit Gateway at `[13, 13]` to escape.

---

## 3. Functional Requirements (FR)

### FR-01: Discrete 2D Maze Representation
- The system must model the game world using a discrete 2D grid (`MazeGrid`) separating logical map data from 3D scene-graph nodes.
- Solid walls are represented as `1` and walkable pathways as `0`.
- The canonical map layout is fixed at $15 \times 15$ with dedicated spawn coordinates at `(1, 1)` and exit portal coordinates at `(13, 13)`.

### FR-02: Procedural 3D Environment Construction
- The system must translate discrete grid cells into actual 3D spatial geometries using procedural meshes (`Box`, `Cylinder`, `Sphere`, `Quad`).
- Continuous floor and ceiling planes must encompass the dungeon perimeter.
- Walls, floors, ceilings, and markers must feature distinct lighting and shading materials with specular highlights.
- Atmospheric lighting must include cold ambient illumination, directional moonlight fill, and 14 warm amber torch point lights.

### FR-03: First-Person Movement & Camera Rig
- The player must navigate via standard WASD keyboard inputs with smooth frame-time (`tpf`) integration.
- Sprinting via Left Shift must increase movement velocity from $4.0\text{ m/s}$ to $7.0\text{ m/s}$.
- The first-person camera rig must decouple horizontal yaw from vertical pitch, with vertical pitch clamped to $[-89^\circ, +89^\circ]$ to prevent gimbal lock.
- Player eye height must remain fixed at $1.8\text{m}$.

### FR-04: Continuous Collision Detection & Wall Sliding
- The collision system must model the player as a vertical bounding cylinder ($R = 0.4\text{m}$).
- Motion must be evaluated against adjacent grid cell wall AABBs.
- When moving diagonally into a wall corner, the collision resolver must project displacement along the open orthogonal axis, providing smooth wall-sliding response without clipping or tunneling.

### FR-05: Digital Differential Analyzer (DDA) Ray Casting
- The system must implement a standalone, deterministic 2D/3D DDA ray caster.
- Each ray cast must evaluate exact cell boundary intersections, return perpendicular distance to avoid fish-eye distortion, identify the wall face normal ($0$ for X-axis, $1$ for Z-axis), and report hit grid coordinates.
- A specialized line-of-sight query (`hasLineOfSight`) must evaluate whether solid walls obstruct the line between two coordinates.

### FR-06: Breadth-First Search (BFS) Pathfinding
- The pathfinding subsystem must implement Breadth-First Search on the discrete maze graph.
- The algorithm must guarantee the shortest traversable path between start and target grid cells.
- The pathfinder must enforce grid walkability, preventing movement through walls or diagonal corner-cutting.
- Outputs must be structured as an ordered sequence of contiguous orthogonal waypoints.

### FR-07: Autonomous Enemy AI ("The Maze Stalker")
- The system must simulate an autonomous enemy entity with a 3-state finite state machine (`PATROL`, `CHASE`, `ATTACK`).
- While patrolling, the enemy must follow a cyclic sequence of waypoints in the central/eastern corridors.
- If the player is within $12.0\text{m}$ and line-of-sight is unobstructed (verified via DDA), the enemy must transition to `CHASE` and calculate pursuit paths via BFS.
- If the enemy reaches the player ($\le 1.3\text{m}$), it must trigger an attack inflicting $20\text{ HP}$ melee damage with an attack cooldown of $1.2\text{s}$.
- If the player escapes beyond $18.0\text{m}$ or breaks line-of-sight for $>3.0\text{s}$, the enemy must return to `PATROL`.
- The enemy must be represented by a procedural obsidian/crimson spatial with glowing red flame eyes and an attached red `PointLight` casting warning shadows down corridors.

### FR-08: Collectibles & Inventory Progression
- The dungeon must instantiate 3 Golden Keys at `(13, 1)`, `(7, 3)`, and `(1, 13)`, and 5 Ancient Coins at `(3, 1)`, `(5, 5)`, `(11, 3)`, `(3, 11)`, and `(9, 9)`.
- Collectibles must animate with harmonic vertical bobbing and rotational spin.
- Proximity detection ($\le 1.3\text{m}$) must collect items, update score ($+1000\text{ pts}$ per key, $+100\text{ pts}$ per coin), increment inventory tallies, display notification toasts, and detach the spatial from the scene graph.

### FR-09: Locked Exit Gateway & Victory Condition
- An ancient exit archway must be located at `(13, 13)`.
- The portal core must initially display a sealed crimson barrier.
- Once all 3 keys are collected, the portal must unlock, transmuting the core into a radiant emerald vortex.
- Stepping onto the portal pad with 3 keys must trigger level completion and transition to the Victory Screen.

### FR-10: Player Health & Defeat Condition
- The player must begin with $100\text{ HP}$.
- Taking enemy damage must reduce HP and activate an invulnerability timer ($1.0\text{s}$) preventing rapid consecutive strikes.
- Reaching $0\text{ HP}$ must freeze player movement and transition to the Game Over Screen.

### FR-11: User Interface & Presentation System
- **Main Menu:** Professional title screen with Start Game, Instructions, and Quit options, accompanied by a background camera pan across the 3D dungeon.
- **Instructions Screen:** Displays dungeon lore, 5 clear mission objectives, and a comprehensive controls guide.
- **Pause Menu:** Pressing `[ESC]` during gameplay must freeze game time, unlock the cursor, and display Resume, Instructions, Restart, Main Menu, and Quit options.
- **In-Game HUD:** Displays health bar with dynamic color transitions, mission objective display, score counter, formatted timer (`MM:SS.S`), key inventory indicators, coin counter, threat radar, crosshair, and telemetry.
- **Victory Screen:** Displays final score, time, keys, coins, remaining health, and an evaluated Explorer Rank (S, A, B, C).
- **Game Over Screen:** Displays survival statistics, death narrative, and options to respawn or return to the menu.
- **Expedition Restart:** `[R]` must completely reset player state, health, score, timer, enemy state, exit portal, and respawn all collectibles.

---

## 4. Non-Functional Requirements (NFR)

- **Performance & Frame Rate:** The simulation must maintain a stable 60 FPS under standard desktop configurations without garbage collection latency spikes.
- **Strict Decoupling:** Algorithmic systems (DDA, BFS, collision) must be completely decoupled from jME3 scene-graph nodes and OpenGL contexts, enabling full headless automated test execution.
- **Build System Preservation:** The project must preserve the original Gradle wrapper and build configurations, building cleanly with `./gradlew test` and `./gradlew build`.
- **Memory Safety:** Meshes, materials, and point lights must be reused efficiently, avoiding per-frame spatial reallocations.
