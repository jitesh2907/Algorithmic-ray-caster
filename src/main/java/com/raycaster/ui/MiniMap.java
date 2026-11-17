package com.raycaster.ui;

import com.raycaster.core.MazeMap;
import com.raycaster.core.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates an interactive top-down 2D mini-map showing the maze layout,
 * player coordinates, and viewing direction indicator.
 */
public class MiniMap {

    private final boolean unicode;

    public MiniMap(boolean unicode) {
        this.unicode = unicode;
    }

    /**
     * Generates text lines representing the mini-map.
     * Each cell is rendered 2 characters wide to compensate for rectangular terminal character aspect ratio.
     *
     * @param map Active maze map
     * @param player Current player
     * @return List of formatted strings
     */
    public List<String> renderMiniMap(MazeMap map, Player player) {
        List<String> lines = new ArrayList<>();
        int mapW = map.getWidth();
        int mapH = map.getHeight();

        int playerCellX = (int) Math.floor(player.getX());
        int playerCellY = (int) Math.floor(player.getY());

        char playerIcon = player.getOrientationChar(unicode);
        String wallCell = unicode ? "██" : "##";
        String emptyCell = "  ";

        // Title and Top border
        String border = "+" + "-".repeat(mapW * 2) + "+";
        lines.add(border);

        for (int y = 0; y < mapH; y++) {
            StringBuilder row = new StringBuilder();
            row.append("|");
            for (int x = 0; x < mapW; x++) {
                if (x == playerCellX && y == playerCellY) {
                    row.append(playerIcon).append(' ');
                } else if (map.isWall(x, y)) {
                    row.append(wallCell);
                } else {
                    row.append(emptyCell);
                }
            }
            row.append("|");
            lines.add(row.toString());
        }

        // Bottom border
        lines.add(border);
        return lines;
    }

    /**
     * Renders a compact side-by-side HUD view merging 3D viewport with MiniMap on the right.
     */
    public static String[] mergeSideBySide(String[] viewportLines, List<String> mapLines, int gapSpaces) {
        int totalRows = Math.max(viewportLines.length, mapLines.size());
        String[] combined = new String[totalRows];
        String gap = " ".repeat(gapSpaces);

        int vpWidth = viewportLines.length > 0 ? viewportLines[0].length() : 0;
        String blankVp = " ".repeat(vpWidth);

        for (int i = 0; i < totalRows; i++) {
            String vpPart = (i < viewportLines.length) ? viewportLines[i] : blankVp;
            String mapPart = (i < mapLines.size()) ? mapLines.get(i) : "";
            combined[i] = vpPart + gap + mapPart;
        }

        return combined;
    }
}
