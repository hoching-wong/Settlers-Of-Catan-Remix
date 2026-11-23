package usecase.build.rules;

/** The rule chains used by the build use cases, in the order the checks run. */
public final class BuildRules {
    private BuildRules() {
    }

    /** Before choosing a location: game in progress, dice rolled, enough resources. */
    public static BuildRule beforeChoosingLocation() {
        BuildRule head = new GameInProgressRule();
        head.linkWith(new DiceRolledRule())
                .linkWith(new AffordabilityRule());
        return head;
    }

    /** Placing on a location: everything above, then the location itself. */
    public static BuildRule forPlacing() {
        BuildRule head = new GameInProgressRule();
        head.linkWith(new DiceRolledRule())
                .linkWith(new AffordabilityRule())
                .linkWith(new ValidLocationRule());
        return head;
    }
}
