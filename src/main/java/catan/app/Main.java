package catan.app;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppBuilder().buildWindow().setVisible(true));
    }
}
