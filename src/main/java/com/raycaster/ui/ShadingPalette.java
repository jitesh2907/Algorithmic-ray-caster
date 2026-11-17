package com.raycaster.ui;

/**
 * Manages ASCII and Unicode shading character sets for walls, floors, and ceilings.
 * Differentiates depth (distance) and lighting angle (X-side vs Y-side wall hits).
 */
public class ShadingPalette {

    public enum PaletteType {
        CLASSIC_ASCII("Classic ASCII (7-bit portable)"),
        UNICODE_BLOCKS("Unicode Block Shades"),
        HIGH_CONTRAST("High Contrast Letters");

        private final String displayName;

        PaletteType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Resolves the wall character based on distance, hit surface orientation, and palette type.
     *
     * @param distance Perpendicular distance to the wall
     * @param side 0 for vertical (X-side), 1 for horizontal (Y-side)
     * @param palette Selected palette
     * @return Character representing the wall slice
     */
    public static char getWallChar(double distance, int side, PaletteType palette) {
        switch (palette) {
            case UNICODE_BLOCKS:
                return getUnicodeWallChar(distance, side);
            case HIGH_CONTRAST:
                return getHighContrastWallChar(distance, side);
            case CLASSIC_ASCII:
            default:
                return getClassicAsciiWallChar(distance, side);
        }
    }

    private static char getClassicAsciiWallChar(double distance, int side) {
        if (distance < 1.5) {
            return (side == 0) ? '@' : '#';
        } else if (distance < 3.0) {
            return (side == 0) ? '%' : '$';
        } else if (distance < 5.0) {
            return (side == 0) ? '*' : '+';
        } else if (distance < 8.0) {
            return (side == 0) ? '=' : '-';
        } else if (distance < 12.0) {
            return (side == 0) ? ':' : '~';
        } else {
            return '.';
        }
    }

    private static char getUnicodeWallChar(double distance, int side) {
        if (distance < 1.5) {
            return (side == 0) ? '█' : '▓';
        } else if (distance < 3.0) {
            return (side == 0) ? '▓' : '▒';
        } else if (distance < 5.0) {
            return (side == 0) ? '▒' : '░';
        } else if (distance < 8.0) {
            return (side == 0) ? '░' : ':';
        } else {
            return '.';
        }
    }

    private static char getHighContrastWallChar(double distance, int side) {
        if (distance < 1.5) {
            return (side == 0) ? 'M' : 'W';
        } else if (distance < 3.0) {
            return (side == 0) ? 'N' : 'H';
        } else if (distance < 5.0) {
            return (side == 0) ? 'X' : 'I';
        } else if (distance < 8.0) {
            return (side == 0) ? '!' : '|';
        } else {
            return ':';
        }
    }

    /**
     * Resolves floor character for row y (from horizon down to screen bottom).
     *
     * @param normalizedDepth Depth value from 0.0 (near player's feet) to 1.0 (horizon)
     * @param palette Selected palette
     * @return Character representing floor at that depth
     */
    public static char getFloorChar(double normalizedDepth, PaletteType palette) {
        if (palette == PaletteType.UNICODE_BLOCKS) {
            if (normalizedDepth < 0.25) return '▒';
            if (normalizedDepth < 0.50) return '░';
            if (normalizedDepth < 0.80) return '.';
            return ' ';
        } else {
            if (normalizedDepth < 0.25) return '=';
            if (normalizedDepth < 0.50) return '-';
            if (normalizedDepth < 0.80) return '.';
            return ' ';
        }
    }

    /**
     * Resolves ceiling character for row y (from top of screen down to horizon).
     */
    public static char getCeilingChar(double normalizedDepth, PaletteType palette) {
        // Subtle roof stippling near apex
        if (normalizedDepth < 0.15) {
            return '`';
        }
        return ' ';
    }
}
