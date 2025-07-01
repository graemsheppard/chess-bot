package com.graemsheppard.chessbot.pieces;

import com.graemsheppard.chessbot.Board;
import com.graemsheppard.chessbot.Location;
import com.graemsheppard.chessbot.Move;
import com.graemsheppard.chessbot.enums.Color;
import com.graemsheppard.chessbot.enums.MoveType;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {

    public King(Color color, Location location) {
        super(color, location);
        this.character = 'K';
        this.descriptor = color == Color.BLACK ? 'k' : 'K';
        this. codePoint = this.color == Color.BLACK ? '\u265a' : '\u2654';
        this.imgPath = this.color == Color.WHITE ? "wKing.png" : "bKing.png";
        this.value = Integer.MAX_VALUE;

    }

    @Override
    public List<Move> getValidMoves(Board board) {
        List<Move> possibleMoves = new ArrayList<>();
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (i == 0 && j == 0) continue;
                Location newLocation = this.location.addFiles(i).addRanks(j);
                if (newLocation.isValid()) {
                    Piece piece = board.getBoardAt(newLocation);
                    if (piece == null) {
                        possibleMoves.add(new Move(this, newLocation, MoveType.MOVE));
                    } else {
                        if (piece.color != this.color) {
                            possibleMoves.add(new Move(this, newLocation, MoveType.ATTACK));
                        }
                    }
                }
            }
        }

        return possibleMoves.stream().filter(m ->
                !board.getUnsafeTiles(this.color).stream().anyMatch(l -> m.getDestination().equals(l))
        ).toList();
    }

    @Override
    public List<Location> getAttackingTiles(Board board) {
        List<Location> possibleLocations = new ArrayList<>();
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (i == 0 && j == 0) continue;
                Location newLocation = this.location.addFiles(i).addRanks(j);
                if (newLocation.isValid()) {
                    possibleLocations.add(newLocation);
                }
            }
        }
        return possibleLocations;
    }

}
