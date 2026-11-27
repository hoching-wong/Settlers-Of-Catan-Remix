package catan.usecase.recordwin;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RecordWinInputDataTest {

    @Test
    void keepsTheWinnerName() {
        assertEquals("Alice", new RecordWinInputData("Alice").winnerName());
    }

    @Test
    void rejectsANullWinnerName() {
        assertThrows(NullPointerException.class, () -> new RecordWinInputData(null));
    }

    @Test
    void rejectsAnEmptyOrBlankWinnerName() {
        assertThrows(IllegalArgumentException.class, () -> new RecordWinInputData(""));
        assertThrows(IllegalArgumentException.class, () -> new RecordWinInputData("   "));
    }
}
