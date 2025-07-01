package com.graemsheppard.chessbot.pieces;

import com.graemsheppard.chessbot.Board;
import com.graemsheppard.chessbot.Location;
import com.graemsheppard.chessbot.Move;
import com.graemsheppard.chessbot.enums.Color;
import com.graemsheppard.chessbot.enums.MoveType;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {

    public Bishop(Color color, Location location) {
        super(color, location);
        this.character = 'B';
        this.descriptor = color == Color.BLACK ? 'b' : 'B';
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
                    Piece piece = board.getBoardAt(newLocation);
                    possibleLocations.add(newLocation);

                    // Go no further once a piece is reached unless it is an enemy king
                    if (piece != null && (piece.getCharacter() != 'K' || piece.getColor() == this.color)) {
                        break;
                    }

                    newLocation = newLocation.addRanks(i).addFiles(j);
                }
            }
        }
        return possibleLocations;
    }
}
