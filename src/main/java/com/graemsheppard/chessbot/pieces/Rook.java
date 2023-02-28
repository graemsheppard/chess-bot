package com.graemsheppard.chessbot.pieces;

import com.graemsheppard.chessbot.*;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {

    public Rook(Color color, Location location) {
        super(color, location);
        this.character = 'R';
        this.codePoint = this.color == Color.BLACK ? '\u265c' : '\u2656';
        this.imgPath = this.color == Color.WHITE ? "wRook.png" : "bRook.png";
        this.value = 5;
    }

    @Override
    public List<Move> getValidMoves(Board board) {
        List<Move> possibleMoves = new ArrayList<>();
        for (int k = 0; k < 2; k++) {
            for (int i = -1; i < 2; i += 2) {
                Location newLocation;
                if (k == 0)
                    newLocation = this.location.addRanks(i);
                else
                    newLocation = this.location.addFiles(i);
                while(newLocation.isValid()) {
                    Piece piece = board.getBoardAt(newLocation);
                    if (piece == null) {
                        possibleMoves.add(new Move(this, newLocation, MoveType.MOVE));
                    } else {
                        if (piece.color != this.color) {
                            possibleMoves.add(new Move(this, newLocation, MoveType.ATTACK));
                        }
                        break;
                    }
                    if (k == 0)
                        newLocation = newLocation.addRanks(i);
                    else
                        newLocation = newLocation.addFiles(i);
                }
            }
        }

        return possibleMoves;
    }

}
