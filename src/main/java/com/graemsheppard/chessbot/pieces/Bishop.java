package com.graemsheppard.chessbot.pieces;

import com.graemsheppard.chessbot.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Bishop extends Piece {

    public Bishop(Color color, Location location) {
        super(color, location);
        this.character = 'B';
        this.codePoint = this.color == Color.BLACK ? '\u265d' : '\u2657';
        this.value = 3;
        this.imgPath = this.color == Color.WHITE ? "wBishop.png" : "bBishop.png";
    }

    @Override
    public List<Move> getValidMoves(Board board) {
        List<Move> possibleMoves = new ArrayList<>();
        for (int i = -1; i < 2; i += 2) {
            for (int j = -1; j < 2; j +=2) {
                Location newLocation = this.location.addRanks(i).addFiles(j);
                while(newLocation.isValid()) {
                    Piece piece = board.getBoardAt(newLocation);
                    if (piece == null) {
                        possibleMoves.add(new Move(this, newLocation, MoveType.MOVE));
                    } else {
                        if (piece.color != this.color)
                            possibleMoves.add(new Move(this, newLocation, MoveType.ATTACK));
                        break;
                    }
                    newLocation = newLocation.addRanks(i).addFiles(j);
                }
            }
        }

        return possibleMoves;
    }

    @Override
    public List<Location> getAttackingTiles(Board board) {
        List<Location> possibleLocations = new ArrayList<>();
        for (int i = -1; i < 2; i += 2) {
            for (int j = -1; j < 2; j +=2) {
                Location newLocation = this.location.addRanks(i).addFiles(j);
                while(newLocation.isValid()) {
                    Piece piece = board.getBoardAt(location);
                    possibleLocations.add(newLocation);
                    if (piece != null) {
                        break;
                    }
                    newLocation = newLocation.addRanks(i).addFiles(j);
                }
            }
        }
        return possibleLocations;
    }
}
