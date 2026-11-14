package catan.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;
import org.junit.jupiter.api.Test;

class DiceTest {
    @Test
    void rollStaysInRangeAndTotalIsSum() {
        Dice dice = new Dice(new Random(42));
        for (int i = 0; i < 1000; i++) {
            dice.roll();
            assertTrue(dice.getDie1() >= 1 && dice.getDie1() <= 6);
            assertTrue(dice.getDie2() >= 1 && dice.getDie2() <= 6);
            assertEquals(dice.getDie1() + dice.getDie2(), dice.getTotal());
        }
    }

    @Test
    void sameSeedGivesSameRolls() {
        Dice a = new Dice(new Random(7));
        Dice b = new Dice(new Random(7));
        for (int i = 0; i < 50; i++) {
            a.roll();
            b.roll();
            assertEquals(a.getDie1(), b.getDie1());
            assertEquals(a.getDie2(), b.getDie2());
        }
    }

    @Test
    void usesTheRandomGiven() {
        // nextInt(6) returning 2 then 4 means dice 3 and 5
        int[] values = {2, 4};
        int[] index = {0};
        Random fake = new Random() {
            @Override
            public int nextInt(int bound) {
                return values[index[0]++];
            }
        };
        Dice dice = new Dice(fake);
        dice.roll();
        assertEquals(3, dice.getDie1());
        assertEquals(5, dice.getDie2());
        assertEquals(8, dice.getTotal());
    }
}
