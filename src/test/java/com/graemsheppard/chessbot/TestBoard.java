package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.enums.Color;
import com.graemsheppard.chessbot.pieces.King;
import com.graemsheppard.chessbot.pieces.Piece;

/**
 * An empty test board with only Kings in their default location
 */
public class TestBoard extends Board {

    public TestBoard() {
        grid = new Piece[8][8];
    }

    @Override
    public void setOnBoard(Piece piece) {
        super.setOnBoard(piece);
        if (piece instanceof King king) {
            if (piece.getColor() == Color.WHITE)
                wKing = king;
            else if (piece.getColor() == Color.BLACK)
                bKing = king;
        }
    }
}
