package catan.framework.ui.swing;

import catan.interfaceadapter.controller.BuildController;
import catan.interfaceadapter.controller.SelectStructureController;
import catan.interfaceadapter.viewmodel.BoardState;
import catan.interfaceadapter.viewmodel.BoardViewModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

/**
 * The board with its build menu (Road, Settlement, City) and a message line.
 *
 * <p>Choosing a structure calls the select-structure controller, which highlights where it can
 * go. Clicking a highlighted corner or edge calls the build controller. The view never decides
 * whether a build is allowed; it only passes the click on.
 */
public class BoardView extends JPanel implements PropertyChangeListener, BoardClickListener {
    public static final String VIEW_NAME = "board";

    private static final Color ERROR_COLOUR = new Color(0xB71C1C);
    private static final Color INFO_COLOUR = new Color(0x263238);

    private final BoardViewModel viewModel;
    private final BoardPanel boardPanel;
    private final ButtonGroup buildButtons = new ButtonGroup();
    private final Map<String, JToggleButton> buttonsByStructure = new LinkedHashMap<>();
    private final JLabel messageLabel = new JLabel(" ");

    private SelectStructureController selectStructureController;
    private BuildController buildController;

    public BoardView(BoardViewModel viewModel) {
        this.viewModel = viewModel;
        this.boardPanel = new BoardPanel(viewModel);
        viewModel.addPropertyChangeListener(this);
        boardPanel.addClickListener(this);

        setLayout(new BorderLayout());
        add(buildMenu(), BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);

        messageLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 8, 10));
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN, 14f));
        add(messageLabel, BorderLayout.SOUTH);
    }

    private JPanel buildMenu() {
        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        menu.add(new JLabel("Build:"));
        addBuildButton(menu, "ROAD", "Road", "1 Lumber + 1 Brick");
        addBuildButton(menu, "SETTLEMENT", "Settlement", "1 Lumber + 1 Brick + 1 Wool + 1 Grain");
        addBuildButton(menu, "CITY", "City", "2 Grain + 3 Ore (upgrades a settlement)");

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> cancelBuilding());
        menu.add(cancel);
        return menu;
    }

    private void addBuildButton(JPanel menu, String structure, String text, String cost) {
        JToggleButton button = new JToggleButton(text);
        button.setToolTipText(text + ": " + cost);
        button.addActionListener(e -> {
            if (selectStructureController != null) {
                selectStructureController.execute(structure);
            }
        });
        buildButtons.add(button);
        buttonsByStructure.put(structure, button);
        menu.add(button);
    }

    /** Leaves build mode. This only changes what is shown, so no use case is needed. */
    private void cancelBuilding() {
        BoardState state = viewModel.getState();
        state.clearSelection();
        state.setMessage(" ", false);
        viewModel.firePropertyChanged();
    }

    public void setSelectStructureController(SelectStructureController controller) {
        this.selectStructureController = controller;
    }

    public void setBuildController(BuildController controller) {
        this.buildController = controller;
    }

    /** So other views (for example initial placement) can listen to clicks on the same board. */
    public BoardPanel getBoardPanel() {
        return boardPanel;
    }

    @Override
    public void vertexClicked(int vertexId) {
        build(BoardState.VERTEX, vertexId);
    }

    @Override
    public void edgeClicked(int edgeId) {
        build(BoardState.EDGE, edgeId);
    }

    private void build(String clickedKind, int id) {
        BoardState state = viewModel.getState();
        if (buildController == null || state.getSelectedStructure() == null
                || !clickedKind.equals(state.getTargetKind())) {
            return;
        }
        buildController.execute(state.getSelectedStructure(), id);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Presenters may run on a background thread (the game start), so update on the Swing thread.
        if (SwingUtilities.isEventDispatchThread()) {
            refresh();
        } else {
            SwingUtilities.invokeLater(this::refresh);
        }
    }

    private void refresh() {
        BoardState state = viewModel.getState();
        String selected = state.getSelectedStructure();
        if (selected == null) {
            buildButtons.clearSelection();
        } else if (buttonsByStructure.containsKey(selected)) {
            buttonsByStructure.get(selected).setSelected(true);
        }
        String message = state.getMessage().isBlank() ? " " : state.getMessage();
        messageLabel.setText(message);
        messageLabel.setForeground(state.isError() ? ERROR_COLOUR : INFO_COLOUR);
    }
}
