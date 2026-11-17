package com.mazeexplorer.builder;

import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.mazeexplorer.config.GameSettings;
import com.mazeexplorer.maze.Maze15x15;
import com.mazeexplorer.maze.MazeGrid;
import java.util.ArrayList;
import java.util.List;

/**
 * Procedural 3D Maze Builder for jMonkeyEngine.
 * Implements a stylized low-poly ancient fantasy dungeon exploration environment:
 * - Detailed modular stone architecture with plinths, ashlar relief bands, dado belt moldings,
 *   cornice capitals, and corner pilaster buttresses.
 * - Multi-tile beveled stone floor pavers with district material variations (slate, mossy, sandstone, bloodstone).
 * - Heavy vaulted dungeon ceilings with transverse weathered oak structural crossbeams.
 * - Corbelled stone arches with carved keystones at district transitions.
 * - Wall alcoves, recessed niches, and wrought-iron wall rings with hanging chains.
 * - Stylized 3D torch sconces with dynamic atmospheric light flickering.
 * - Luminous arcane crystal clusters (Violet Amethyst, Cyan Mana, Amber Citrine) in mystical alcoves.
 * - Atmospheric dungeon props: wooden banded barrels, supply crates, and terracotta urns.
 * - Environmental landmarks: Adventurer's Camp (Start), Sunken Treasury (Key 2), Stalker's Crypt (Enemy),
 *   and Ascension Sanctuary (Exit).
 * - Imposing Exit Gateway with heavy iron-banded double doors, locked crimson ward, and radiant emerald vortex upon unlock.
 */
public class Maze3DBuilder {

    public static final float CELL_SIZE = GameSettings.WALL_SIZE;
    public static final float WALL_HEIGHT = GameSettings.WALL_HEIGHT;

    private final AssetManager assetManager;
    private final MazeGrid mazeGrid;

    // Shared Material Palette
    private Material wallStoneMat;
    private Material wallTrimMat;
    private Material wallDarkTrimMat;
    private Material floorSlateMat;
    private Material floorMossMat;
    private Material floorSandstoneMat;
    private Material floorBloodstoneMat;
    private Material floorRunicMat;
    private Material ceilingMat;
    private Material woodPlankMat;
    private Material woodDarkMat;
    private Material ironMat;
    private Material goldMat;
    private Material torchFlameMat;
    private Material flameGlowMat;
    private Material crystalCyanMat;
    private Material crystalVioletMat;
    private Material crystalAmberMat;
    private Material urnCeramicMat;

    // Dynamic Atmosphere Animation Holders
    public static class TorchLightHolder {
        public final PointLight light;
        public final Geometry flameGeo;
        public final ColorRGBA baseColor;
        public final float baseRadius;
        public final float phase;

        public TorchLightHolder(PointLight light, Geometry flameGeo, ColorRGBA baseColor, float baseRadius, float phase) {
            this.light = light;
            this.flameGeo = flameGeo;
            this.baseColor = baseColor;
            this.baseRadius = baseRadius;
            this.phase = phase;
        }
    }

    private final List<TorchLightHolder> animatedTorches = new ArrayList<>();
    private final List<Spatial> animatedCrystals = new ArrayList<>();
    private Node exitPortalRingsNode;
    private Node exitDoorPanelsNode;
    private float atmosphereTime = 0f;

    public Maze3DBuilder(AssetManager assetManager, MazeGrid mazeGrid) {
        this.assetManager = assetManager;
        this.mazeGrid = mazeGrid;
        initializeMaterials();
    }

    private void initializeMaterials() {
        // 1. Medieval Slate Ashlar Walls with procedural ashlar texture
        wallStoneMat = createLitMaterial(
            new ColorRGBA(0.85f, 0.83f, 0.80f, 1.0f),
            new ColorRGBA(0.35f, 0.34f, 0.32f, 1.0f),
            new ColorRGBA(0.20f, 0.20f, 0.20f, 1.0f),
            16.0f,
            ProceduralTextureFactory.getStoneWallTexture()
        );

        // 2. Chiseled Stone Trims, Plinths, and Pilasters
        wallTrimMat = createLitMaterial(
            new ColorRGBA(0.75f, 0.72f, 0.70f, 1.0f),
            new ColorRGBA(0.32f, 0.30f, 0.28f, 1.0f),
            new ColorRGBA(0.25f, 0.25f, 0.25f, 1.0f),
            24.0f,
            ProceduralTextureFactory.getStoneWallTexture()
        );

        wallDarkTrimMat = createLitMaterial(
            new ColorRGBA(0.50f, 0.48f, 0.46f, 1.0f),
            new ColorRGBA(0.22f, 0.20f, 0.20f, 1.0f),
            new ColorRGBA(0.15f, 0.15f, 0.15f, 1.0f),
            16.0f,
            ProceduralTextureFactory.getFloorSlateTexture()
        );

        // 3. Floor Pavers (District Variations)
        floorSlateMat = createLitMaterial(
            new ColorRGBA(0.80f, 0.78f, 0.76f, 1.0f),
            new ColorRGBA(0.32f, 0.30f, 0.30f, 1.0f),
            new ColorRGBA(0.15f, 0.15f, 0.15f, 1.0f),
            10.0f,
            ProceduralTextureFactory.getFloorSlateTexture()
        );

        floorMossMat = createLitMaterial(
            new ColorRGBA(0.75f, 0.85f, 0.70f, 1.0f),
            new ColorRGBA(0.30f, 0.38f, 0.28f, 1.0f),
            new ColorRGBA(0.12f, 0.15f, 0.12f, 1.0f),
            8.0f,
            ProceduralTextureFactory.getFloorMossTexture()
        );

        floorSandstoneMat = createLitMaterial(
            new ColorRGBA(0.90f, 0.82f, 0.68f, 1.0f),
            new ColorRGBA(0.38f, 0.34f, 0.26f, 1.0f),
            new ColorRGBA(0.20f, 0.18f, 0.14f, 1.0f),
            14.0f,
            ProceduralTextureFactory.getFloorSandstoneTexture()
        );

        floorBloodstoneMat = createLitMaterial(
            new ColorRGBA(0.70f, 0.22f, 0.22f, 1.0f),
            new ColorRGBA(0.32f, 0.10f, 0.10f, 1.0f),
            new ColorRGBA(0.40f, 0.15f, 0.15f, 1.0f),
            32.0f,
            ProceduralTextureFactory.getFloorBloodstoneTexture()
        );

        floorRunicMat = createLitMaterial(
            new ColorRGBA(0.60f, 1.0f, 0.75f, 1.0f),
            new ColorRGBA(0.25f, 0.65f, 0.35f, 1.0f),
            ColorRGBA.White,
            64.0f,
            ProceduralTextureFactory.getFloorRunicTexture()
        );

        // 4. Ceiling Stone
        ceilingMat = createLitMaterial(
            new ColorRGBA(0.55f, 0.52f, 0.50f, 1.0f),
            new ColorRGBA(0.22f, 0.20f, 0.20f, 1.0f),
            new ColorRGBA(0.08f, 0.08f, 0.08f, 1.0f),
            6.0f,
            ProceduralTextureFactory.getStoneWallTexture()
        );

        // 5. Wood & Timber
        woodPlankMat = createLitMaterial(
            new ColorRGBA(0.85f, 0.65f, 0.45f, 1.0f),
            new ColorRGBA(0.35f, 0.22f, 0.14f, 1.0f),
            new ColorRGBA(0.15f, 0.12f, 0.08f, 1.0f),
            12.0f,
            ProceduralTextureFactory.getWoodPlankTexture()
        );

        woodDarkMat = createLitMaterial(
            new ColorRGBA(0.70f, 0.50f, 0.35f, 1.0f),
            new ColorRGBA(0.28f, 0.18f, 0.10f, 1.0f),
            new ColorRGBA(0.12f, 0.09f, 0.06f, 1.0f),
            8.0f,
            ProceduralTextureFactory.getWoodPlankTexture()
        );

        // 6. Forged Wrought Iron
        ironMat = createLitMaterial(
            new ColorRGBA(0.65f, 0.65f, 0.70f, 1.0f),
            new ColorRGBA(0.25f, 0.25f, 0.28f, 1.0f),
            new ColorRGBA(0.70f, 0.70f, 0.75f, 1.0f),
            56.0f,
            ProceduralTextureFactory.getIronTexture()
        );

        // 7. Radiant Gold
        goldMat = createLitMaterial(
            new ColorRGBA(0.95f, 0.80f, 0.25f, 1.0f),
            new ColorRGBA(0.42f, 0.34f, 0.10f, 1.0f),
            ColorRGBA.White,
            96.0f
        );

        // 8. Ceramic Urn
        urnCeramicMat = createLitMaterial(
            new ColorRGBA(0.55f, 0.28f, 0.16f, 1.0f),
            new ColorRGBA(0.22f, 0.12f, 0.07f, 1.0f),
            new ColorRGBA(0.15f, 0.15f, 0.15f, 1.0f),
            16.0f
        );

        // 9. Emissive Materials (Unshaded for vivid glowing visuals)
        torchFlameMat = createUnshadedMaterial(new ColorRGBA(1.0f, 0.78f, 0.28f, 1.0f));
        flameGlowMat = createUnshadedMaterial(new ColorRGBA(1.0f, 0.42f, 0.12f, 0.70f));
        crystalCyanMat = createUnshadedMaterial(new ColorRGBA(0.20f, 0.95f, 1.0f, 1.0f));
        crystalVioletMat = createUnshadedMaterial(new ColorRGBA(0.85f, 0.30f, 1.0f, 1.0f));
        crystalAmberMat = createUnshadedMaterial(new ColorRGBA(1.0f, 0.82f, 0.22f, 1.0f));
    }

    private Material createLitMaterial(ColorRGBA diffuse, ColorRGBA ambient, ColorRGBA specular, float shininess) {
        return createLitMaterial(diffuse, ambient, specular, shininess, null);
    }

    private Material createLitMaterial(ColorRGBA diffuse, ColorRGBA ambient, ColorRGBA specular, float shininess, com.jme3.texture.Texture2D texture) {
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Diffuse", diffuse);
        mat.setColor("Ambient", ambient);
        mat.setColor("Specular", specular);
        mat.setFloat("Shininess", shininess);
        if (texture != null) {
            mat.setTexture("DiffuseMap", texture);
        }
        return mat;
    }

    private Material createUnshadedMaterial(ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        if (color.a < 1.0f) {
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        }
        return mat;
    }

    /**
     * Builds and returns the complete 3D maze scene graph root node.
     */
    public Node buildMazeScene() {
        Node mazeNode = new Node("DungeonMazeRoot");
        animatedTorches.clear();
        animatedCrystals.clear();

        int width = mazeGrid.getWidth();
        int height = mazeGrid.getHeight();
        float totalWidth = width * CELL_SIZE;
        float totalDepth = height * CELL_SIZE;

        // 1. Base Continuous Foundation Floor & Ceiling Slab
        Box floorMesh = new Box(totalWidth / 2.0f, 0.1f, totalDepth / 2.0f);
        Geometry floorGeo = new Geometry("DungeonFoundationFloor", floorMesh);
        floorGeo.setMaterial(wallDarkTrimMat);
        floorGeo.setLocalTranslation(totalWidth / 2.0f, -0.1f, totalDepth / 2.0f);
        mazeNode.attachChild(floorGeo);

        Box ceilingMesh = new Box(totalWidth / 2.0f, 0.1f, totalDepth / 2.0f);
        Geometry ceilingGeo = new Geometry("DungeonCeiling", ceilingMesh);
        ceilingGeo.setMaterial(ceilingMat);
        ceilingGeo.setLocalTranslation(totalWidth / 2.0f, WALL_HEIGHT + 0.1f, totalDepth / 2.0f);
        mazeNode.attachChild(ceilingGeo);

        // 2. Modular Stone Wall Architecture
        Node wallGroup = new Node("ModularWallsGroup");
        buildModularWalls(wallGroup);
        mazeNode.attachChild(wallGroup);

        // 3. Modular Floor Paver Tiles & District Patterns
        Node floorGroup = new Node("ModularFloorsGroup");
        buildModularFloors(floorGroup);
        mazeNode.attachChild(floorGroup);

        // 4. Overhead Vault Crossbeams
        Node beamsGroup = new Node("CeilingBeamsGroup");
        buildCeilingBeams(beamsGroup);
        mazeNode.attachChild(beamsGroup);

        // 5. Corbelled Stone Arches at Thresholds
        Node archesGroup = new Node("ArchesGroup");
        buildArchitecturalArches(archesGroup);
        mazeNode.attachChild(archesGroup);

        // 6. Environmental Landmarks (Start Camp, Treasury, Stalker Crypt)
        Node landmarksGroup = new Node("LandmarksGroup");
        buildLandmarks(landmarksGroup);
        mazeNode.attachChild(landmarksGroup);

        // 7. Dungeon Props (Barrels, Supply Crates, Urns, Wall Chains)
        Node propsGroup = new Node("DungeonPropsGroup");
        buildDungeonProps(propsGroup);
        mazeNode.attachChild(propsGroup);

        // 8. Mystical Glowing Crystals
        Node crystalsGroup = new Node("MysticCrystalsGroup");
        buildMysticCrystals(crystalsGroup);
        mazeNode.attachChild(crystalsGroup);

        // 9. Monumental Exit Portal Gateway (at 13, 13)
        buildExitPortal(mazeNode);

        // 10. Atmospheric Dungeon Lighting & Torches
        setupDungeonLighting(mazeNode);

        return mazeNode;
    }

    /**
     * Builds detailed modular stone architecture for every solid wall cell.
     * Features: Core block, protruding base plinths, relief ashlar stone bands,
     * mid-height dado belt moldings, crown capital moldings, and corner buttress pilasters.
     */
    private void buildModularWalls(Node parent) {
        int width = mazeGrid.getWidth();
        int height = mazeGrid.getHeight();

        Box coreBox = new Box(CELL_SIZE / 2.0f, WALL_HEIGHT / 2.0f, CELL_SIZE / 2.0f);
        Box plinthNorthSouth = new Box(CELL_SIZE / 2.0f, 0.18f, 0.05f);
        Box plinthEastWest = new Box(0.05f, 0.18f, CELL_SIZE / 2.0f);

        Box dadoNorthSouth = new Box(CELL_SIZE / 2.0f, 0.06f, 0.04f);
        Box dadoEastWest = new Box(0.04f, 0.06f, CELL_SIZE / 2.0f);

        Box corniceNorthSouth = new Box(CELL_SIZE / 2.0f, 0.12f, 0.06f);
        Box corniceEastWest = new Box(0.06f, 0.12f, CELL_SIZE / 2.0f);

        Box pilasterMesh = new Box(0.14f, WALL_HEIGHT / 2.0f, 0.14f);

        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                if (!mazeGrid.isWall(x, z)) continue;

                float wx = x * CELL_SIZE + CELL_SIZE / 2.0f;
                float wy = WALL_HEIGHT / 2.0f;
                float wz = z * CELL_SIZE + CELL_SIZE / 2.0f;

                // Main wall core
                Geometry wallCore = new Geometry("WallCore_" + x + "_" + z, coreBox);
                wallCore.setMaterial(wallStoneMat);
                wallCore.setLocalTranslation(wx, wy, wz);
                parent.attachChild(wallCore);

                // Check neighbors for exposed corridor faces
                boolean openNorth = (z > 0 && !mazeGrid.isWall(x, z - 1));
                boolean openSouth = (z < height - 1 && !mazeGrid.isWall(x, z + 1));
                boolean openWest  = (x > 0 && !mazeGrid.isWall(x - 1, z));
                boolean openEast  = (x < width - 1 && !mazeGrid.isWall(x + 1, z));

                // North Face Trim (-Z)
                if (openNorth) {
                    attachWallFaceTrim(parent, wx, wz - CELL_SIZE / 2.0f + 0.05f, plinthNorthSouth, dadoNorthSouth, corniceNorthSouth, true);
                }
                // South Face Trim (+Z)
                if (openSouth) {
                    attachWallFaceTrim(parent, wx, wz + CELL_SIZE / 2.0f - 0.05f, plinthNorthSouth, dadoNorthSouth, corniceNorthSouth, true);
                }
                // West Face Trim (-X)
                if (openWest) {
                    attachWallFaceTrim(parent, wx - CELL_SIZE / 2.0f + 0.05f, wz, plinthEastWest, dadoEastWest, corniceEastWest, false);
                }
                // East Face Trim (+X)
                if (openEast) {
                    attachWallFaceTrim(parent, wx + CELL_SIZE / 2.0f - 0.05f, wz, plinthEastWest, dadoEastWest, corniceEastWest, false);
                }

                // Corner Pilaster Buttresses where two corridor faces meet (convex corners)
                if (openNorth && openWest) {
                    attachCornerPilaster(parent, wx - CELL_SIZE / 2.0f + 0.12f, wz - CELL_SIZE / 2.0f + 0.12f, pilasterMesh);
                }
                if (openNorth && openEast) {
                    attachCornerPilaster(parent, wx + CELL_SIZE / 2.0f - 0.12f, wz - CELL_SIZE / 2.0f + 0.12f, pilasterMesh);
                }
                if (openSouth && openWest) {
                    attachCornerPilaster(parent, wx - CELL_SIZE / 2.0f + 0.12f, wz + CELL_SIZE / 2.0f - 0.12f, pilasterMesh);
                }
                if (openSouth && openEast) {
                    attachCornerPilaster(parent, wx + CELL_SIZE / 2.0f - 0.12f, wz + CELL_SIZE / 2.0f - 0.12f, pilasterMesh);
                }
            }
        }
    }

    private void attachWallFaceTrim(Node parent, float fx, float fz, Box plinthBox, Box dadoBox, Box corniceBox, boolean isNorthSouth) {
        // 1. Base Plinth Trim (Bottom skirting)
        Geometry plinth = new Geometry("WallPlinth", plinthBox);
        plinth.setMaterial(wallTrimMat);
        plinth.setLocalTranslation(fx, 0.18f, fz);
        parent.attachChild(plinth);

        // 2. Dado Belt Molding (Eye-level horizontal belt)
        Geometry dado = new Geometry("WallDado", dadoBox);
        dado.setMaterial(wallTrimMat);
        dado.setLocalTranslation(fx, 1.70f, fz);
        parent.attachChild(dado);

        // 3. Cornice Capital Trim (Top ceiling support)
        Geometry cornice = new Geometry("WallCornice", corniceBox);
        cornice.setMaterial(wallTrimMat);
        cornice.setLocalTranslation(fx, WALL_HEIGHT - 0.12f, fz);
        parent.attachChild(cornice);
    }

    private void attachCornerPilaster(Node parent, float px, float pz, Box pilasterBox) {
        Geometry pilaster = new Geometry("CornerPilaster", pilasterBox);
        pilaster.setMaterial(wallTrimMat);
        pilaster.setLocalTranslation(px, WALL_HEIGHT / 2.0f, pz);
        parent.attachChild(pilaster);
    }

    /**
     * Builds attractive multi-tile stone floor pavers across walkable corridors.
     * Incorporates distinct material themes for key dungeon districts.
     */
    private void buildModularFloors(Node parent) {
        int width = mazeGrid.getWidth();
        int height = mazeGrid.getHeight();

        Box paverBox = new Box(0.68f, 0.02f, 0.68f);
        Box mosaicBorder = new Box(1.42f, 0.025f, 1.42f);

        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                if (mazeGrid.isWall(x, z)) continue;

                float cx = x * CELL_SIZE + CELL_SIZE / 2.0f;
                float cz = z * CELL_SIZE + CELL_SIZE / 2.0f;

                Material currentFloorMat;

                // Select district material
                if (x == Maze15x15.START_X && z == Maze15x15.START_Z) {
                    currentFloorMat = floorRunicMat;
                } else if (x == 7 && z == 7) {
                    currentFloorMat = floorBloodstoneMat; // Central Stalker Crypt
                } else if ((x >= 12 && z <= 2) || (x >= 6 && x <= 8 && z >= 2 && z <= 4)) {
                    currentFloorMat = floorSandstoneMat; // Treasury & Vault corridors
                } else if (x <= 2 && z >= 12) {
                    currentFloorMat = floorMossMat; // Damp subterranean alcove
                } else if (x == Maze15x15.EXIT_X && z == Maze15x15.EXIT_Z) {
                    currentFloorMat = goldMat; // Ascension Gateway
                } else {
                    // Alternate subtle tone by grid coordinate checker for natural variation
                    currentFloorMat = ((x + z) % 2 == 0) ? floorSlateMat : wallDarkTrimMat;
                }

                // Inlaid central tile border for special landmark rooms
                if (x == Maze15x15.START_X && z == Maze15x15.START_Z || (x == 7 && z == 7)) {
                    Geometry borderGeo = new Geometry("FloorBorder_" + x + "_" + z, mosaicBorder);
                    borderGeo.setMaterial(wallTrimMat);
                    borderGeo.setLocalTranslation(cx, 0.02f, cz);
                    parent.attachChild(borderGeo);
                }

                // 4-Quadrant stylized flagstone pavers
                float[] offsets = {-0.72f, 0.72f};
                for (float ox : offsets) {
                    for (float oz : offsets) {
                        Geometry paver = new Geometry("Paver_" + x + "_" + z, paverBox);
                        paver.setMaterial(currentFloorMat);
                        paver.setLocalTranslation(cx + ox, 0.02f, cz + oz);
                        parent.attachChild(paver);
                    }
                }
            }
        }
    }

    /**
     * Builds heavy wooden transverse structural beams running across ceiling corridors.
     */
    private void buildCeilingBeams(Node parent) {
        int width = mazeGrid.getWidth();
        int height = mazeGrid.getHeight();

        Box beamNS = new Box(CELL_SIZE / 2.0f, 0.10f, 0.12f);
        Box beamEW = new Box(0.12f, 0.10f, CELL_SIZE / 2.0f);

        for (int z = 1; z < height - 1; z += 2) {
            for (int x = 1; x < width - 1; x += 2) {
                if (mazeGrid.isWall(x, z)) continue;

                float cx = x * CELL_SIZE + CELL_SIZE / 2.0f;
                float cz = z * CELL_SIZE + CELL_SIZE / 2.0f;

                // Check corridor orientation
                boolean openNorth = !mazeGrid.isWall(x, z - 1);
                boolean openSouth = !mazeGrid.isWall(x, z + 1);

                Geometry beam;
                if (openNorth || openSouth) {
                    beam = new Geometry("CeilingBeam", beamNS);
                } else {
                    beam = new Geometry("CeilingBeam", beamEW);
                }
                beam.setMaterial(woodPlankMat);
                beam.setLocalTranslation(cx, WALL_HEIGHT - 0.10f, cz);
                parent.attachChild(beam);
            }
        }
    }

    /**
     * Builds corbelled stone arches spanning across corridors at significant threshold transitions.
     */
    private void buildArchitecturalArches(Node parent) {
        // Strategic gateway transitions between dungeon districts
        int[][] archLocations = {
            {2, 1, 1},  // Exit from spawn into East corridor (span North-South)
            {1, 2, 0},  // Exit from spawn into South corridor (span East-West)
            {10, 1, 1}, // Entrance to East Key Vault
            {7, 2, 0},  // Entrance to Central Treasury
            {6, 7, 1},  // Western approach to Stalker Crypt
            {8, 7, 1},  // Eastern approach to Stalker Crypt
            {1, 12, 0}, // Entrance to South-West Mossy Grotto
            {12, 13, 1} // Grand Gateway leading directly to Exit Portal
        };

        for (int[] loc : archLocations) {
            int gx = loc[0];
            int gz = loc[1];
            boolean spanEastWest = (loc[2] == 0);

            float cx = gx * CELL_SIZE + CELL_SIZE / 2.0f;
            float cz = gz * CELL_SIZE + CELL_SIZE / 2.0f;

            buildSingleArch(parent, cx, cz, spanEastWest);
        }
    }

    private void buildSingleArch(Node parent, float cx, float cz, boolean spanEastWest) {
        Node archNode = new Node("CorbelledArch");

        float spanOffset = CELL_SIZE / 2.0f - 0.25f; // Pillar placement against walls
        float pillarWidth = 0.20f;
        float pillarHeight = WALL_HEIGHT * 0.78f;

        Box pillarMesh = new Box(pillarWidth, pillarHeight / 2.0f, pillarWidth);
        Box impostMesh = new Box(pillarWidth * 1.3f, 0.08f, pillarWidth * 1.3f);
        Box lintelMesh = spanEastWest
                ? new Box(pillarWidth * 1.1f, 0.16f, CELL_SIZE / 2.0f)
                : new Box(CELL_SIZE / 2.0f, 0.16f, pillarWidth * 1.1f);

        Box keystoneMesh = new Box(0.16f, 0.22f, 0.16f);

        // Left & Right Pillars
        Geometry leftPillar = new Geometry("ArchPillarLeft", pillarMesh);
        leftPillar.setMaterial(wallTrimMat);
        leftPillar.setLocalTranslation(spanEastWest ? cx : cx - spanOffset, pillarHeight / 2.0f, spanEastWest ? cz - spanOffset : cz);
        archNode.attachChild(leftPillar);

        Geometry leftImpost = new Geometry("ArchImpostLeft", impostMesh);
        leftImpost.setMaterial(wallTrimMat);
        leftImpost.setLocalTranslation(spanEastWest ? cx : cx - spanOffset, pillarHeight + 0.04f, spanEastWest ? cz - spanOffset : cz);
        archNode.attachChild(leftImpost);

        Geometry rightPillar = new Geometry("ArchPillarRight", pillarMesh);
        rightPillar.setMaterial(wallTrimMat);
        rightPillar.setLocalTranslation(spanEastWest ? cx : cx + spanOffset, pillarHeight / 2.0f, spanEastWest ? cz + spanOffset : cz);
        archNode.attachChild(rightPillar);

        Geometry rightImpost = new Geometry("ArchImpostRight", impostMesh);
        rightImpost.setMaterial(wallTrimMat);
        rightImpost.setLocalTranslation(spanEastWest ? cx : cx + spanOffset, pillarHeight + 0.04f, spanEastWest ? cz + spanOffset : cz);
        archNode.attachChild(rightImpost);

        // Lintel Span
        Geometry lintel = new Geometry("ArchLintel", lintelMesh);
        lintel.setMaterial(wallTrimMat);
        lintel.setLocalTranslation(cx, WALL_HEIGHT - 0.22f, cz);
        archNode.attachChild(lintel);

        // Central Carved Keystone
        Geometry keystone = new Geometry("ArchKeystone", keystoneMesh);
        keystone.setMaterial(goldMat);
        keystone.setLocalTranslation(cx, WALL_HEIGHT - 0.22f, cz);
        archNode.attachChild(keystone);

        parent.attachChild(archNode);
    }

    /**
     * Builds environmental landmarks establishing a distinct visual identity for each dungeon district:
     * - Adventurer's Camp (Start at [1,1])
     * - The Sunken Treasury (Key 2 at [7,3])
     * - The Stalker's Crypt (Enemy encounter at [7,7])
     */
    private void buildLandmarks(Node parent) {
        // 1. Starting Base Camp at [1, 1]
        float startX = Maze15x15.START_X * CELL_SIZE + CELL_SIZE / 2.0f;
        float startZ = Maze15x15.START_Z * CELL_SIZE + CELL_SIZE / 2.0f;

        // Runic Dais / Summoning Plinth
        Cylinder daisCylinder = new Cylinder(16, 16, 1.15f, 0.08f, true);
        Geometry dais = new Geometry("StartDais", daisCylinder);
        dais.setMaterial(floorRunicMat);
        dais.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
        dais.setLocalTranslation(startX, 0.04f, startZ);
        parent.attachChild(dais);

        // Expedition supply crates and traveler's lantern at camp corner
        createCrate(startX - 0.95f, startZ - 0.95f, 0.55f, 0.1f, parent);
        createCrate(startX - 0.95f, startZ - 0.45f, 0.42f, -0.2f, parent);
        createBarrel(startX + 0.95f, startZ - 0.95f, parent);

        // Traveler's Brass Lantern on post
        Cylinder postMesh = new Cylinder(8, 8, 0.04f, 1.2f, true);
        Geometry post = new Geometry("LanternPost", postMesh);
        post.setMaterial(woodPlankMat);
        post.setLocalTranslation(startX - 1.05f, 0.6f, startZ + 0.85f);
        parent.attachChild(post);

        Box lanternBox = new Box(0.10f, 0.14f, 0.10f);
        Geometry lantern = new Geometry("CampLantern", lanternBox);
        lantern.setMaterial(torchFlameMat);
        lantern.setLocalTranslation(startX - 1.05f, 1.25f, startZ + 0.85f);
        parent.attachChild(lantern);

        PointLight campLight = new PointLight();
        campLight.setColor(new ColorRGBA(1.0f, 0.85f, 0.45f, 1.0f));
        campLight.setRadius(7.5f);
        campLight.setPosition(new Vector3f(startX - 1.05f, 1.35f, startZ + 0.85f));
        parent.addLight(campLight);

        // 2. The Sunken Treasury at [7, 3] (near Key 2)
        float treasX = 7 * CELL_SIZE + CELL_SIZE / 2.0f;
        float treasZ = 3 * CELL_SIZE + CELL_SIZE / 2.0f;
        createTreasureChest(treasX, treasZ - 0.65f, FastMath.PI, parent);
        createUrn(treasX - 0.95f, treasZ - 0.95f, parent);
        createUrn(treasX + 0.95f, treasZ - 0.95f, parent);
        createBarrel(treasX + 0.95f, treasZ + 0.85f, parent);

        // 3. The Stalker's Crypt at [7, 7] (Enemy Arena)
        float cryptX = 7 * CELL_SIZE + CELL_SIZE / 2.0f;
        float cryptZ = 7 * CELL_SIZE + CELL_SIZE / 2.0f;

        // Dark Obsidian Sacrificial Altar
        Box altarBase = new Box(0.70f, 0.22f, 0.45f);
        Geometry altar = new Geometry("StalkerAltar", altarBase);
        altar.setMaterial(floorBloodstoneMat);
        altar.setLocalTranslation(cryptX, 0.22f, cryptZ);
        parent.attachChild(altar);

        // Corner Obsidian Obelisks
        Box obeliskMesh = new Box(0.12f, 1.2f, 0.12f);
        float[][] obeliskOffsets = {{-1.1f, -1.1f}, {1.1f, -1.1f}, {-1.1f, 1.1f}, {1.1f, 1.1f}};
        for (float[] off : obeliskOffsets) {
            Geometry ob = new Geometry("CryptObelisk", obeliskMesh);
            ob.setMaterial(wallDarkTrimMat);
            ob.setLocalTranslation(cryptX + off[0], 1.2f, cryptZ + off[1]);
            parent.attachChild(ob);
        }
    }

    /**
     * Builds procedural props: wooden banded barrels, supply crates, and clay urns.
     */
    private void buildDungeonProps(Node parent) {
        // Strategic decorative props placed in dead ends and corridor alcoves without blocking navigation
        createBarrel(3 * CELL_SIZE + 0.5f, 1 * CELL_SIZE + 0.5f, parent);
        createCrate(5 * CELL_SIZE + 0.5f, 5 * CELL_SIZE + 0.5f, 0.5f, 0.4f, parent);
        createBarrel(11 * CELL_SIZE + 0.6f, 3 * CELL_SIZE + 0.6f, parent);
        createUrn(3 * CELL_SIZE + 0.5f, 11 * CELL_SIZE + 0.5f, parent);
        createCrate(11 * CELL_SIZE + 0.5f, 11 * CELL_SIZE + 0.5f, 0.52f, -0.3f, parent);
        createBarrel(5 * CELL_SIZE + 0.5f, 13 * CELL_SIZE + 0.5f, parent);
        createUrn(9 * CELL_SIZE + 0.5f, 13 * CELL_SIZE + 0.5f, parent);
    }

    private void createBarrel(float x, float z, Node parent) {
        Node barrelNode = new Node("DungeonBarrel");

        // Stave body
        Cylinder bodyMesh = new Cylinder(12, 12, 0.32f, 0.72f, true);
        Geometry body = new Geometry("BarrelBody", bodyMesh);
        body.setMaterial(woodDarkMat);
        barrelNode.attachChild(body);

        // Dark iron hoops
        Cylinder hoopMesh = new Cylinder(12, 12, 0.335f, 0.04f, true);
        Geometry topHoop = new Geometry("BarrelHoopTop", hoopMesh);
        topHoop.setMaterial(ironMat);
        topHoop.setLocalTranslation(0f, 0.24f, 0f);
        barrelNode.attachChild(topHoop);

        Geometry midHoop1 = new Geometry("BarrelHoopMid1", hoopMesh);
        midHoop1.setMaterial(ironMat);
        midHoop1.setLocalTranslation(0f, 0.08f, 0f);
        barrelNode.attachChild(midHoop1);

        Geometry midHoop2 = new Geometry("BarrelHoopMid2", hoopMesh);
        midHoop2.setMaterial(ironMat);
        midHoop2.setLocalTranslation(0f, -0.08f, 0f);
        barrelNode.attachChild(midHoop2);

        Geometry btmHoop = new Geometry("BarrelHoopBtm", hoopMesh);
        btmHoop.setMaterial(ironMat);
        btmHoop.setLocalTranslation(0f, -0.24f, 0f);
        barrelNode.attachChild(btmHoop);

        barrelNode.setLocalTranslation(x, 0.36f, z);
        parent.attachChild(barrelNode);
    }

    private void createCrate(float x, float z, float size, float rotY, Node parent) {
        Node crateNode = new Node("SupplyCrate");

        Box boxMesh = new Box(size / 2.0f, size / 2.0f, size / 2.0f);
        Geometry crate = new Geometry("CrateBody", boxMesh);
        crate.setMaterial(woodPlankMat);
        crateNode.attachChild(crate);

        // Dark iron corner reinforcements
        Box cornerMesh = new Box(0.02f, size / 2.0f, 0.02f);
        float cOff = size / 2.0f - 0.015f;
        float[][] corners = {{-cOff, -cOff}, {cOff, -cOff}, {-cOff, cOff}, {cOff, cOff}};
        for (float[] c : corners) {
            Geometry corner = new Geometry("CrateCorner", cornerMesh);
            corner.setMaterial(ironMat);
            corner.setLocalTranslation(c[0], 0f, c[1]);
            crateNode.attachChild(corner);
        }

        crateNode.setLocalTranslation(x, size / 2.0f, z);
        crateNode.rotate(0f, rotY, 0f);
        parent.attachChild(crateNode);
    }

    private void createUrn(float x, float z, Node parent) {
        Node urnNode = new Node("CeramicUrn");

        Sphere bellyMesh = new Sphere(12, 12, 0.24f);
        Geometry belly = new Geometry("UrnBelly", bellyMesh);
        belly.setMaterial(urnCeramicMat);
        belly.setLocalTranslation(0f, 0.24f, 0f);
        urnNode.attachChild(belly);

        Cylinder neckMesh = new Cylinder(10, 10, 0.11f, 0.18f, true);
        Geometry neck = new Geometry("UrnNeck", neckMesh);
        neck.setMaterial(urnCeramicMat);
        neck.setLocalTranslation(0f, 0.44f, 0f);
        urnNode.attachChild(neck);

        Cylinder rimMesh = new Cylinder(10, 10, 0.15f, 0.04f, true);
        Geometry rim = new Geometry("UrnRim", rimMesh);
        rim.setMaterial(urnCeramicMat);
        rim.setLocalTranslation(0f, 0.54f, 0f);
        urnNode.attachChild(rim);

        urnNode.setLocalTranslation(x, 0f, z);
        parent.attachChild(urnNode);
    }

    private void createTreasureChest(float x, float z, float rotY, Node parent) {
        Node chestNode = new Node("TreasureChest");

        // Chest lower body
        Box chestBase = new Box(0.38f, 0.22f, 0.26f);
        Geometry base = new Geometry("ChestBase", chestBase);
        base.setMaterial(woodPlankMat);
        base.setLocalTranslation(0f, 0.22f, 0f);
        chestNode.attachChild(base);

        // Chest arched lid
        Cylinder lidMesh = new Cylinder(12, 12, 0.26f, 0.76f, true);
        Geometry lid = new Geometry("ChestLid", lidMesh);
        lid.setMaterial(woodDarkMat);
        lid.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Z));
        lid.setLocalTranslation(0f, 0.44f, 0f);
        chestNode.attachChild(lid);

        // Iron bands
        Box bandMesh = new Box(0.04f, 0.23f, 0.27f);
        Geometry bandL = new Geometry("ChestBandL", bandMesh);
        bandL.setMaterial(ironMat);
        bandL.setLocalTranslation(-0.25f, 0.22f, 0f);
        chestNode.attachChild(bandL);

        Geometry bandR = new Geometry("ChestBandR", bandMesh);
        bandR.setMaterial(ironMat);
        bandR.setLocalTranslation(0.25f, 0.22f, 0f);
        chestNode.attachChild(bandR);

        // Golden Lock Plate
        Box lockMesh = new Box(0.06f, 0.08f, 0.03f);
        Geometry lock = new Geometry("ChestLock", lockMesh);
        lock.setMaterial(goldMat);
        lock.setLocalTranslation(0f, 0.28f, 0.27f);
        chestNode.attachChild(lock);

        chestNode.setLocalTranslation(x, 0f, z);
        chestNode.rotate(0f, rotY, 0f);
        parent.attachChild(chestNode);
    }

    /**
     * Builds 3 distinct magical glowing crystal clusters in key mystical dungeon alcoves:
     * - Violet Arcane Crystals at [13, 1] (Key 1 alcove)
     * - Amber Citrine Crystals at [7, 3] (Treasury alcove)
     * - Cyan Mana Crystals at [1, 13] (South-West alcove)
     */
    private void buildMysticCrystals(Node parent) {
        // 1. Violet Amethyst Cluster at East Alcove [13, 1]
        float c1x = 13 * CELL_SIZE + CELL_SIZE / 2.0f + 0.85f;
        float c1z = 1 * CELL_SIZE + CELL_SIZE / 2.0f - 0.85f;
        buildCrystalFormation(parent, c1x, c1z, crystalVioletMat, new ColorRGBA(0.85f, 0.30f, 1.0f, 1.0f));

        // 2. Amber Citrine Cluster at Central Treasury [7, 3]
        float c2x = 7 * CELL_SIZE + CELL_SIZE / 2.0f - 0.95f;
        float c2z = 3 * CELL_SIZE + CELL_SIZE / 2.0f + 0.85f;
        buildCrystalFormation(parent, c2x, c2z, crystalAmberMat, new ColorRGBA(1.0f, 0.82f, 0.22f, 1.0f));

        // 3. Cyan Mana Cluster at South-West Grotto [1, 13]
        float c3x = 1 * CELL_SIZE + CELL_SIZE / 2.0f - 0.85f;
        float c3z = 13 * CELL_SIZE + CELL_SIZE / 2.0f + 0.85f;
        buildCrystalFormation(parent, c3x, c3z, crystalCyanMat, new ColorRGBA(0.20f, 0.95f, 1.0f, 1.0f));
    }

    private void buildCrystalFormation(Node parent, float x, float z, Material mat, ColorRGBA glowColor) {
        Node clusterNode = new Node("CrystalCluster");

        // Rocky base plinth
        Box rockBase = new Box(0.35f, 0.08f, 0.35f);
        Geometry rock = new Geometry("CrystalRockBase", rockBase);
        rock.setMaterial(wallDarkTrimMat);
        rock.setLocalTranslation(0f, 0.08f, 0f);
        clusterNode.attachChild(rock);

        // 5 Faceted Hexagonal Crystal Spikes
        float[][] spikeData = {
            {0.00f, 0.00f, 0.08f, 0.75f, 0.00f, 0.00f},   // Main central spire
            {-0.15f, 0.12f, 0.06f, 0.52f, 0.20f, -0.15f}, // North-West spire
            {0.16f, 0.10f, 0.05f, 0.44f, 0.25f, 0.18f},   // North-East spire
            {-0.14f, -0.12f, 0.06f, 0.58f, -0.18f, -0.12f},// South-West spire
            {0.14f, -0.14f, 0.05f, 0.38f, -0.22f, 0.20f}  // South-East spire
        };

        for (float[] data : spikeData) {
            Cylinder spikeMesh = new Cylinder(6, 6, data[2], data[3], true);
            Geometry spike = new Geometry("CrystalSpike", spikeMesh);
            spike.setMaterial(mat);

            Quaternion rot = new Quaternion();
            rot.fromAngles(data[4], 0f, data[5]);
            spike.setLocalRotation(rot);
            spike.setLocalTranslation(data[0], data[3] / 2.0f + 0.06f, data[1]);
            clusterNode.attachChild(spike);
        }

        // Soft Glowing Ambient Point Light
        PointLight light = new PointLight();
        light.setColor(glowColor);
        light.setRadius(7.0f);
        light.setPosition(new Vector3f(x, 0.6f, z));
        parent.addLight(light);

        clusterNode.setLocalTranslation(x, 0f, z);
        parent.attachChild(clusterNode);
        animatedCrystals.add(clusterNode);
    }

    /**
     * Builds the Monumental Exit Portal Gateway at [13, 13].
     * Includes dual portal pillars, gothic archway, twin standing flame braziers,
     * heavy double-banded oak doors with iron lock plate, and the dynamic ExitPortalCore.
     */
    private void buildExitPortal(Node parent) {
        float exitX = Maze15x15.EXIT_X * CELL_SIZE + CELL_SIZE / 2.0f;
        float exitZ = Maze15x15.EXIT_Z * CELL_SIZE + CELL_SIZE / 2.0f;

        Node exitGroup = new Node("ExitSanctuary");

        // 1. Stepped Stone Foundation Plinth
        Box plinthMesh = new Box(1.4f, 0.08f, 0.65f);
        Geometry plinth = new Geometry("ExitPlinth", plinthMesh);
        plinth.setMaterial(goldMat);
        plinth.setLocalTranslation(exitX, 0.08f, exitZ);
        exitGroup.attachChild(plinth);

        // 2. Twin Monumental Runic Pillars
        Box pillarMesh = new Box(0.24f, WALL_HEIGHT * 0.48f, 0.24f);
        Geometry leftPillar = new Geometry("ExitPillarLeft", pillarMesh);
        leftPillar.setMaterial(wallTrimMat);
        leftPillar.setLocalTranslation(exitX - 1.05f, WALL_HEIGHT * 0.48f, exitZ);
        exitGroup.attachChild(leftPillar);

        Geometry rightPillar = new Geometry("ExitPillarRight", pillarMesh);
        rightPillar.setMaterial(wallTrimMat);
        rightPillar.setLocalTranslation(exitX + 1.05f, WALL_HEIGHT * 0.48f, exitZ);
        exitGroup.attachChild(rightPillar);

        // Golden pillar bases and capitals
        Box capMesh = new Box(0.30f, 0.12f, 0.30f);
        Geometry leftCap = new Geometry("ExitCapL", capMesh);
        leftCap.setMaterial(goldMat);
        leftCap.setLocalTranslation(exitX - 1.05f, WALL_HEIGHT * 0.94f, exitZ);
        exitGroup.attachChild(leftCap);

        Geometry rightCap = new Geometry("ExitCapR", capMesh);
        rightCap.setMaterial(goldMat);
        rightCap.setLocalTranslation(exitX + 1.05f, WALL_HEIGHT * 0.94f, exitZ);
        exitGroup.attachChild(rightCap);

        // 3. Imposing Lintel Arch & Runic Keystone
        Box lintelMesh = new Box(1.25f, 0.22f, 0.26f);
        Geometry lintel = new Geometry("ExitLintel", lintelMesh);
        lintel.setMaterial(wallTrimMat);
        lintel.setLocalTranslation(exitX, WALL_HEIGHT * 0.88f, exitZ);
        exitGroup.attachChild(lintel);

        Box keystoneMesh = new Box(0.22f, 0.32f, 0.30f);
        Geometry keystone = new Geometry("ExitKeystone", keystoneMesh);
        keystone.setMaterial(goldMat);
        keystone.setLocalTranslation(exitX, WALL_HEIGHT * 0.88f, exitZ);
        exitGroup.attachChild(keystone);

        // 4. Double-Banded Oak Door Panels (which swing open on unlock)
        this.exitDoorPanelsNode = new Node("ExitDoorPanels");
        Box doorPanelMesh = new Box(0.42f, WALL_HEIGHT * 0.36f, 0.04f);

        Geometry leftDoor = new Geometry("ExitDoorLeft", doorPanelMesh);
        leftDoor.setMaterial(woodDarkMat);
        leftDoor.setLocalTranslation(exitX - 0.44f, WALL_HEIGHT * 0.38f, exitZ - 0.05f);
        exitDoorPanelsNode.attachChild(leftDoor);

        Geometry rightDoor = new Geometry("ExitDoorRight", doorPanelMesh);
        rightDoor.setMaterial(woodDarkMat);
        rightDoor.setLocalTranslation(exitX + 0.44f, WALL_HEIGHT * 0.38f, exitZ - 0.05f);
        exitDoorPanelsNode.attachChild(rightDoor);

        // Iron Lock Box & Crossbar
        Box lockPlateMesh = new Box(0.18f, 0.16f, 0.06f);
        Geometry lockPlate = new Geometry("ExitLockPlate", lockPlateMesh);
        lockPlate.setMaterial(ironMat);
        lockPlate.setLocalTranslation(exitX, WALL_HEIGHT * 0.40f, exitZ - 0.08f);
        exitDoorPanelsNode.attachChild(lockPlate);

        // 3 Keyholes on lock plate
        Sphere keyholeMesh = new Sphere(8, 8, 0.035f);
        for (int i = -1; i <= 1; i++) {
            Geometry kh = new Geometry("Keyhole_" + i, keyholeMesh);
            kh.setMaterial(goldMat);
            kh.setLocalTranslation(exitX + i * 0.09f, WALL_HEIGHT * 0.40f, exitZ - 0.12f);
            exitDoorPanelsNode.attachChild(kh);
        }

        exitGroup.attachChild(exitDoorPanelsNode);

        // 5. Glowing Mystic Portal Core (ExitPortalCore)
        Box portalMesh = new Box(0.82f, WALL_HEIGHT * 0.36f, 0.04f);
        Geometry portalCore = new Geometry("ExitPortalCore", portalMesh);
        Material coreMat = createUnshadedMaterial(new ColorRGBA(1.0f, 0.22f, 0.12f, 0.90f)); // Locked crimson
        portalCore.setMaterial(coreMat);
        portalCore.setLocalTranslation(exitX, WALL_HEIGHT * 0.40f, exitZ);
        exitGroup.attachChild(portalCore);

        // 6. Mystic Energy Rings Node (rotates when unlocked)
        this.exitPortalRingsNode = new Node("ExitPortalRings");
        Cylinder ringMesh = new Cylinder(16, 16, 0.70f, 0.03f, true);
        Geometry ring1 = new Geometry("EnergyRing1", ringMesh);
        ring1.setMaterial(coreMat);
        ring1.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
        exitPortalRingsNode.attachChild(ring1);
        exitPortalRingsNode.setLocalTranslation(exitX, WALL_HEIGHT * 0.40f, exitZ);
        exitGroup.attachChild(exitPortalRingsNode);

        // 7. Flanking Standing Flame Braziers
        buildStandingBrazier(exitGroup, exitX - 1.45f, exitZ - 0.5f);
        buildStandingBrazier(exitGroup, exitX + 1.45f, exitZ - 0.5f);

        // 8. Gateway Beacon Light
        PointLight exitLight = new PointLight();
        exitLight.setName("ExitBeaconLight");
        exitLight.setColor(new ColorRGBA(1.0f, 0.35f, 0.18f, 1.0f));
        exitLight.setRadius(14.0f);
        exitLight.setPosition(new Vector3f(exitX, WALL_HEIGHT * 0.5f, exitZ - 0.5f));
        exitGroup.addLight(exitLight);

        parent.attachChild(exitGroup);
    }

    private void buildStandingBrazier(Node parent, float bx, float bz) {
        Node brazierNode = new Node("StandingBrazier");

        // Stone pedestal
        Box baseMesh = new Box(0.20f, 0.35f, 0.20f);
        Geometry base = new Geometry("BrazierBase", baseMesh);
        base.setMaterial(wallTrimMat);
        base.setLocalTranslation(0f, 0.35f, 0f);
        brazierNode.attachChild(base);

        // Bronze / Iron Bowl
        Cylinder bowlMesh = new Cylinder(12, 12, 0.28f, 0.16f, true);
        Geometry bowl = new Geometry("BrazierBowl", bowlMesh);
        bowl.setMaterial(ironMat);
        bowl.setLocalTranslation(0f, 0.78f, 0f);
        brazierNode.attachChild(bowl);

        // Roaring Flame
        Sphere flameMesh = new Sphere(10, 10, 0.18f);
        Geometry flame = new Geometry("BrazierFlame", flameMesh);
        flame.setMaterial(torchFlameMat);
        flame.setLocalTranslation(0f, 0.94f, 0f);
        brazierNode.attachChild(flame);

        PointLight light = new PointLight();
        ColorRGBA flameColor = new ColorRGBA(1.0f, 0.72f, 0.30f, 1.0f);
        light.setColor(flameColor);
        light.setRadius(9.5f);
        light.setPosition(new Vector3f(bx, 1.1f, bz));
        parent.addLight(light);

        brazierNode.setLocalTranslation(bx, 0f, bz);
        parent.attachChild(brazierNode);

        animatedTorches.add(new TorchLightHolder(light, flame, flameColor, 9.5f, (float) Math.random() * FastMath.TWO_PI));
    }

    /**
     * Updates Exit Portal appearance when unlocked with all 3 keys:
     * Raises/opens heavy doors, turns the crimson barrier into a radiant emerald vortex,
     * and shifts beacon light to emerald sanctuary brilliance.
     */
    public void updateExitPortalCore(Spatial portalCore, boolean unlocked) {
        if (portalCore instanceof Geometry) {
            Geometry geo = (Geometry) portalCore;
            if (unlocked) {
                geo.setMaterial(createUnshadedMaterial(new ColorRGBA(0.15f, 1.0f, 0.45f, 0.95f)));
            } else {
                geo.setMaterial(createUnshadedMaterial(new ColorRGBA(1.0f, 0.22f, 0.12f, 0.90f)));
            }
        }

        // Open doors and reveal emerald vortex
        if (exitDoorPanelsNode != null) {
            exitDoorPanelsNode.setCullHint(unlocked ? Spatial.CullHint.Always : Spatial.CullHint.Inherit);
        }

        // Update beacon light color
        if (portalCore != null && portalCore.getParent() != null) {
            for (com.jme3.light.Light l : portalCore.getParent().getLocalLightList()) {
                if (l instanceof PointLight && "ExitBeaconLight".equals(l.getName())) {
                    PointLight pl = (PointLight) l;
                    if (unlocked) {
                        pl.setColor(new ColorRGBA(0.25f, 1.0f, 0.50f, 1.0f));
                        pl.setRadius(18.0f);
                    } else {
                        pl.setColor(new ColorRGBA(1.0f, 0.35f, 0.18f, 1.0f));
                        pl.setRadius(14.0f);
                    }
                }
            }
        }
    }

    /**
     * Sets up cohesive dungeon lighting:
     * Dark crypt ambient light, subtle directional fill, and atmospheric wall sconces.
     */
    private void setupDungeonLighting(Node parent) {
        // Cold crypt ambient lighting
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.14f, 0.14f, 0.19f, 1.0f));
        parent.addLight(ambient);

        // Directional fill light providing dimensional form and shadow relief
        DirectionalLight dirLight = new DirectionalLight();
        dirLight.setDirection(new Vector3f(-0.35f, -0.90f, -0.45f).normalizeLocal());
        dirLight.setColor(new ColorRGBA(0.16f, 0.18f, 0.24f, 1.0f));
        parent.addLight(dirLight);

        // Strategic wall torch sconces at key corridor junctions
        int[][] torchPositions = {
            {Maze15x15.START_X, Maze15x15.START_Z},
            {5, 1},
            {9, 1},
            {1, 5},
            {5, 5},
            {11, 5},
            {3, 7},
            {7, 7},
            {13, 7},
            {3, 11},
            {7, 11},
            {11, 11},
            {1, 13},
            {7, 13}
        };

        Sphere flameMesh = new Sphere(8, 8, 0.13f);
        Box bracketArm = new Box(0.04f, 0.18f, 0.04f);
        Box wallPlate = new Box(0.08f, 0.14f, 0.02f);
        Cylinder cupMesh = new Cylinder(8, 8, 0.10f, 0.08f, true);

        for (int[] pos : torchPositions) {
            float tx = pos[0] * CELL_SIZE + CELL_SIZE / 2.0f;
            float ty = 2.05f; // Torch sconce height
            float tz = pos[1] * CELL_SIZE + CELL_SIZE / 2.0f;

            Node torchNode = new Node("DungeonTorch");

            // Wrought-iron wall plate
            Geometry plate = new Geometry("TorchPlate", wallPlate);
            plate.setMaterial(ironMat);
            plate.setLocalTranslation(0f, 0f, 0f);
            torchNode.attachChild(plate);

            // Angled iron bracket
            Geometry bracket = new Geometry("TorchBracket", bracketArm);
            bracket.setMaterial(ironMat);
            bracket.setLocalTranslation(0f, 0.06f, 0.08f);
            torchNode.attachChild(bracket);

            // Fuel cup
            Geometry cup = new Geometry("TorchCup", cupMesh);
            cup.setMaterial(ironMat);
            cup.setLocalTranslation(0f, 0.18f, 0.14f);
            torchNode.attachChild(cup);

            // Radiant stylized flame core
            Geometry flame = new Geometry("TorchFlame", flameMesh);
            flame.setMaterial(torchFlameMat);
            flame.setLocalTranslation(0f, 0.32f, 0.14f);
            torchNode.attachChild(flame);

            torchNode.setLocalTranslation(tx, ty, tz);
            parent.attachChild(torchNode);

            // Warm Amber Point Light
            PointLight torch = new PointLight();
            ColorRGBA baseCol = new ColorRGBA(1.0f, 0.72f, 0.32f, 1.0f);
            torch.setColor(baseCol);
            torch.setRadius(10.5f);
            torch.setPosition(new Vector3f(tx, ty + 0.32f, tz + 0.14f));
            parent.addLight(torch);

            animatedTorches.add(new TorchLightHolder(torch, flame, baseCol, 10.5f, (float) Math.random() * FastMath.TWO_PI));
        }
    }

    /**
     * Updates dynamic dungeon atmosphere every frame:
     * Animates torch flame flickering, smooth light intensity fluctuations, and crystal pulses.
     */
    public void updateAtmosphere(float tpf) {
        atmosphereTime += tpf;

        // 1. Dynamic Torch Light Flickering
        for (TorchLightHolder th : animatedTorches) {
            float flicker = 1.0f + 0.09f * FastMath.sin(atmosphereTime * 8.2f + th.phase)
                                 + 0.05f * FastMath.cos(atmosphereTime * 14.7f + th.phase * 1.6f);

            th.light.setColor(new ColorRGBA(
                th.baseColor.r * flicker,
                th.baseColor.g * flicker,
                th.baseColor.b * flicker,
                1.0f
            ));

            if (th.flameGeo != null) {
                float fScale = 1.0f + 0.08f * FastMath.sin(atmosphereTime * 10.0f + th.phase);
                th.flameGeo.setLocalScale(fScale, fScale * 1.15f, fScale);
            }
        }

        // 2. Mystic Crystal Soft Pulse
        float pulse = 1.0f + 0.05f * FastMath.sin(atmosphereTime * 2.5f);
        for (Spatial s : animatedCrystals) {
            s.setLocalScale(1.0f, pulse, 1.0f);
        }

        // 3. Exit Portal Energy Rings Gentle Spin
        if (exitPortalRingsNode != null) {
            exitPortalRingsNode.rotate(0f, 0f, 1.4f * tpf);
        }
    }

    /**
     * Builds a procedural 3D Golden Key spatial with glowing rune core and warm point light.
     */
    public Node buildKeySpatial(int keyIndex) {
        Node keyNode = new Node("KeyNode_" + keyIndex);

        Material keyMat = createUnshadedMaterial(new ColorRGBA(1.0f, 0.88f, 0.25f, 1.0f));

        // Key bow / loop
        Box bowMesh = new Box(0.18f, 0.18f, 0.04f);
        Geometry bowGeo = new Geometry("KeyBow", bowMesh);
        bowGeo.setMaterial(keyMat);
        bowGeo.setLocalTranslation(0f, 0.35f, 0f);
        keyNode.attachChild(bowGeo);

        // Key shaft / stem
        Cylinder shaftMesh = new Cylinder(8, 8, 0.04f, 0.45f, true);
        Geometry shaftGeo = new Geometry("KeyShaft", shaftMesh);
        shaftGeo.setMaterial(keyMat);
        shaftGeo.setLocalTranslation(0f, 0.05f, 0f);
        keyNode.attachChild(shaftGeo);

        // Key bits / teeth
        Box tooth1 = new Box(0.10f, 0.04f, 0.04f);
        Geometry bit1 = new Geometry("KeyBit1", tooth1);
        bit1.setMaterial(keyMat);
        bit1.setLocalTranslation(0.08f, -0.10f, 0f);
        keyNode.attachChild(bit1);

        Box tooth2 = new Box(0.07f, 0.04f, 0.04f);
        Geometry bit2 = new Geometry("KeyBit2", tooth2);
        bit2.setMaterial(keyMat);
        bit2.setLocalTranslation(0.06f, -0.18f, 0f);
        keyNode.attachChild(bit2);

        // Warm amber point light
        PointLight light = new PointLight();
        light.setColor(new ColorRGBA(1.0f, 0.85f, 0.3f, 1.0f));
        light.setRadius(5.5f);
        light.setPosition(new Vector3f(0f, 0.2f, 0f));
        keyNode.addLight(light);

        return keyNode;
    }

    /**
     * Builds a procedural 3D Ancient Coin spatial with shining gold shimmer.
     */
    public Node buildCoinSpatial(int coinIndex) {
        Node coinNode = new Node("CoinNode_" + coinIndex);

        Material coinMat = createUnshadedMaterial(new ColorRGBA(1.0f, 0.78f, 0.18f, 1.0f));

        // Circular disc
        Cylinder discMesh = new Cylinder(16, 16, 0.22f, 0.06f, true);
        Geometry discGeo = new Geometry("CoinDisc", discMesh);
        discGeo.setMaterial(coinMat);
        coinNode.attachChild(discGeo);

        // Center embossed gem
        Sphere gemMesh = new Sphere(8, 8, 0.08f);
        Geometry gemGeo = new Geometry("CoinGem", gemMesh);
        gemGeo.setMaterial(createUnshadedMaterial(new ColorRGBA(0.95f, 0.95f, 0.5f, 1.0f)));
        coinNode.attachChild(gemGeo);

        // Subtle gleam light
        PointLight light = new PointLight();
        light.setColor(new ColorRGBA(1.0f, 0.8f, 0.3f, 1.0f));
        light.setRadius(3.5f);
        light.setPosition(new Vector3f(0f, 0.1f, 0f));
        coinNode.addLight(light);

        return coinNode;
    }

    /**
     * Builds a procedural 3D Maze Stalker enemy spatial.
     * Obsidian/Crimson hovering beast with faceted shoulders, horned crown, and glowing crimson eyes.
     */
    public Node buildEnemySpatial() {
        Node enemyNode = new Node("MazeStalkerEnemy");

        // Menacing faceted torso
        Box torsoMesh = new Box(0.35f, 0.55f, 0.35f);
        Geometry torsoGeo = new Geometry("StalkerTorso", torsoMesh);
        Material bodyMat = createLitMaterial(
            new ColorRGBA(0.12f, 0.02f, 0.02f, 1.0f),
            new ColorRGBA(0.08f, 0.01f, 0.01f, 1.0f),
            new ColorRGBA(0.6f, 0.1f, 0.1f, 1.0f),
            32.0f
        );
        torsoGeo.setMaterial(bodyMat);
        enemyNode.attachChild(torsoGeo);

        // Floating shoulder pauldrons
        Box pauldronMesh = new Box(0.16f, 0.22f, 0.22f);
        Geometry leftPauldron = new Geometry("PauldronLeft", pauldronMesh);
        leftPauldron.setMaterial(bodyMat);
        leftPauldron.setLocalTranslation(-0.46f, 0.28f, 0f);
        enemyNode.attachChild(leftPauldron);

        Geometry rightPauldron = new Geometry("PauldronRight", pauldronMesh);
        rightPauldron.setMaterial(bodyMat);
        rightPauldron.setLocalTranslation(0.46f, 0.28f, 0f);
        enemyNode.attachChild(rightPauldron);

        // Spiked horned crown
        Box hornMesh = new Box(0.08f, 0.24f, 0.08f);
        Geometry leftHorn = new Geometry("LeftHorn", hornMesh);
        leftHorn.setMaterial(bodyMat);
        leftHorn.setLocalTranslation(-0.25f, 0.68f, 0f);
        enemyNode.attachChild(leftHorn);

        Geometry rightHorn = new Geometry("RightHorn", hornMesh);
        rightHorn.setMaterial(bodyMat);
        rightHorn.setLocalTranslation(0.25f, 0.68f, 0f);
        enemyNode.attachChild(rightHorn);

        // Glowing fiery crimson eyes
        Material eyeMat = createUnshadedMaterial(new ColorRGBA(1.0f, 0.15f, 0.05f, 1.0f));

        Sphere eyeMesh = new Sphere(8, 8, 0.07f);
        Geometry leftEye = new Geometry("LeftEye", eyeMesh);
        leftEye.setMaterial(eyeMat);
        leftEye.setLocalTranslation(-0.16f, 0.30f, 0.36f);
        enemyNode.attachChild(leftEye);

        Geometry rightEye = new Geometry("RightEye", eyeMesh);
        rightEye.setMaterial(eyeMat);
        rightEye.setLocalTranslation(0.16f, 0.30f, 0.36f);
        enemyNode.attachChild(rightEye);

        // Chest rune sigil
        Box runeMesh = new Box(0.14f, 0.14f, 0.02f);
        Geometry rune = new Geometry("ChestRune", runeMesh);
        rune.setMaterial(eyeMat);
        rune.setLocalTranslation(0f, 0.08f, 0.36f);
        enemyNode.attachChild(rune);

        // Ominous Crimson Aura Point Light
        PointLight stalkerLight = new PointLight();
        stalkerLight.setName("StalkerLight");
        stalkerLight.setColor(new ColorRGBA(1.0f, 0.12f, 0.12f, 1.0f));
        stalkerLight.setRadius(9.0f);
        stalkerLight.setPosition(new Vector3f(0f, 0.4f, 0f));
        enemyNode.addLight(stalkerLight);

        return enemyNode;
    }
}
