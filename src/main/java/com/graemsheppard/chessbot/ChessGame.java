package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.pieces.*;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class ChessGame {

    @Getter
    private final Board board;

    @Getter
    private Color turn = Color.WHITE;

    public ChessGame() {

        this.board = new Board();

    }

    // TODO: Handle pawn promotions, checks, and checkmates

    public boolean move(String command) {

        if (command.length() < 2) {
            return false;
        }

        if (command.equals("0-0"))
            return castle(true);
        else if (command.equals("0-0-0"))
            return castle(false);

        // Get the type of piece being moved based on command
        Class<? extends Piece> pieceType;
        boolean isPawn = false;
        switch (command.charAt(0)) {
            case 'R':
                pieceType = Rook.class;
                break;
            case 'N':
                pieceType = Knight.class;
                break;
            case 'B':
                pieceType = Bishop.class;
                break;
            case 'Q':
                pieceType = Queen.class;
                break;
            case 'K':
                pieceType = King.class;
                break;
            default:
                pieceType = Pawn.class;
                isPawn = true;
        }

        // Get the piece being promoted to based on command
        Class<? extends Piece> promotionType = null;
        if (isPawn) {
            switch (command.charAt(command.length() - 1)) {
                case 'R':
                    promotionType = Rook.class;
                    break;
                case 'N':
                    promotionType = Knight.class;
                    break;
                case 'B':
                    promotionType = Bishop.class;
                    break;
                case 'Q':
                    promotionType = Queen.class;
                    break;
            }
        }

        int attackIdx = command.length() - 3;
        if (promotionType != null)
            attackIdx--;
        if (command.charAt(command.length() - 1) == '+' || command.charAt(command.length() - 1) == '#')
            attackIdx--;

        Location destination;
        destination = new Location(command.substring(attackIdx + 1, attackIdx + 3));

        if (!destination.isValid()) {
            return false;
        }

        MoveType moveType;
        if (command.length() == 2)
            moveType = MoveType.MOVE;
        else {
            if (attackIdx >= 0 && command.charAt(attackIdx) == 'x')
                moveType = MoveType.ATTACK;
            else
                moveType = MoveType.MOVE;
        }

        MoveType finalMoveType = moveType;
        List<Move> moveList = this.board.getPieces()
                .filter(p -> p.getColor() == this.turn)
                .filter(Piece::isAlive)
                .filter(p -> p.getClass() == pieceType)
                .flatMap(p -> p.getValidMoves(board).stream())
                .filter(m -> m.getMoveType() == finalMoveType)
                .filter(m -> m.getDestination().equals(destination))
                .filter(m -> m.isSafe(board))
                .toList();

        if (moveList.size() == 0)
            return false;

        if (moveList.size() == 1) {
            Move move = moveList.get(0);
            board.move(move);
            turn = turn == Color.WHITE ? Color.BLACK : Color.WHITE;
            return true;
        }

        return false;
    }

    /**
     * @param kingside True if trying to castle kingside, false for queenside
     * @return True if the move was successful
     */
    public boolean castle(boolean kingside) {
        Location rookLoc;
        King king = turn == Color.WHITE ? board.getWKing() : board.getBKing();
        char rank = turn == Color.WHITE ? '1' : '8';
        Rook rook = null;
        if (kingside) {
            rookLoc = new Location('h', rank);
            Piece piece = board.getBoardAt(rookLoc);
            if (piece instanceof Rook)
                rook = (Rook) piece;
        } else {
            rookLoc = new Location('a', rank);
            Piece piece = board.getBoardAt(rookLoc);
            if (piece instanceof Rook)
                rook = (Rook) piece;
        }

        if (rook != null && !rook.isMoved() && !king.isMoved()) {
            if (kingside) {
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
                    }
                }
            } else {
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

}
