package catan.interfaceadapter.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import catan.usecase.longestroad.LongestRoadInputBoundary;
import catan.usecase.longestroad.LongestRoadInputData;
import org.junit.jupiter.api.Test;

/** Tests for LongestRoadController. */
class LongestRoadControllerTest {

  /** Fake input boundary used to capture requests. */
  private static class TestInputBoundary implements LongestRoadInputBoundary {

    private LongestRoadInputData receivedData;
    private int callCount;

    @Override
    public void execute(LongestRoadInputData inputData) {
      receivedData = inputData;
      callCount++;
    }
  }

  @Test
  void executeCallsInputBoundary() {
    TestInputBoundary input = new TestInputBoundary();
    LongestRoadController controller = new LongestRoadController(input);

    controller.execute();

    assertEquals(1, input.callCount);
    assertInstanceOf(LongestRoadInputData.class, input.receivedData);
  }

  @Test
  void executeCanBeCalledMultipleTimes() {
    TestInputBoundary input = new TestInputBoundary();
    LongestRoadController controller = new LongestRoadController(input);

    controller.execute();
    controller.execute();

    assertEquals(2, input.callCount);
  }

  @Test
  void constructorRejectsNullInputBoundary() {
    assertThrows(NullPointerException.class, () -> new LongestRoadController(null));
  }
}
