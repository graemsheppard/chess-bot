package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.enums.Castle;
import com.graemsheppard.chessbot.enums.Color;
import com.graemsheppard.chessbot.enums.MoveType;
import com.graemsheppard.chessbot.pieces.King;
import com.graemsheppard.chessbot.pieces.Piece;
import com.graemsheppard.chessbot.pieces.Rook;
import lombok.Getter;

import java.util.List;
import java.util.Random;

public class ChessGame {

    @Getter
    private final Board board;

    @Getter
    private Color turn = Color.WHITE;

    public ChessGame() {
        this.board = new Board();
    }

    @Getter
    private Location lastMoveStart;

    @Getter Location lastMoveEnd;

    /**
     * Takes a chess command and returns true and switches turns if the move was made successfully,
     * otherwise returns false and does not change turns or update the board.
     * @param command A string in FIDE algebraic chess notation
     */
    public boolean move(String command) {

        Command parsed = new Command(command);
        if (!parsed.isValid())
            return false;

        if (parsed.getCastleSide() != null) {
            return castle(parsed.getCastleSide());
        }

        List<Move> moveList = this.board.getPieces()
                .filter(p -> p.getColor() == this.turn)
                .filter(p -> p.getClass() == parsed.getPieceType())
                .flatMap(p -> p.getValidMoves(board).stream())
                .filter(m -> m.getMoveType() == parsed.getMoveType())
                .filter(m -> m.getDestination().equals(parsed.getDestination()))
                .filter(m -> m.isSafe(board))
                .toList();

        // No valid moves
        if (moveList.size() == 0)
            return false;

        // One valid move, does not need disambiguation
        if (moveList.size() == 1) {
            Move move = moveList.get(0);
            move.setPromotionType(parsed.getPromotionType());
            lastMoveStart = move.getPiece().getLocation().addFiles(0);
            lastMoveEnd = move.getDestination().addFiles(0);
            board.move(move);
            nextTurn();
            return true;
        }

        // Two valid moves, need to disambiguate the pieces that can move here
        if (moveList.size() == 2) {
            Move move1 = moveList.get(0);
            Move move2 = moveList.get(1);

            // Files are not equal, providing a file is sufficient to determine the piece
            if (move1.getPiece().getLocation().getFile() != move2.getPiece().getLocation().getFile()) {
                Move move = null;
                if (move1.getPiece().getLocation().getFile() == parsed.getFile())
                    move = move1;
                else if (move2.getPiece().getLocation().getFile() == parsed.getFile())
                    move = move2;

                if (move != null) {
                    move.setPromotionType(parsed.getPromotionType());
                    lastMoveStart = move.getPiece().getLocation().addFiles(0);
                    lastMoveEnd = move.getDestination().addFiles(0);
                    board.move(move);
                    nextTurn();
                    return true;
                }

            }
            // Files are equal but ranks are not, so only a rank is needed to identify the piece
            else if (move1.getPiece().getLocation().getRank() != move2.getPiece().getLocation().getRank()) {
                Move move = null;
                if (move1.getPiece().getLocation().getRank() == parsed.getRank())
                    move = move1;
                else if (move2.getPiece().getLocation().getRank() == parsed.getRank())
                    move = move2;

                if (move != null) {
                    move.setPromotionType(parsed.getPromotionType());
                    lastMoveStart = move.getPiece().getLocation().addFiles(0);
                    lastMoveEnd = move.getDestination().addFiles(0);
                    board.move(move);
                    nextTurn();
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Helper function to toggle the turn
     */
    private void nextTurn() {
        turn = turn == Color.WHITE ? Color.BLACK : Color.WHITE;
    }

    /**
     * @param side The direction we are castling to
     * @return true if the move was successful
     */
    public boolean castle(Castle side) {
        Location rookLoc;
        King king = turn == Color.WHITE ? board.getWKing() : board.getBKing();
        char rank = turn == Color.WHITE ? '1' : '8';
        Rook rook = null;
        if (side == Castle.KINGSIDE) {
            rookLoc = new Location('h', rank);
            Piece piece = board.getBoardAt(rookLoc);
            if (piece instanceof Rook)
                rook = (Rook) piece;
        } else if (side == Castle.QUEENSIDE) {
            rookLoc = new Location('a', rank);
            Piece piece = board.getBoardAt(rookLoc);
            if (piece instanceof Rook)
                rook = (Rook) piece;
        }

        if (rook != null && !rook.isMoved() && !king.isMoved()) {
            if (side == Castle.KINGSIDE) {
                Location loc1 = new Location('f', rank);
                Location loc2 = new Location('g', rank);
                if (board.getBoardAt(loc1) == null && board.getBoardAt(loc2) == null) {
                    List<Location> dangerTiles = board.getUnsafeTiles(turn);
                    boolean isPossible = !dangerTiles.stream().anyMatch(l -> l.equals(loc1) || l.equals(loc2) || l.equals(king.getLocation()));
                    if (isPossible) {
                        Move kingMove = new Move(king, loc2, MoveType.MOVE);
                        Move rookMove = new Move(rook, loc1, MoveType.MOVE);
                        board.move(kingMove);
                        board.move(rookMove);
                        turn = turn == Color.WHITE ? Color.BLACK : Color.WHITE;
                        return true;
                    }
                }
            } else if (side == Castle.QUEENSIDE) {
                Location loc1 = new Location('d', rank);
                Location loc2 = new Location('c', rank);
                Location loc3 = new Location('b', rank);
                if (board.getBoardAt(loc1) == null && board.getBoardAt(loc2) == null && board.getBoardAt(loc3) == null) {
                    List<Location> dangerTiles = board.getUnsafeTiles(turn);
                    boolean isPossible = !dangerTiles.stream().anyMatch(l -> l.equals(loc1) || l.equals(loc2) || l.equals(king.getLocation()));
                    if (isPossible) {
                        Move kingMove = new Move(king, loc2, MoveType.MOVE);
                        Move rookMove = new Move(rook, loc1, MoveType.MOVE);
                        board.move(kingMove);
                        board.move(rookMove);
                        turn = turn == Color.WHITE ? Color.BLACK : Color.WHITE;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void doRandomMove() {
        List<Move> moveList = this.board.getPieces()
                .filter(p -> p.getColor() == this.turn)
                .flatMap(p -> p.getValidMoves(board).stream())
                .filter(m -> m.isSafe(board))
                .toList();

        Random random = new Random();
        Move move = moveList.get(random.nextInt(moveList.size()));
        if (move != null) {
            lastMoveStart = move.getPiece().getLocation().addFiles(0);
            lastMoveEnd = move.getDestination().addFiles(0);
            board.move(move);
            turn = this.turn == Color.WHITE ? Color.BLACK : Color.WHITE;
        }
    }

}
