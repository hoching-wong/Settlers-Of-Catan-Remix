package catan.usecase.selectstructure;

/**
 * Part of use case 1: the player picks a structure from the build menu and the system works out
 * where it can go, so the board can highlight those places before the player clicks.
 */
public interface SelectStructureInputBoundary {
    void execute(SelectStructureInputData inputData);
}
