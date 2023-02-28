package com.graemsheppard.chessbot.pieces;

import com.graemsheppard.chessbot.*;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {

    public Knight(Color color, Location location) {
        super(color, location);
        this.character = 'N';
        this.codePoint = this.color == Color.BLACK ? '\u265e' : '\u2658';
        this.imgPath = this.color == Color.WHITE ? "wKnight.png" : "bKnight.png";
        this.value = 3;
    }

    @Override
    public List<Move> getValidMoves(Board board) {
        List<Move> possibleMoves = new ArrayList<>();
        for (int k = 0; k < 2; k++) {
            for (int i : new int[] { 1, -1 }) {
                for (int j : new int[] { 2, -2 }) {
                    Location newLocation;
                    if (k == 0)
                        newLocation = this.location.addRanks(i).addFiles(j);
                    else
                        newLocation = this.location.addRanks(j).addFiles(i);

                    if (newLocation.isValid()) {
                        Piece piece = board.getBoardAt(newLocation);
                        if (piece == null) {
                            possibleMoves.add(new Move(this, newLocation, MoveType.MOVE));
                        } else if (piece.color != this.color) {
                            possibleMoves.add(new Move(this, newLocation, MoveType.ATTACK));
                        }
                    }
                }
            }
        }
        return possibleMoves;
    }

}
