package com.graemsheppard.chessbot.pieces;

import com.graemsheppard.chessbot.Board;
import com.graemsheppard.chessbot.Location;
import com.graemsheppard.chessbot.Move;
import com.graemsheppard.chessbot.enums.Color;
import com.graemsheppard.chessbot.enums.MoveType;

import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece {

    public Queen(Color color, Location location) {
        super(color, location);
        this.character = 'Q';
        this.codePoint = this.color == Color.BLACK ? '\u265b' : '\u2655';
        this.value = 7;
    }

    @Override
    public List<Move> getValidMoves(Board board) {
        List<Move> possibleMoves = new ArrayList<>();

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (i == 0 && j == 0) continue;
                Location newLocation = this.location.addRanks(i).addFiles(j);
                while(newLocation.isValid()) {
                    Piece piece = board.getBoardAt(newLocation);
                    if (piece == null) {
                        possibleMoves.add(new Move(this, newLocation, MoveType.MOVE));
                    } else  {
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

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (i == 0 && j == 0) continue;
                Location newLocation = this.location.addRanks(i).addFiles(j);
                while(newLocation.isValid()) {
                    Piece piece = board.getBoardAt(newLocation);
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

    public String getImgPath() {
        return this.color == Color.WHITE ? "wQueen.png" : "bQueen.png";
    }
}
