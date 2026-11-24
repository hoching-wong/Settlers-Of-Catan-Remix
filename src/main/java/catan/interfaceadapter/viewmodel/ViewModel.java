package catan.interfaceadapter.viewmodel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** Base for the view models. Views register a listener and redraw when a property changes. */
public abstract class ViewModel {
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    /** Tells the listeners that a property changed. Always fires, even if the value is unchanged. */
    protected void firePropertyChanged(String propertyName, Object newValue) {
        support.firePropertyChange(new PropertyChangeEvent(this, propertyName, null, newValue));
    }
}
