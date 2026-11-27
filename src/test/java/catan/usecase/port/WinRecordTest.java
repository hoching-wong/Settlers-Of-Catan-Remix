package catan.usecase.port;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;
import org.junit.jupiter.api.Test;

class WinRecordTest {

    @Test
    void keepsTheNameAndTheWins() {
        WinRecord record = new WinRecord("Alice", 3);

        assertEquals("Alice", record.name());
        assertEquals(3, record.wins());
    }

    @Test
    void trimsSurroundingWhitespaceFromTheNameButKeepsInnerSpaces() {
        assertEquals("Alice", new WinRecord("  Alice ", 1).name());
        assertEquals("Al ice", new WinRecord(" Al ice ", 1).name());
    }

    @Test
    void rejectsANullName() {
        assertThrows(NullPointerException.class, () -> new WinRecord(null, 1));
    }

    @Test
    void rejectsAnEmptyOrBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new WinRecord("", 1));
        assertThrows(IllegalArgumentException.class, () -> new WinRecord("   ", 1));
    }

    @Test
    void rejectsNegativeWins() {
        assertThrows(IllegalArgumentException.class, () -> new WinRecord("Alice", -1));
    }

    @Test
    void allowsZeroWins() {
        assertDoesNotThrow(() -> new WinRecord("Alice", 0));
    }

    @Test
    void keyIgnoresCaseAndSurroundingWhitespace() {
        String key = WinRecord.keyOf("Alice");

        assertEquals(key, WinRecord.keyOf("alice"));
        assertEquals(key, WinRecord.keyOf(" ALICE "));
    }

    @Test
    void keyKeepsDifferentNamesApart() {
        assertNotEquals(WinRecord.keyOf("Alice"), WinRecord.keyOf("Alicia"));
        assertNotEquals(WinRecord.keyOf("Al ice"), WinRecord.keyOf("Alice"));
    }

    @Test
    void keyDoesNotDependOnTheDefaultLocale() {
        Locale original = Locale.getDefault();
        try {
            // In Turkish, a default-locale toLowerCase would turn "I" into a dotless "ı".
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            assertEquals(WinRecord.keyOf("kit"), WinRecord.keyOf("KIT"));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void instanceKeyMatchesKeyOfTheName() {
        WinRecord record = new WinRecord("  Alice ", 2);

        assertEquals(WinRecord.keyOf("alice"), record.key());
    }
}
