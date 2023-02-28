package com.graemsheppard.chessbot.pieces;

import com.graemsheppard.chessbot.*;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {

    private final int direction;

    public Pawn(Color color, Location location) {
        super(color, location);
        this.direction = this.color == Color.WHITE ? 1 : -1;
        this.character = 'p';
        this.codePoint = this.color == Color.BLACK ? '\u265f' : '\u2657';
        this.value = 1;
    }

    public List<Move> getValidMoves(Board board) {
        List<Move> possibleMoves = new ArrayList();

        int direction = this.color == Color.WHITE ? 1 : -1;
        Location newLocation1 = this.location.addRanks(direction);
        if (newLocation1.isValid() && board.getBoardAt(newLocation1) == null) {
            possibleMoves.add(new Move(this, newLocation1, MoveType.MOVE));
            if (!this.isMoved()) {
                Location newLocation2 = this.location.addRanks(2 * direction);
                if (newLocation2.isValid() && board.getBoardAt(newLocation2) == null) {
                    possibleMoves.add(new Move(this, newLocation2, MoveType.MOVE));
                }
            }
        }

        Location newLocation3 = this.location.addRanks(direction).addFiles(-1);
        if (newLocation3.isValid() && board.getBoardAt(newLocation3) != null)
            possibleMoves.add(new Move(this, newLocation3, MoveType.ATTACK));
        Location newLocation4 = this.location.addRanks(direction).addFiles(1);
        if (newLocation4.isValid() && board.getBoardAt(newLocation4) != null)
            possibleMoves.add(new Move(this, newLocation4, MoveType.ATTACK));

        return possibleMoves;
    }

    @Override
    public List<Location> getAttackingTiles(Board board) {
        List<Location> possibleLocations = new ArrayList<>();
        Location newLocation1 = this.location.addRanks(direction).addFiles(-1);
        if (newLocation1.isValid())
            possibleLocations.add(newLocation1);
        Location newLocation2 = this.location.addRanks(direction).addFiles(1);
        if (newLocation2.isValid())
            possibleLocations.add(newLocation2);
        return possibleLocations;
    }

    public String getImgPath() {
        return this.color == Color.WHITE ? "wPawn.png" : "bPawn.png";
    }


}
