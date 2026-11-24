package catan.usecase.generateboard;

import catan.usecase.boardstate.BoardSnapshot;

/** The newly generated board, ready to be drawn. */
public record GenerateBoardOutputData(BoardSnapshot board) {}
