package catan.framework.ui.swing;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;

/** Helpers for tests that drive Swing components without showing a window. */
final class SwingTestSupport {
    private SwingTestSupport() {
    }

    /** The component with this name below root, or null. */
    static <T extends Component> T find(Container root, String name, Class<T> type) {
        for (Component c : root.getComponents()) {
            if (name.equals(c.getName()) && type.isInstance(c)) {
                return type.cast(c);
            }
            if (c instanceof Container inner) {
                T found = find(inner, name, type);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /** Waits for everything already queued on the Swing thread. */
    static void flush() {
        try {
            SwingUtilities.invokeAndWait(() -> { });
        } catch (InterruptedException | InvocationTargetException e) {
            throw new AssertionError(e);
        }
    }
}
