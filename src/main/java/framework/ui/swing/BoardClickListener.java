package framework.ui.swing;

/**
 * Told when the player clicks a corner or an edge of the board. The build view listens for
 * this; the initial placement view can listen too, so the same board panel works for setup.
 */
public interface BoardClickListener {
    void vertexClicked(int vertexId);

    void edgeClicked(int edgeId);
}
