package catan.interfaceadapter.presenter;

/** The wording the player sees, kept in one place so the presenters agree. */
final class Text {
    static final String PLACE_SETTLEMENT = "Place a settlement";
    static final String PLACE_ROAD = "Place a road next to your new settlement";
    static final String ROLL_DICE = "Roll the dice";
    static final String BUILD_OR_END_TURN = "Build or end your turn";

    private Text() {
    }

    /** LUMBER becomes Lumber. */
    static String title(Enum<?> value) {
        String name = value.name();
        return title(name);
    }

    /** LUMBER becomes Lumber. */
    static String title(String name) {
        return name.charAt(0) + name.substring(1).toLowerCase();
    }
}
