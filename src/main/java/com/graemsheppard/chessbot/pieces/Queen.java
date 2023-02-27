package com.graemsheppard.chessbot.pieces;

import com.graemsheppard.chessbot.Color;
import com.graemsheppard.chessbot.Location;
import com.graemsheppard.chessbot.MoveType;

import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece {

    private final int value = 7;
    public Queen(Color color, Location location) {
        super(color, location);
        this.character = 'Q';
        this.codePoint = this.color == Color.BLACK ? '\u265b' : '\u2655';
    }

    @Override
    public List<Location> getValidMoves(Piece[][] board, MoveType type) {
        List<Location> possibleMoves = new ArrayList<>();

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (i == 0 && j == 0) continue;
                Location newLocation = this.location.addRanks(i).addFiles(j);
                while(newLocation.isValid()) {
                    Piece piece = tileOccupied(newLocation, board);
                    if (piece != null && piece.color == this.color) {
                        break;
                    } else if (piece != null) {
                        possibleMoves.add(newLocation);
                        break;
                    }
                    possibleMoves.add(newLocation);
                    newLocation = newLocation.addRanks(i).addFiles(j);
                }
            }
        }
        return possibleMoves;
    }

    public String getImgPath() {
        return this.color == Color.WHITE ? "wQueen.png" : "bQueen.png";
    }
}
