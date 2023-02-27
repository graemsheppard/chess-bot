package com.graemsheppard.chessbot.pieces;

import com.graemsheppard.chessbot.Color;
import com.graemsheppard.chessbot.Location;
import com.graemsheppard.chessbot.MoveType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Bishop extends Piece {

    private final int value = 3;

    public Bishop(Color color, Location location) {
        super(color, location);
        this.character = 'B';
        this. codePoint = this.color == Color.BLACK ? '\u265d' : '\u2657';
    }

    public List<Location> getValidMoves(Piece[][] board, MoveType type) {
        List<Location> possibleMoves = new ArrayList<>();

        for (int i = -1; i < 2; i += 2) {
            for (int j = -1; j < 2; j +=2) {
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
        return this.color == Color.WHITE ? "wBishop.png" : "bBishop.png";
    }
}
