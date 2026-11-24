package catan.framework.ui.swing;

import catan.interfaceadapter.viewmodel.BoardPoint;
import catan.interfaceadapter.viewmodel.BoardState;
import catan.interfaceadapter.viewmodel.BoardViewModel;
import catan.interfaceadapter.viewmodel.EdgeView;
import catan.interfaceadapter.viewmodel.TileView;
import catan.interfaceadapter.viewmodel.VertexView;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * Draws the board from the {@link BoardViewModel} and turns mouse clicks into "vertex 12 was
 * clicked" or "edge 40 was clicked". It holds no game logic: what a click means is up to the
 * {@link BoardClickListener}s.
 */
public class BoardPanel extends JPanel {
    /** Half the width and height of the board in board units, plus a margin of sea. */
    private static final double HALF_WIDTH = Math.sqrt(3) * 2.5 + 0.55;
    private static final double HALF_HEIGHT = 4.0 + 0.55;
    /** How close (in board units) the mouse must be to pick a corner or an edge. */
    private static final double PICK_RADIUS = 0.32;

    private final BoardViewModel viewModel;
    private final List<BoardClickListener> listeners = new ArrayList<>();
    private int hoverVertex = -1;
    private int hoverEdge = -1;

    public BoardPanel(BoardViewModel viewModel) {
        this.viewModel = viewModel;
        setPreferredSize(new Dimension(720, 640));
        setBackground(new Color(0xEDE6D6));
        viewModel.addPropertyChangeListener(evt -> repaint());

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHover(e.getX(), e.getY());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverVertex = -1;
                hoverEdge = -1;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public void addClickListener(BoardClickListener listener) {
        listeners.add(listener);
    }

    // ---- coordinates ----

    private double scale() {
        return Math.min(getWidth() / (2 * HALF_WIDTH), getHeight() / (2 * HALF_HEIGHT));
    }

    private Point2D.Double toScreen(BoardPoint p) {
        double s = scale();
        return new Point2D.Double(getWidth() / 2.0 + p.x() * s, getHeight() / 2.0 + p.y() * s);
    }

    private BoardPoint toBoard(int x, int y) {
        double s = scale();
        return new BoardPoint((x - getWidth() / 2.0) / s, (y - getHeight() / 2.0) / s);
    }

    // ---- picking ----

    /** The vertex under the mouse, or -1. Only corners are considered while placing on corners. */
    private int vertexAt(BoardPoint p) {
        String kind = viewModel.getState().getTargetKind();
        if (BoardState.EDGE.equals(kind)) {
            return -1;
        }
        int best = -1;
        double bestDist = PICK_RADIUS;
        for (VertexView v : viewModel.getState().getVertices()) {
            double d = Math.hypot(v.position().x() - p.x(), v.position().y() - p.y());
            if (d < bestDist) {
                bestDist = d;
                best = v.id();
            }
        }
        return best;
    }

    /** The edge under the mouse, or -1. */
    private int edgeAt(BoardPoint p) {
        String kind = viewModel.getState().getTargetKind();
        if (BoardState.VERTEX.equals(kind)) {
            return -1;
        }
        int best = -1;
        double bestDist = PICK_RADIUS;
        for (EdgeView e : viewModel.getState().getEdges()) {
            double d = Line2D.ptSegDist(e.from().x(), e.from().y(), e.to().x(), e.to().y(), p.x(), p.y());
            // stay away from the ends so a click near a corner picks the corner
            double toMid = Math.hypot(e.midpoint().x() - p.x(), e.midpoint().y() - p.y());
            if (d < bestDist && toMid < 0.4) {
                bestDist = d;
                best = e.id();
            }
        }
        return best;
    }

    private void updateHover(int x, int y) {
        BoardPoint p = toBoard(x, y);
        int v = vertexAt(p);
        int e = v >= 0 ? -1 : edgeAt(p);
        if (v != hoverVertex || e != hoverEdge) {
            hoverVertex = v;
            hoverEdge = e;
            repaint();
        }
    }

    private void handleClick(int x, int y) {
        BoardPoint p = toBoard(x, y);
        int v = vertexAt(p);
        if (v >= 0) {
            for (BoardClickListener l : listeners) {
                l.vertexClicked(v);
            }
            return;
        }
        int e = edgeAt(p);
        if (e >= 0) {
            for (BoardClickListener l : listeners) {
                l.edgeClicked(e);
            }
        }
    }

    // ---- drawing ----

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        BoardState state = viewModel.getState();
        double s = scale();

        drawSea(g, s);
        for (TileView tile : state.getTiles()) {
            drawTile(g, tile, s);
        }
        for (EdgeView edge : state.getEdges()) {
            boolean highlighted = state.getHighlightedEdgeIds().contains(edge.id());
            if (edge.hasRoad()) {
                drawRoad(g, edge, s);
            } else if (highlighted) {
                drawEdgeHighlight(g, edge, s, edge.id() == hoverEdge);
            }
        }
        for (VertexView vertex : state.getVertices()) {
            boolean highlighted = state.getHighlightedVertexIds().contains(vertex.id());
            if (!vertex.isEmpty()) {
                drawBuilding(g, vertex, s, highlighted, vertex.id() == hoverVertex);
            } else if (highlighted) {
                drawVertexHighlight(g, vertex, s, vertex.id() == hoverVertex);
            }
        }
        g.dispose();
    }

    private void drawSea(Graphics2D g, double s) {
        double w = 2 * HALF_WIDTH * s;
        double h = 2 * HALF_HEIGHT * s;
        double x = (getWidth() - w) / 2;
        double y = (getHeight() - h) / 2;
        RoundRectangle2D sea = new RoundRectangle2D.Double(x, y, w, h, s * 1.2, s * 1.2);
        g.setColor(BoardColors.SEA);
        g.fill(sea);
        g.setColor(BoardColors.SEA_EDGE);
        g.setStroke(new BasicStroke((float) (s * 0.08)));
        g.draw(sea);
    }

    private void drawTile(Graphics2D g, TileView tile, double s) {
        Path2D hex = new Path2D.Double();
        for (int i = 0; i < tile.corners().size(); i++) {
            Point2D.Double p = toScreen(tile.corners().get(i));
            if (i == 0) {
                hex.moveTo(p.x, p.y);
            } else {
                hex.lineTo(p.x, p.y);
            }
        }
        hex.closePath();
        g.setColor(BoardColors.terrain(tile.terrain()));
        g.fill(hex);
        g.setColor(BoardColors.HEX_BORDER);
        g.setStroke(new BasicStroke((float) (s * 0.07), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(hex);

        Point2D.Double c = toScreen(tile.centre());
        // what the tile produces, above the token
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(9, (int) (s * 0.2))));
        drawCentredText(g, tile.label(), c.x, c.y - s * 0.55, new Color(0, 0, 0, 90), 1);
        drawCentredText(g, tile.label(), c.x, c.y - s * 0.55, Color.WHITE, 0);

        if (tile.hasNumberToken()) {
            drawToken(g, tile, c, s);
        }
    }

    private void drawToken(Graphics2D g, TileView tile, Point2D.Double c, double s) {
        double r = s * 0.34;
        Ellipse2D token = new Ellipse2D.Double(c.x - r, c.y - r, 2 * r, 2 * r);
        g.setColor(new Color(0, 0, 0, 60));
        g.fill(new Ellipse2D.Double(c.x - r + 2, c.y - r + 3, 2 * r, 2 * r));
        g.setColor(BoardColors.TOKEN_FILL);
        g.fill(token);
        g.setColor(BoardColors.TOKEN_BORDER);
        g.setStroke(new BasicStroke((float) Math.max(1, s * 0.025)));
        g.draw(token);

        Color ink = tile.red() ? BoardColors.TOKEN_RED : BoardColors.TOKEN_TEXT;
        g.setFont(new Font(Font.SERIF, Font.BOLD, Math.max(10, (int) (s * (tile.red() ? 0.34 : 0.3)))));
        drawCentredText(g, String.valueOf(tile.numberToken()), c.x, c.y - s * 0.04, ink, 0);

        double dot = Math.max(2, s * 0.045);
        double gap = dot * 1.7;
        double startX = c.x - (tile.pips() - 1) * gap / 2;
        g.setColor(ink);
        for (int i = 0; i < tile.pips(); i++) {
            g.fill(new Ellipse2D.Double(startX + i * gap - dot / 2, c.y + s * 0.17, dot, dot));
        }
    }

    private void drawRoad(Graphics2D g, EdgeView edge, double s) {
        Point2D.Double a = toScreen(shrink(edge.from(), edge.to()));
        Point2D.Double b = toScreen(shrink(edge.to(), edge.from()));
        g.setStroke(new BasicStroke((float) (s * 0.2), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(BoardColors.OUTLINE);
        g.draw(new Line2D.Double(a, b));
        g.setStroke(new BasicStroke((float) (s * 0.13), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(BoardColors.player(edge.ownerIndex()));
        g.draw(new Line2D.Double(a, b));
    }

    /** Pulls an edge's end in a little so roads don't cover the corner pieces. */
    private static BoardPoint shrink(BoardPoint from, BoardPoint to) {
        double t = 0.18;
        return new BoardPoint(from.x() + (to.x() - from.x()) * t, from.y() + (to.y() - from.y()) * t);
    }

    private void drawEdgeHighlight(Graphics2D g, EdgeView edge, double s, boolean hover) {
        Point2D.Double a = toScreen(shrink(edge.from(), edge.to()));
        Point2D.Double b = toScreen(shrink(edge.to(), edge.from()));
        float width = (float) (s * (hover ? 0.17 : 0.11));
        g.setColor(BoardColors.OUTLINE);
        g.setStroke(new BasicStroke(width + (float) (s * 0.05), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(a, b));
        g.setColor(hover ? BoardColors.HIGHLIGHT_HOVER : BoardColors.HIGHLIGHT);
        g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(a, b));
    }

    private void drawVertexHighlight(Graphics2D g, VertexView vertex, double s, boolean hover) {
        Point2D.Double p = toScreen(vertex.position());
        double r = s * (hover ? 0.19 : 0.14);
        Ellipse2D ring = new Ellipse2D.Double(p.x - r, p.y - r, 2 * r, 2 * r);
        g.setColor(hover ? BoardColors.HIGHLIGHT_HOVER : BoardColors.HIGHLIGHT);
        g.fill(ring);
        g.setColor(BoardColors.OUTLINE);
        g.setStroke(new BasicStroke((float) Math.max(1, s * 0.025)));
        g.draw(ring);
    }

    private void drawBuilding(Graphics2D g, VertexView vertex, double s, boolean highlighted, boolean hover) {
        Point2D.Double p = toScreen(vertex.position());
        boolean city = "CITY".equals(vertex.building());
        if (highlighted) {
            // a settlement that can be upgraded to a city
            double r = s * (hover ? 0.36 : 0.31);
            g.setColor(hover ? BoardColors.HIGHLIGHT_HOVER : BoardColors.HIGHLIGHT);
            g.fill(new Ellipse2D.Double(p.x - r, p.y - r, 2 * r, 2 * r));
        }
        Path2D shape = city ? cityShape(p, s) : settlementShape(p, s);
        g.setColor(BoardColors.player(vertex.ownerIndex()));
        g.fill(shape);
        g.setColor(BoardColors.OUTLINE);
        g.setStroke(new BasicStroke((float) Math.max(1.2, s * 0.035), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g.draw(shape);
    }

    /** A little house. */
    private static Path2D settlementShape(Point2D.Double p, double s) {
        double w = s * 0.17;
        double h = s * 0.13;
        Path2D house = new Path2D.Double();
        house.moveTo(p.x - w, p.y + h);
        house.lineTo(p.x - w, p.y - h * 0.3);
        house.lineTo(p.x, p.y - h * 1.5);
        house.lineTo(p.x + w, p.y - h * 0.3);
        house.lineTo(p.x + w, p.y + h);
        house.closePath();
        return house;
    }

    /** A house with a tall tower on the left. */
    private static Path2D cityShape(Point2D.Double p, double s) {
        double u = s * 0.11;
        Path2D city = new Path2D.Double();
        city.moveTo(p.x - 2.2 * u, p.y + 1.4 * u);
        city.lineTo(p.x - 2.2 * u, p.y - 1.4 * u);
        city.lineTo(p.x - 1.1 * u, p.y - 2.5 * u);
        city.lineTo(p.x, p.y - 1.4 * u);
        city.lineTo(p.x, p.y - 0.4 * u);
        city.lineTo(p.x + 2.2 * u, p.y - 0.4 * u);
        city.lineTo(p.x + 2.2 * u, p.y + 1.4 * u);
        city.closePath();
        return city;
    }

    private static void drawCentredText(Graphics2D g, String text, double cx, double cy, Color color, int offset) {
        FontMetrics fm = g.getFontMetrics();
        int x = (int) Math.round(cx - fm.stringWidth(text) / 2.0) + offset;
        int y = (int) Math.round(cy + (fm.getAscent() - fm.getDescent()) / 2.0) + offset;
        g.setColor(color);
        g.drawString(text, x, y);
    }
}
