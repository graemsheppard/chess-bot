package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.pieces.*;
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

    // TODO: Handle checks, and checkmates

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

        boolean shouldPromote = isPawn && (destination.getRank() == '1' || destination.getRank() == '8');
        if (promotionType == null &&  shouldPromote) {
            return false;
        }

        if (promotionType != null && destination.getRank() != '1' && destination.getRank() != '8') {
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
        Class<? extends Piece> finalPromotionType = promotionType;
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
            if (promotionType != null)
                move.setPromotionType(promotionType);
            lastMoveStart = move.getPiece().getLocation().addFiles(0);
            lastMoveEnd = move.getDestination().addFiles(0);
            board.move(move);
            turn = turn == Color.WHITE ? Color.BLACK : Color.WHITE;
            return true;
        }

        if (moveList.size() == 2) { // TODO: Fix this
            if (attackIdx < 0)
                return false;

            char rankOrFile = command.charAt(attackIdx - 1);

            Move move1 = moveList.get(0);
            Move move2 = moveList.get(1);

            if (move1.getPiece().getLocation().getFile() != move2.getPiece().getLocation().getFile()) {
                if (rankOrFile >= 'a' && rankOrFile <= 'h') {
                    Move move = null;
                    if (move1.getPiece().getLocation().getFile() == rankOrFile)
                        move = move1;
                    else if (move2.getPiece().getLocation().getFile() == rankOrFile)
                        move = move2;

                    if (move != null) {
                        if (promotionType != null)
                            move.setPromotionType(promotionType);
                        lastMoveStart = move.getPiece().getLocation().addFiles(0);
                        lastMoveEnd = move.getDestination().addFiles(0);
                        board.move(move);
                        turn = turn == Color.WHITE ? Color.BLACK : Color.WHITE;
                    }
                }

            }
            else if (move1.getPiece().getLocation().getRank() != move2.getPiece().getLocation().getRank()) {
                if (rankOrFile >= '1' && rankOrFile <= '8') {
                    Move move = null;
                    if (move1.getPiece().getLocation().getRank() == rankOrFile)
                        move = move1;
                    else if (move2.getPiece().getLocation().getRank() == rankOrFile)
                        move = move2;

                    if (move != null) {
                        if (promotionType != null)
                            move.setPromotionType(promotionType);
                        lastMoveStart = move.getPiece().getLocation().addFiles(0);
                        lastMoveEnd = move.getDestination().addFiles(0);
                        board.move(move);
                        turn = turn == Color.WHITE ? Color.BLACK : Color.WHITE;
                    }
                }
            }

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
