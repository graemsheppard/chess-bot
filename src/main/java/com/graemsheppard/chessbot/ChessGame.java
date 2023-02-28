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

    public boolean move(String command) {

        if (command.length() < 2) {
            return false;
        }

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


}
