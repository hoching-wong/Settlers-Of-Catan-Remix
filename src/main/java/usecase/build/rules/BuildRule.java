package usecase.build.rules;

import java.util.Optional;

/**
 * Chain of Responsibility pattern: one check a build must pass. Rules are linked into a chain;
 * each rule either rejects the request with a message or hands it to the next rule. The first
 * failure stops the chain, so the player sees the most important problem first (for example
 * "roll the dice" before "not enough resources").
 *
 * <p>To add a rule (say, a build limit per turn), write a new subclass and link it in
 * {@link BuildRules}. No existing rule or interactor has to change.
 */
public abstract class BuildRule {
    private BuildRule next;

    /** Sets the rule that runs after this one and returns it, so calls can be chained. */
    public BuildRule linkWith(BuildRule nextRule) {
        this.next = nextRule;
        return nextRule;
    }

    /** Runs this rule and, if it passes, the rest of the chain. Empty means every rule passed. */
    public final Optional<String> check(BuildRequest request) {
        Optional<String> error = checkThis(request);
        if (error.isPresent() || next == null) {
            return error;
        }
        return next.check(request);
    }

    /** This rule only. Empty if it passes, otherwise the message to show the player. */
    protected abstract Optional<String> checkThis(BuildRequest request);
}
