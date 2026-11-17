package com.raycaster.engine;

import com.raycaster.ui.ShadingPalette;
import com.raycaster.ui.ShadingPalette.PaletteType;

/**
 * Converts ray-casting hit distances into perspective-projected ASCII wall slices,
 * floor gradients, and ceiling rendering across a 2D character buffer.
 */
public class Renderer {

    private final int screenWidth;
    private final int screenHeight;
    private PaletteType palette;

    public Renderer(int screenWidth, int screenHeight, PaletteType palette) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.palette = palette != null ? palette : PaletteType.UNICODE_BLOCKS;
    }

    public PaletteType getPalette() {
        return palette;
    }

    public void setPalette(PaletteType palette) {
        this.palette = palette;
    }

    public void cyclePalette() {
        PaletteType[] values = PaletteType.values();
        int nextIndex = (palette.ordinal() + 1) % values.length;
        this.palette = values[nextIndex];
    }

    /**
     * Renders the 3D first-person perspective into a 2D character buffer.
     *
     * @param hits Array of RayHit results, one for each screen column
     * @return 2D character array of dimensions [screenHeight][screenWidth]
     */
    public char[][] renderFrame(RayHit[] hits) {
        char[][] buffer = new char[screenHeight][screenWidth];
        int horizonY = screenHeight / 2;

        for (int x = 0; x < screenWidth; x++) {
            RayHit hit = hits[x];

            if (!hit.isHit()) {
                // Ray struck nothing within max range: render open horizon
                for (int y = 0; y < horizonY; y++) {
                    buffer[y][x] = ' ';
                }
                for (int y = horizonY; y < screenHeight; y++) {
                    double normFloor = (double) (screenHeight - 1 - y) / Math.max(1, horizonY);
                    buffer[y][x] = ShadingPalette.getFloorChar(normFloor, palette);
                }
                continue;
            }

            // Calculate apparent wall height on screen
            double perpDist = Math.max(0.01, hit.getPerpDistance());
            int lineHeight = (int) (screenHeight / perpDist);

            // Calculate top and bottom pixels of the wall slice
            int drawStart = -lineHeight / 2 + horizonY;
            int drawEnd = lineHeight / 2 + horizonY;

            int clampedStart = Math.max(0, drawStart);
            int clampedEnd = Math.min(screenHeight - 1, drawEnd);

            char wallChar = ShadingPalette.getWallChar(perpDist, hit.getSide(), palette);

            // Render ceiling
            for (int y = 0; y < clampedStart; y++) {
                double normCeil = (double) y / Math.max(1, horizonY);
                buffer[y][x] = ShadingPalette.getCeilingChar(normCeil, palette);
            }

            // Render wall slice
            for (int y = clampedStart; y <= clampedEnd; y++) {
                buffer[y][x] = wallChar;
            }

            // Render floor
            for (int y = clampedEnd + 1; y < screenHeight; y++) {
                double normFloor = (double) (screenHeight - 1 - y) / Math.max(1, horizonY);
                buffer[y][x] = ShadingPalette.getFloorChar(normFloor, palette);
            }
        }

        return buffer;
    }

    /**
     * Converts the 2D character buffer into formatted display lines with optional decorative border.
     */
    public String[] formatFrame(char[][] buffer, boolean withBorder) {
        if (!withBorder) {
            String[] lines = new String[screenHeight];
            for (int y = 0; y < screenHeight; y++) {
                lines[y] = new String(buffer[y]);
            }
            return lines;
        }

        String[] lines = new String[screenHeight + 2];
        String horizontalBorder = "+" + "-".repeat(screenWidth) + "+";
        lines[0] = horizontalBorder;
        for (int y = 0; y < screenHeight; y++) {
            lines[y + 1] = "|" + new String(buffer[y]) + "|";
        }
        lines[screenHeight + 1] = horizontalBorder;
        return lines;
    }
}
