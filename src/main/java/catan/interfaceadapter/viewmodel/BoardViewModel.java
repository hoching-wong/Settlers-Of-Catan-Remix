package catan.interfaceadapter.viewmodel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Observer pattern: presenters change the {@link BoardState} and call
 * {@link #firePropertyChanged()}; every registered view (the board panel, a status panel, ...)
 * is told and redraws. Presenters never need to know which views exist.
 */
public class BoardViewModel {
    public static final String STATE_PROPERTY = "boardState";

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private BoardState state = new BoardState();

    public BoardState getState() {
        return state;
    }

    public void setState(BoardState state) {
        this.state = state;
    }

    public void firePropertyChanged() {
        support.firePropertyChange(STATE_PROPERTY, null, state);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
