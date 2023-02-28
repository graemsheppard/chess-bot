package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.pieces.*;
import lombok.Getter;

public class Move {

    @Getter
    private final Piece piece;
    @Getter
    private final Location destination;
    @Getter
    private final MoveType moveType;

    public Move (Piece piece, Location destination, MoveType moveType) {
        this.piece = piece;
        this.destination = destination;
        this.moveType = moveType;
    }

    /**
     * Checks if a move is safe by looking for pins
     * @return True if the move is safe to make
     */
    public boolean isSafe(Board board) {
        King king = this.piece.getColor() == Color.WHITE ? board.getWKing() : board.getBKing();

        int diffX = Math.abs(king.getLocation().getFile() - this.getPiece().getLocation().getFile());
        int diffY = Math.abs(king.getLocation().getRank() - this.getPiece().getLocation().getRank());
        // Check rows
        if (this.getPiece().getLocation().getRank() == king.getLocation().getRank()) {
            int direction = this.getPiece().getLocation().getFile() > king.getLocation().getFile()
                    ? 1 : -1;
            Location location = king.getLocation().addFiles(direction);
            while (location.isValid() && (board.getBoardAt(location) == null || board.getBoardAt(location) == this.piece)) {
                location = location.addFiles(direction);
            }

            if (location.isValid())  {
                Piece piece = board.getBoardAt(location);
                if (piece.getColor() != king.getColor() && (piece instanceof Queen || piece instanceof Rook))
                    return false;
            }
        }
        // Check files
        else if (this.getPiece().getLocation().getFile() == king.getLocation().getFile()) {
            int direction = this.getPiece().getLocation().getRank() > king.getLocation().getRank()
                    ? 1 : -1;
            Location location = king.getLocation().addRanks(direction);
            while (location.isValid() && (board.getBoardAt(location) == null || board.getBoardAt(location) == this.piece)) {
                location = location.addRanks(direction);
            }

            if (location.isValid())  {
                Piece piece = board.getBoardAt(location);
                if (piece.getColor() != king.getColor() && (piece instanceof Queen || piece instanceof Rook))
                    return false;
            }
        }
        // Check diagonals
        else if (diffX == diffY) {
            int dirY = this.getPiece().getLocation().getRank() > king.getLocation().getRank()
                    ? 1 : -1;
            int dirX = this.getPiece().getLocation().getFile() > king.getLocation().getFile()
                    ? 1 : -1;
            Location location = king.getLocation().addFiles(dirX).addRanks(dirY);
            while (location.isValid() && (board.getBoardAt(location) == null || board.getBoardAt(location) == this.piece)) {
                location = location.addFiles(dirX).addRanks(dirY);
            }

            if (location.isValid()) {
                Piece piece = board.getBoardAt(location);
                if (piece.getColor() != king.getColor() && (piece instanceof Bishop || piece instanceof Queen))
                    return false;
            }
        }
        return true;
    }

}
