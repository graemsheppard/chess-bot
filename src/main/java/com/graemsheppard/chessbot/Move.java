package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.pieces.Piece;
import lombok.Getter;

public class Move {

    @Getter
    private final Piece piece;
    @Getter
    private final Location destination;
    @Getter
    private final MoveType moveType;

    public Move (Piece piece, Location destination, MoveType moveType) {
        this.piece = piece;
        this.destination = destination;
        this.moveType = moveType;
    }

}
