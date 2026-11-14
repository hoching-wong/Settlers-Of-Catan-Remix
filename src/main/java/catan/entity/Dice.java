package catan.entity;

import java.util.Random;

/** Two six-sided dice. Pass a seeded Random for deterministic tests. */
public class Dice {
    private int die1;
    private int die2;
    private final Random random;

    public Dice(Random random) {
        this.random = random;
    }

    public void roll() {
        die1 = random.nextInt(6) + 1;
        die2 = random.nextInt(6) + 1;
    }

    public int getDie1() {
        return die1;
    }

    public int getDie2() {
        return die2;
    }

    public int getTotal() {
        return die1 + die2;
    }
}
