package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.enums.Color;
import com.graemsheppard.chessbot.enums.MoveType;
import com.graemsheppard.chessbot.pieces.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

public class Move {

    @Getter
    private final Piece piece;
    @Getter
    private final Location destination;
    @Getter
    private final MoveType moveType;

    @Getter
    private MoveHandler handler;

    @Getter
    @Setter
    private Class<? extends Piece> promotionType;

    public Move (Piece piece, Location destination, MoveType moveType) {
        this.piece = piece;
        this.destination = destination;
        this.moveType = moveType;
    }

    public Move (Piece piece, Location destination, MoveType moveType, MoveHandler handler) {
        this(piece, destination, moveType);
        this.handler = handler;
    }

    /**
     * Checks if a move is safe by looking for pins
     * @return True if the move is safe to make
     */
    public boolean isSafe(Board board) {
        King king = this.piece.getColor() == Color.WHITE ? board.getWKing() : board.getBKing();

        // If the king is in check, we need to block or move the king
        List<Location> unsafeTiles = board.getUnsafeTiles(king.getColor());

        // If we are moving the king, ensure destination is safe
        if (this.getPiece() == king) {
            return !unsafeTiles.stream().anyMatch(l -> l.equals(this.getDestination()));
        }

        int diffX = Math.abs(king.getLocation().getFile() - this.getPiece().getLocation().getFile());
        int diffY = Math.abs(king.getLocation().getRank() - this.getPiece().getLocation().getRank());
        // Check rows for pins
        if (this.getPiece().getLocation().getRank() == king.getLocation().getRank()) {
            int direction = this.getPiece().getLocation().getFile() > king.getLocation().getFile()
                    ? 1 : -1;
            Location location = king.getLocation().addFiles(direction);
            while (location.isValid() && (board.getBoardAt(location) == null || board.getBoardAt(location) == this.piece)) {
                location = location.addFiles(direction);
            }

            if (location.isValid())  {
                Piece piece = board.getBoardAt(location);
                if (!piece.getLocation().equals(this.getDestination()))
                    if (piece.getColor() != king.getColor() && (piece instanceof Queen || piece instanceof Rook)
                        && this.getDestination().getRank() != piece.getLocation().getRank())
                        return false;
            }
        }
        // Check files for pins
        else if (this.getPiece().getLocation().getFile() == king.getLocation().getFile()) {
            int direction = this.getPiece().getLocation().getRank() > king.getLocation().getRank()
                    ? 1 : -1;
            Location location = king.getLocation().addRanks(direction);
            while (location.isValid() && (board.getBoardAt(location) == null || board.getBoardAt(location) == this.piece)) {
                location = location.addRanks(direction);
            }

            if (location.isValid())  {
                Piece piece = board.getBoardAt(location);
                if (!piece.getLocation().equals(this.getDestination()))
                    if (piece.getColor() != king.getColor() && (piece instanceof Queen || piece instanceof Rook)
                        && this.getDestination().getFile() != piece.getLocation().getFile())
                        return false;
            }
        }
        // Check diagonals for pins
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
                if (!piece.getLocation().equals(this.getDestination()))
                    if (piece.getColor() != king.getColor() && (piece instanceof Bishop || piece instanceof Queen))
                        return false;
            }

        }

        // If king is under check, and we are not moving it
        if (unsafeTiles.stream().anyMatch(l -> l.equals(king.getLocation())) && this.getPiece() != king) {
            // Check if a Knight is checking, invalid move if it is
            Optional<Piece> attackingKnight = board.getPieces()
                    .filter(p -> p instanceof Knight)
                    .filter(p -> p.getColor() != king.getColor())
                    .filter(p -> p.getAttackingTiles(board).stream().anyMatch(l -> l.equals(king.getLocation())))
                    .findAny();
            if (attackingKnight.isPresent() && !this.getDestination().equals(attackingKnight.get().getLocation()))
                return false;

            // Check for pawns
            int pawnDir = king.getColor() == Color.WHITE ? 1 : -1;
            Location pLoc1 = king.getLocation().addRanks(pawnDir).addFiles(-1);
            Location pLoc2 = king.getLocation().addRanks(pawnDir).addFiles(1);

            Piece piece1 = pLoc1.isValid() ? board.getBoardAt(pLoc1): null;
            Piece piece2 = pLoc2.isValid() ? board.getBoardAt(pLoc2) : null;

            // A pawn is checking on lower file
            if (piece1 instanceof Pawn && piece1.getColor() != king.getColor()) {
                if (!this.getDestination().equals(piece1.getLocation()))
                    return false;
            }

            // A pawn is checking on higher file
            if (piece2 instanceof Pawn && piece2.getColor() != king.getColor()) {
                if (!this.getDestination().equals(piece2.getLocation()))
                    return false;
            }

            // Check if the move is onto the same file, rank, or diagonal as the king
            // Invalid move if not
            diffX = Math.abs(king.getLocation().getFile() - this.getDestination().getFile());
            diffY = Math.abs(king.getLocation().getRank() - this.getDestination().getRank());

            // Check files, ranks, and diagonals from king
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    if (x == 0 && y == 0)
                        continue;

                    Location location = king.getLocation().addFiles(x).addRanks(y);
                    // Go in direction until a piece is encountered that is not the one being moved
                    while (location.isValid() && (board.getBoardAt(location) == null || board.getBoardAt(location) == this.getPiece())) {
                        location = location.addFiles(x).addRanks(y);
                    }

                    // If location is valid, there was a piece found
                    if (location.isValid()) {
                        Piece piece = board.getBoardAt(location);
                        boolean rookOrBishop = Math.abs(x) == Math.abs(y) ? (piece instanceof Bishop) : (piece instanceof Rook);
                        if (piece.getColor() != king.getColor() && (piece instanceof Queen || rookOrBishop)) {
                            // Check if we are moving to a location between king and piece

                            // Distance from piece to king
                            int dPieceX = piece.getLocation().getFile() - king.getLocation().getFile();
                            int dPieceY = piece.getLocation().getRank() - king.getLocation().getRank();

                            // Distance from destination to king
                            int dMoveX = this.getDestination().getFile() - king.getLocation().getFile();
                            int dMoveY = this.getDestination().getRank() - king.getLocation().getRank();

                            // Attacker and destination are on diagonals
                            boolean tooFarX = Math.abs(dMoveX) > Math.abs(dPieceX);
                            if (Math.abs(dPieceX) == Math.abs(dPieceY) && Math.abs(dMoveX) == Math.abs(dMoveY)) {
                                int m = dPieceX * dMoveX;
                                int n = dPieceY * dMoveY;
                                // m and m are positive iff the pieces are on the same diagonal
                                if (!tooFarX && m > 0 && n > 0)
                                    return true;
                            // Attacker and destination are on same file
                            } else if (dPieceX == 0 && dMoveX == 0) {
                                if (Math.abs(dMoveY) <= Math.abs(dPieceY))
                                    return true;
                            } else if (dPieceY == 0 && dMoveY == 0) {
                                if (!tooFarX)
                                    return true;
                            }
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * A handler called by Board after the piece is moved
     */
    public interface MoveHandler {
        void handle();
    }

}
