package catan.framework.ui.swing;

import java.awt.Color;
import java.util.Map;

/** The colours used to draw the board. Only the Swing layer knows about colours. */
public final class BoardColors {
    public static final Color SEA = new Color(0x2F6F9F);
    public static final Color SEA_EDGE = new Color(0x245A82);
    public static final Color HEX_BORDER = new Color(0xF3E7C9);
    public static final Color TOKEN_FILL = new Color(0xFBF4E2);
    public static final Color TOKEN_BORDER = new Color(0x6B5B3E);
    public static final Color TOKEN_TEXT = new Color(0x2B2B2B);
    public static final Color TOKEN_RED = new Color(0xC62828);
    public static final Color OUTLINE = new Color(0x1E1E1E);
    public static final Color HIGHLIGHT = new Color(255, 214, 64, 235);
    public static final Color HIGHLIGHT_HOVER = new Color(255, 255, 255, 250);

    private static final Map<String, Color> TERRAIN = Map.of(
            "FOREST", new Color(0x2E7D32),
            "HILLS", new Color(0xC0562B),
            "FIELDS", new Color(0xE8B83A),
            "PASTURE", new Color(0x8BC34A),
            "MOUNTAINS", new Color(0x8A94A6),
            "DESERT", new Color(0xE6D3A3));

    /** Red, blue, orange, white: the classic Catan player colours, in player order. */
    private static final Color[] PLAYERS = {
        new Color(0xE53935), new Color(0x1E63D6), new Color(0xF57C00), new Color(0xF5F5F5)
    };

    private BoardColors() {
    }

    public static Color terrain(String terrain) {
        return TERRAIN.getOrDefault(terrain, Color.GRAY);
    }

    public static Color player(int index) {
        return index < 0 ? Color.GRAY : PLAYERS[index % PLAYERS.length];
    }
}
