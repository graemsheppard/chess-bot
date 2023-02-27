package com.graemsheppard.chessbot.pieces;

import com.graemsheppard.chessbot.Color;
import com.graemsheppard.chessbot.Location;
import com.graemsheppard.chessbot.MoveType;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {

    private final int value = 3;

    public Knight(Color color, Location location) {
        super(color, location);
        this.character = 'N';
        this.codePoint = this.color == Color.BLACK ? '\u265e' : '\u2658';
    }

    @Override
    public List<Location> getValidMoves(Piece[][] board, MoveType type) {
        List<Location> possibleMoves = new ArrayList<>();
        for (int k = 0; k < 2; k++) {
            for (int i : new int[] { 1, -1 }) {
                for (int j : new int[] { 2, -2 }) {
                    Location newLocation;
                    if (k == 0)
                        newLocation = this.location.addRanks(i).addFiles(j);
                    else
                        newLocation = this.location.addRanks(j).addFiles(i);
                    if (newLocation.isValid()) {
                        Piece piece = tileOccupied(newLocation, board);
                        if (piece == null || !piece.color.equals(this.color)) {
                            possibleMoves.add(newLocation);
                        }
                    }
                }
            }
        }
        return possibleMoves;
    }

    public String getImgPath() {
        return this.color == Color.WHITE ? "wKnight.png" : "bKnight.png";
    }
}
