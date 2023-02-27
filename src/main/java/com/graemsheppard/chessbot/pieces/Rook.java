package com.graemsheppard.chessbot.pieces;

import com.graemsheppard.chessbot.Color;
import com.graemsheppard.chessbot.Location;
import com.graemsheppard.chessbot.MoveType;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {

    private final int value = 5;

    public Rook(Color color, Location location) {
        super(color, location);
        this.character = 'R';
        this.codePoint = this.color == Color.BLACK ? '\u265c' : '\u2656';
    }

    public List<Location> getValidMoves(Piece[][] board, MoveType type) {
        List<Location> possibleMoves = new ArrayList<>();
        for (int k = 0; k < 2; k++) {
            for (int i = -1; i < 2; i += 2) {
                Location newLocation;
                if (k == 0)
                    newLocation = this.location.addRanks(i);
                else
                    newLocation = this.location.addFiles(i);
                while(newLocation.isValid()) {
                    Piece piece = tileOccupied(newLocation, board);
                    if (piece != null && piece.color == this.color) {
                        break;
                    } else if (piece != null) {
                        possibleMoves.add(newLocation);
                        break;
                    }
                    possibleMoves.add(newLocation);
                    if (k == 0)
                        newLocation = newLocation.addRanks(i);
                    else
                        newLocation = newLocation.addFiles(i);
                }
            }
        }
        return possibleMoves;
    }

    public String getImgPath() {
        return this.color == Color.WHITE ? "wRook.png" : "bRook.png";
    }
}
