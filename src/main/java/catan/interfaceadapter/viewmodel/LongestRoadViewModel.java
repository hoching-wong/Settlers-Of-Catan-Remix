package catan.interfaceadapter.viewmodel;

/** Stores Longest Road information for the view. */
public class LongestRoadViewModel extends ViewModel {

  private String holderName;
  private int roadLength;

  /**
   * Returns the name of the current Longest Road holder.
   *
   * @return the holder's name, or null if there is no holder
   */
  public String getHolderName() {
    return holderName;
  }

  /**
   * Returns the length of the current holder's longest road.
   *
   * @return the road length
   */
  public int getRoadLength() {
    return roadLength;
  }

  /**
   * Updates the Longest Road display state.
   *
   * @param newHolderName the new holder's name, or null
   * @param newRoadLength the new holder's road length
   */
  public void updateState(String newHolderName, int newRoadLength) {
    holderName = newHolderName;
    roadLength = newHolderName == null ? 0 : newRoadLength;

    firePropertyChanged("longestRoad", this);
  }
}
