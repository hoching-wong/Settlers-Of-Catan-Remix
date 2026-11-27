package catan.usecase.port;

import java.util.Locale;
import java.util.Objects;

/**
 * One player's total number of wins, as kept between runs of the game.
 *
 * <p>Two names that differ only in letter case or surrounding whitespace are the same player.
 * Compare them with {@link #key()}, not with {@code equals}. The name is stored without
 * surrounding whitespace, in the spelling it was first recorded with.
 *
 * @param name the player's name, never blank
 * @param wins the total number of wins, never negative
 */
public record WinRecord(String name, int wins) {

    public WinRecord {
        Objects.requireNonNull(name, "name");
        name = name.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("The name must not be blank");
        }
        if (wins < 0) {
            throw new IllegalArgumentException("The wins must not be negative");
        }
    }

    /** The key that identifies a player: the name without surrounding whitespace, in lower case. */
    public static String keyOf(String name) {
        Objects.requireNonNull(name, "name");
        return name.trim().toLowerCase(Locale.ROOT);
    }

    /** The key that identifies this record's player. */
    public String key() {
        return keyOf(name);
    }
}
