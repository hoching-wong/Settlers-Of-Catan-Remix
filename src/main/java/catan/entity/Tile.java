package catan.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** A hexagonal tile. The desert has no resource and no number token. */
public class Tile {
    /** Value of {@link #getNumberToken()} for a tile with no token (the desert). */
    public static final int NO_TOKEN = 0;

    private final int id;
    private final TerrainType terrain;
    private final ResourceType resource;
    private final int numberToken;
    private final List<Vertex> vertices = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    /**
     * @param numberToken 2-12 except 7 for a resource tile, {@link #NO_TOKEN} for the desert
     */
    public Tile(int id, TerrainType terrain, int numberToken) {
        this.id = id;
        this.terrain = Objects.requireNonNull(terrain, "terrain");
        this.resource = resourceFor(terrain);
        if (terrain == TerrainType.DESERT) {
            if (numberToken != NO_TOKEN) {
                throw new IllegalArgumentException("The desert has no number token");
            }
        } else if (numberToken < 2 || numberToken > 12 || numberToken == 7) {
            throw new IllegalArgumentException("Invalid number token: " + numberToken);
        }
        this.numberToken = numberToken;
    }

    /** The resource a terrain produces, or null for the desert. */
    public static ResourceType resourceFor(TerrainType terrain) {
        return switch (terrain) {
            case FOREST -> ResourceType.LUMBER;
            case HILLS -> ResourceType.BRICK;
            case FIELDS -> ResourceType.GRAIN;
            case PASTURE -> ResourceType.WOOL;
            case MOUNTAINS -> ResourceType.ORE;
            case DESERT -> null;
        };
    }

    void addVertex(Vertex vertex) {
        vertices.add(vertex);
    }

    void addEdge(Edge edge) {
        edges.add(edge);
    }

    public int getId() {
        return id;
    }

    public TerrainType getTerrain() {
        return terrain;
    }

    /** Null for the desert. */
    public ResourceType getResourceType() {
        return resource;
    }

    /** {@link #NO_TOKEN} for the desert. */
    public int getNumberToken() {
        return numberToken;
    }

    public boolean hasNumberToken() {
        return numberToken != NO_TOKEN;
    }

    public List<Vertex> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }
}
