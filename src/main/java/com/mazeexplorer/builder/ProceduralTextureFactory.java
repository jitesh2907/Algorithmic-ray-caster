package com.mazeexplorer.builder;

import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Generates stylized low-poly procedural textures in memory using Java AWT and converts
 * them to jMonkeyEngine Texture2D objects.
 * Eliminates external asset dependencies while providing consistent, rich visual surface detail
 * for stone architecture, flagstone pavers, wooden planks, forged iron, and mystic runes.
 */
public final class ProceduralTextureFactory {

    private static final AWTLoader AWT_LOADER = new AWTLoader();

    private static Texture2D stoneWallTexture;
    private static Texture2D floorSlateTexture;
    private static Texture2D floorMossTexture;
    private static Texture2D floorSandstoneTexture;
    private static Texture2D floorBloodstoneTexture;
    private static Texture2D floorRunicTexture;
    private static Texture2D woodPlankTexture;
    private static Texture2D ironTexture;

    private ProceduralTextureFactory() {}

    /**
     * Stylized Ashlar Stone Wall: Staggered beveled stone blocks with dark recessed mortar lines.
     */
    public static synchronized Texture2D getStoneWallTexture() {
        if (stoneWallTexture == null) {
            int size = 128;
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Mortar background
            g.setColor(new Color(42, 40, 38));
            g.fillRect(0, 0, size, size);

            // 4 Courses of staggered stone bricks
            int rows = 4;
            int rowH = size / rows;
            for (int r = 0; r < rows; r++) {
                int y = r * rowH;
                int cols = 3;
                int colW = size / cols;
                int xOffset = (r % 2 == 0) ? 0 : colW / 2;

                for (int c = -1; c <= cols; c++) {
                    int x = c * colW + xOffset;
                    // Brick body
                    int baseTone = 100 + ((r * 13 + c * 19) % 25);
                    Color brickColor = new Color(baseTone, baseTone - 3, baseTone - 7);
                    g.setColor(brickColor);
                    g.fillRect(x + 2, y + 2, colW - 4, rowH - 4);

                    // Top/Left highlight bevel
                    g.setColor(new Color(baseTone + 30, baseTone + 27, baseTone + 23));
                    g.drawLine(x + 2, y + 2, x + colW - 3, y + 2);
                    g.drawLine(x + 2, y + 2, x + 2, y + rowH - 3);

                    // Bottom/Right shadow bevel
                    g.setColor(new Color(baseTone - 35, baseTone - 38, baseTone - 42));
                    g.drawLine(x + 2, y + rowH - 3, x + colW - 3, y + rowH - 3);
                    g.drawLine(x + colW - 3, y + 2, x + colW - 3, y + rowH - 3);
                }
            }
            g.dispose();
            stoneWallTexture = toTexture2D(img);
        }
        return stoneWallTexture;
    }

    /**
     * Stylized Floor Flagstone Pavers: 4 large beveled paver stones with dark mortar grooves.
     */
    public static synchronized Texture2D getFloorSlateTexture() {
        if (floorSlateTexture == null) {
            floorSlateTexture = createPaverTexture(
                new Color(75, 72, 70), // Base slate stone
                new Color(32, 30, 28), // Dark mortar
                new Color(105, 102, 100), // Highlight
                new Color(45, 43, 40)     // Shadow
            );
        }
        return floorSlateTexture;
    }

    public static synchronized Texture2D getFloorMossTexture() {
        if (floorMossTexture == null) {
            floorMossTexture = createPaverTexture(
                new Color(65, 75, 58), // Damp mossy stone
                new Color(28, 35, 25), // Damp dark mortar
                new Color(90, 105, 80), // Moss highlight
                new Color(40, 48, 35)   // Shadow
            );
        }
        return floorMossTexture;
    }

    public static synchronized Texture2D getFloorSandstoneTexture() {
        if (floorSandstoneTexture == null) {
            floorSandstoneTexture = createPaverTexture(
                new Color(100, 88, 70), // Warm sandstone
                new Color(45, 38, 28),  // Dark mortar
                new Color(130, 115, 92), // Sunlit highlight
                new Color(65, 55, 42)    // Shadow
            );
        }
        return floorSandstoneTexture;
    }

    public static synchronized Texture2D getFloorBloodstoneTexture() {
        if (floorBloodstoneTexture == null) {
            floorBloodstoneTexture = createPaverTexture(
                new Color(62, 22, 22), // Crypt bloodstone
                new Color(25, 8, 8),   // Dark crimson mortar
                new Color(95, 35, 35), // Ruby highlight
                new Color(38, 12, 12)  // Deep shadow
            );
        }
        return floorBloodstoneTexture;
    }

    private static Texture2D createPaverTexture(Color baseColor, Color mortarColor, Color hiColor, Color shadowColor) {
        int size = 128;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Mortar fill
        g.setColor(mortarColor);
        g.fillRect(0, 0, size, size);

        // 2x2 Large Flagstones
        int half = size / 2;
        int m = 4; // Mortar margin

        int[][] paverBounds = {
            {m, m, half - 2 * m, half - 2 * m},
            {half + m, m, half - 2 * m, half - 2 * m},
            {m, half + m, half - 2 * m, half - 2 * m},
            {half + m, half + m, half - 2 * m, half - 2 * m}
        };

        for (int[] b : paverBounds) {
            int px = b[0], py = b[1], pw = b[2], ph = b[3];
            g.setColor(baseColor);
            g.fillRect(px, py, pw, ph);

            // Highlight top/left
            g.setColor(hiColor);
            g.drawLine(px, py, px + pw, py);
            g.drawLine(px, py, px, py + ph);

            // Shadow bottom/right
            g.setColor(shadowColor);
            g.drawLine(px, py + ph, px + pw, py + ph);
            g.drawLine(px + pw, py, px + pw, py + ph);
        }

        g.dispose();
        return toTexture2D(img);
    }

    /**
     * Runic Dais: Mystical emerald circular glyphs with compass markers and ancient protective runes.
     */
    public static synchronized Texture2D getFloorRunicTexture() {
        if (floorRunicTexture == null) {
            int size = 128;
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Dark stone background
            g.setColor(new Color(30, 48, 36));
            g.fillRect(0, 0, size, size);

            // Outer runic ring
            g.setColor(new Color(60, 200, 110));
            g.setStroke(new BasicStroke(3.0f));
            g.drawOval(10, 10, size - 20, size - 20);

            // Inner circle
            g.setStroke(new BasicStroke(2.0f));
            g.drawOval(26, 26, size - 52, size - 52);

            // Center star / compass cross
            g.drawLine(size / 2, 14, size / 2, size - 14);
            g.drawLine(14, size / 2, size - 14, size / 2);

            // 4 Diagonal runic spokes
            g.drawLine(34, 34, size - 34, size - 34);
            g.drawLine(size - 34, 34, 34, size - 34);

            // Glowing center gem core
            g.setColor(new Color(120, 255, 160));
            g.fillOval(size / 2 - 8, size / 2 - 8, 16, 16);

            g.dispose();
            floorRunicTexture = toTexture2D(img);
        }
        return floorRunicTexture;
    }

    /**
     * Weathered Dark Oak Timber: Horizontal wooden planks with subtle grain fibers and dark iron rivets.
     */
    public static synchronized Texture2D getWoodPlankTexture() {
        if (woodPlankTexture == null) {
            int size = 128;
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int planks = 4;
            int pH = size / planks;

            for (int i = 0; i < planks; i++) {
                int y = i * pH;
                int tone = 70 + (i * 11) % 18;
                g.setColor(new Color(tone + 20, tone, tone - 20)); // Rich timber brown
                g.fillRect(0, y, size, pH);

                // Subtle wood grain lines
                g.setColor(new Color(tone + 10, tone - 8, tone - 28));
                g.drawLine(0, y + pH / 3, size, y + pH / 3);
                g.drawLine(0, y + 2 * pH / 3, size, y + 2 * pH / 3);

                // Top highlight
                g.setColor(new Color(tone + 35, tone + 15, tone - 10));
                g.drawLine(0, y, size, y);

                // Bottom seam shadow
                g.setColor(new Color(25, 14, 8));
                g.drawLine(0, y + pH - 1, size, y + pH - 1);

                // Iron nail rivets at ends
                g.setColor(new Color(40, 40, 42));
                g.fillOval(8, y + pH / 2 - 2, 4, 4);
                g.fillOval(size - 12, y + pH / 2 - 2, 4, 4);
            }

            g.dispose();
            woodPlankTexture = toTexture2D(img);
        }
        return woodPlankTexture;
    }

    /**
     * Forged Wrought Iron: Dark mottled metal with subtle metallic noise and specular sheen.
     */
    public static synchronized Texture2D getIronTexture() {
        if (ironTexture == null) {
            int size = 64;
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();

            g.setColor(new Color(45, 45, 48));
            g.fillRect(0, 0, size, size);

            // Subtle hammered metal highlights
            g.setColor(new Color(65, 65, 70));
            for (int y = 4; y < size; y += 8) {
                for (int x = 4; x < size; x += 8) {
                    g.fillRect(x, y, 3, 3);
                }
            }

            g.dispose();
            ironTexture = toTexture2D(img);
        }
        return ironTexture;
    }

    private static Texture2D toTexture2D(BufferedImage img) {
        Image jmeImg = AWT_LOADER.load(img, true);
        Texture2D tex = new Texture2D(jmeImg);
        tex.setWrap(Texture.WrapMode.Repeat);
        tex.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
        tex.setMagFilter(Texture.MagFilter.Bilinear);
        return tex;
    }
}

