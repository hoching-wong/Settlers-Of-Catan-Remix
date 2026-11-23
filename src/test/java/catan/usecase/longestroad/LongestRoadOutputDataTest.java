package catan.usecase.longestroad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Tests for LongestRoadOutputData. */
class LongestRoadOutputDataTest {

  @Test
  void storesHolderInformation() {
    LongestRoadOutputData data = new LongestRoadOutputData("Alice", 6);

    assertEquals("Alice", data.holderName());
    assertEquals(6, data.roadLength());
  }

  @Test
  void allowsNoHolder() {
    LongestRoadOutputData data = new LongestRoadOutputData(null, 0);

    assertNull(data.holderName());
    assertEquals(0, data.roadLength());
  }

  @Test
  void rejectsNegativeRoadLength() {
    assertThrows(IllegalArgumentException.class, () -> new LongestRoadOutputData("Alice", -1));
  }

  @Test
  void rejectsLengthWithoutHolder() {
    assertThrows(IllegalArgumentException.class, () -> new LongestRoadOutputData(null, 5));
  }
}
