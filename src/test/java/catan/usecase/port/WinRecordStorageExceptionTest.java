package catan.usecase.port;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class WinRecordStorageExceptionTest {

  @Test
  void keepsTheMessageAndTheCause() {
    IOException cause = new IOException("disk full");

    WinRecordStorageException withCause = new WinRecordStorageException("Could not save.", cause);
    WinRecordStorageException withoutCause = new WinRecordStorageException("Could not read.");

    assertEquals("Could not save.", withCause.getMessage());
    assertSame(cause, withCause.getCause());
    assertEquals("Could not read.", withoutCause.getMessage());
    assertNull(withoutCause.getCause());
  }

  @Test
  void corruptedDataIsAKindOfStorageFailure() {
    IllegalStateException cause = new IllegalStateException("bad json");

    Exception caught = new CorruptedWinRecordsException("The records are corrupted.", cause);

    assertInstanceOf(WinRecordStorageException.class, caught);
    assertEquals("The records are corrupted.", caught.getMessage());
    assertSame(cause, caught.getCause());
    assertNull(new CorruptedWinRecordsException("No cause.").getCause());
  }

  @Test
  void isACheckedExceptionSoCallersMustHandleIt() {
    assertFalse(RuntimeException.class.isAssignableFrom(WinRecordStorageException.class));
  }
}
