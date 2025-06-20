package com.graemsheppard.chessbot;

public interface ChessContext {

    Board getBoard(long id);

    Board getCachedBoard(long id);

    Board buildBoard(long id);

    boolean addMove(String move);

}
