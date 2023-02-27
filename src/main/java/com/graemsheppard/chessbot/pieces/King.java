package com.graemsheppard.chessbot.pieces;

import com.graemsheppard.chessbot.Color;
import com.graemsheppard.chessbot.Location;
import com.graemsheppard.chessbot.MoveType;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {

    private final int value = Integer.MAX_VALUE;

    public King(Color color, Location location) {
        super(color, location);
        this.character = 'K';
        this. codePoint = this.color == Color.BLACK ? '\u265a' : '\u2654';

    }

    public List<Location> getValidMoves(Piece[][] board, MoveType type) {
        List<Location> possibleMoves = new ArrayList<>();
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (i == 0 && j == 0) continue;
                Location newLocation = this.location.addFiles(i).addRanks(j);
                if (newLocation.isValid()) {
                    Piece piece = tileOccupied(newLocation, board);
                    if (piece == null || !piece.color.equals(this.color)) {
                        possibleMoves.add(newLocation);
                    }
                }
            }
        }
        return possibleMoves;
    }

    public String getImgPath() {
        return this.color == Color.WHITE ? "wKing.png" : "bKing.png";
    }
}
