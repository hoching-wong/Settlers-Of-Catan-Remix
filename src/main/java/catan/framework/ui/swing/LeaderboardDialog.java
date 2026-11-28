package catan.framework.ui.swing;

import java.awt.BorderLayout;
import java.awt.Window;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/** A modal window around the {@link LeaderboardPanel}. Create one each time it is needed. */
public class LeaderboardDialog extends JDialog {

    /**
     * @param owner the window it opens over, or null
     * @param panel the content to show
     */
    public LeaderboardDialog(Window owner, LeaderboardPanel panel) {
        super(owner, LeaderboardPanel.TITLE, ModalityType.APPLICATION_MODAL);
        Objects.requireNonNull(panel, "panel");

        JButton close = new JButton("Close");
        close.setName("close");
        close.addActionListener(event -> dispose());
        JPanel buttons = new JPanel();
        buttons.add(close);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getRootPane().setDefaultButton(close);
        pack();
        setLocationRelativeTo(owner);
    }

    /** Shows the dialog and waits until the player closes it. */
    public void open() {
        setVisible(true);
    }
}
