package catan.framework.ui.swing;

import catan.interfaceadapter.viewmodel.LeaderboardRow;
import catan.interfaceadapter.viewmodel.LeaderboardViewModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * The content of the leaderboard dialog. Shows the rows, or the error message, or says that
 * nothing has been recorded yet. It only reads {@link LeaderboardViewModel}.
 */
public class LeaderboardPanel extends JPanel implements PropertyChangeListener {

    static final String TITLE = "Leaderboard";
    static final String NO_RECORDS_TEXT = "No records yet.";

    private final LeaderboardViewModel viewModel;
    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[] {"Rank", "Name", "Wins"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
    private final JTable table = new JTable(tableModel);
    private final JScrollPane tableScroll = new JScrollPane(table);
    private final JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);

    public LeaderboardPanel(LeaderboardViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel");

        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setPreferredSize(new Dimension(360, 240));

        JLabel title = new JLabel(TITLE, SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        add(title, BorderLayout.NORTH);

        table.setName("leaderboardTable");
        table.setRowSelectionAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        messageLabel.setName("leaderboardMessage");

        JPanel center = new JPanel(new BorderLayout());
        center.add(tableScroll, BorderLayout.CENTER);
        center.add(messageLabel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        viewModel.addPropertyChangeListener(this);
        redraw();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (SwingUtilities.isEventDispatchThread()) {
            redraw();
        } else {
            SwingUtilities.invokeLater(this::redraw);
        }
    }

    private void redraw() {
        tableModel.setRowCount(0);
        for (LeaderboardRow row : viewModel.getRows()) {
            tableModel.addRow(new Object[] {row.rank(), row.name(), row.wins()});
        }
        String error = viewModel.getErrorMessage();
        boolean hasRows = tableModel.getRowCount() > 0;

        table.setVisible(hasRows);
        tableScroll.setVisible(hasRows);
        messageLabel.setVisible(!hasRows);
        messageLabel.setText(error.isEmpty() ? NO_RECORDS_TEXT : error);
        revalidate();
        repaint();
    }
}
