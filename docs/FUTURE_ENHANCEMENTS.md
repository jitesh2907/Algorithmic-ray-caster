# ALGORITHMIC 3D MAZE EXPLORER - Known Limitations & Future Enhancements

## 1. Known Technical & Algorithmic Limitations

1. **Discrete Grid Representation vs Continuous Physics:**
   - *Current Implementation:* Wall collisions and entity pathfinding are resolved against a discrete 2D grid matrix with continuous cylinder sliding.
   - *Limitation:* The maze is currently planar with uniform wall heights ($3.5\text{m}$). True multi-level ramps, stairs, and vertical platforms require 3D navmesh representations.
2. **Single Enemy Threat Model:**
   - *Current Implementation:* One autonomous "Maze Stalker" patrols the central and eastern wings.
   - *Limitation:* In large mazes, adding multiple concurrent enemies requires hierarchical flocking or dynamic spatial partitioning (e.g. Quadtree/BVH) to avoid redundant BFS calculations.
3. **Procedural Geometry First:**
   - *Current Implementation:* Complies with `AGENTS.md` by using procedural meshes (`Box`, `Sphere`, `Cylinder`, `Quad`) with standard diffuse, ambient, and specular lighting materials.
   - *Limitation:* External textures (normal maps, PBR heightmaps, custom glTF skeletal animated character meshes) are not yet loaded.
4. **Lighting Count Overhead on Low-End Hardware:**
   - *Current Implementation:* 14 static point lights representing torch sconces, 1 portal beacon light, and 1 mobile dynamic enemy light.
   - *Limitation:* In forward rendering without deferred shading or light clustering, having $>20$ simultaneous point lights can cause GPU fill-rate pressure on integrated graphics.

---

## 2. Planned Future Enhancements

### Phase 1: Gameplay & Level Generation
- **Dynamic Procedural Maze Generation:** Integrate Prim's algorithm, Kruskal's algorithm, or Recursive Backtracker at runtime to generate unique $21 \times 21$ or $31 \times 31$ labyrinths each session.
- **Multiple Enemy Classes:**
  - *Stalker:* High speed, low health, direct BFS pursuit.
  - *Sentinel:* Stationary guardian with 360-degree DDA line-of-sight sweep.
  - *Phantom:* Teleports between shadow alcoves when unobserved.
- **Power-Ups & Consumables:**
  - *Torch Oil:* Temporarily extends player vision radius.
  - *Boots of Haste:* Grants $30\%$ sprint speed boost for $10\text{s}$.
  - *Invisibility Rune:* Cloaks the player from DDA line-of-sight detection.

### Phase 2: Advanced Rendering & Visual Effects
- **PBR Texture Mapping:** Add normal maps, roughness maps, and ambient occlusion textures for slate stone walls and damp basalt floor flags.
- **Post-Processing Filter Pipeline:** Add subtle vignette, bloom on torch flames, and depth-of-field fog effects via jME3's `FilterPostProcessor`.
- **Procedural Particle Systems:** Embers floating from wall torch sconces and radiant mystic sparkles hovering over the unlocked Exit Portal.

### Phase 3: Spatial Audio & Soundscapes
- **3D Spatial Audio:** Attach OpenAL 3D sound nodes to torches (crackling fire), collectible coins (crystalline chime), and the enemy (menacing guttural breathing that increases in volume as it gets closer).
- **Dynamic Tension Soundtrack:** Ambient dungeon drone during `PATROL`, smoothly cross-fading into high-tempo rhythmic percussion during `CHASE`.

