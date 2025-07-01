package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.Exceptions.InvalidMoveException;
import com.graemsheppard.chessbot.enums.Castle;
import com.graemsheppard.chessbot.enums.Color;
import com.graemsheppard.chessbot.enums.GameResultType;
import com.graemsheppard.chessbot.enums.MoveType;
import com.graemsheppard.chessbot.pieces.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ChessGame {

    @Getter
    @Setter
    private Board board;

    @Getter
    @Setter
    private Color turn = Color.WHITE;

    @Getter
    @Setter
    private int turnNumber = 1;

    @Getter
    private Color winner;

    @Getter
    private GameResultType outcome;

    @Getter
    private Location lastMoveStart;

    @Getter
    private Location lastMoveEnd;

    @Getter
    @Setter
    private WinHandler winHandler;

    @Getter
    @Setter
    private int lastNonDrawTurn = 0;

    @Getter
    private boolean inProgress = true;

    @Getter
    private final HashMap<String, Integer> positionCounts = new HashMap<>();

    public ChessGame() {
        board = new Board();
        positionCounts.put(board.getEncodedBase64(), 1);
    }

    /**
     * Takes a chess command and returns true and switches turns if the move was made successfully,
     * otherwise returns false and does not change turns or update the board.
     * @param command A string in FIDE algebraic chess notation
     */
    public void move(String command) throws InvalidMoveException {

        if (!inProgress)
            throw new InvalidMoveException(InvalidMoveException.GAME_NOT_IN_PROGRESS);

        Command parsed = new Command(command);

        if (parsed.getResignee() != null) {
            if (parsed.getResignee() == this.turn) {
                winner = parsed.getResignee() == Color.BLACK ? Color.WHITE : Color.BLACK;
                outcome = GameResultType.RESIGNATION;
                inProgress = false;
                nextTurn();
                return;
            } else {
                throw new InvalidMoveException(InvalidMoveException.MUST_RESIGN_ON_OWN_TURN);
            }
        }

        if (parsed.getCastleSide() != null) {
            castle(parsed.getCastleSide());
            return;
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
            throw new InvalidMoveException(InvalidMoveException.NO_AVAILABLE_PIECE);

        // One valid move, does not need disambiguation
        if (moveList.size() == 1 && (parsed.getRank() == 0 && parsed.getFile() == 0 || parsed.getPieceType() == Pawn.class && parsed.getMoveType() == MoveType.ATTACK)) {
            Move move = moveList.get(0);
            move.setPromotionType(parsed.getPromotionType());
            lastMoveStart = move.getPiece().getLocation().addFiles(0);
            lastMoveEnd = move.getDestination().addFiles(0);
            board.move(move);
            if (move.getPiece().getCharacter() == 'p' || move.getMoveType() == MoveType.ATTACK)
                lastNonDrawTurn = turnNumber;
            nextTurn();
            checkWinner(turn);
            return;
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
                    if (move.getPiece().getCharacter() == 'p' || move.getMoveType() == MoveType.ATTACK)
                        lastNonDrawTurn = turnNumber;
                    nextTurn();
                    checkWinner(turn);
                    return;
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
                    if (move.getPiece().getCharacter() == 'p' || move.getMoveType() == MoveType.ATTACK)
                        lastNonDrawTurn = turnNumber;
                    board.move(move);
                    nextTurn();
                    checkWinner(turn);
                    return;
                }
            }
        }

        throw new InvalidMoveException(InvalidMoveException.UNHANDLED);
    }

    /**
     * Helper function to toggle the turn
     */
    private void nextTurn() {
        turn = turn == Color.WHITE ? Color.BLACK : Color.WHITE;
        if (turn == Color.WHITE)
            turnNumber++;
    }

    /**
     * @param side The direction we are castling to
     */
    public void castle(Castle side) throws InvalidMoveException {
        Location rookLoc;
        King king = turn == Color.WHITE ? board.getWKing() : board.getBKing();
        char rank = turn == Color.WHITE ? '1' : '8';
        Rook rook = null;
        if (side == Castle.KINGSIDE) {
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

        if (rook == null)
            throw new InvalidMoveException("Cannot castle " + side.toString() + " because there is no rook on " + rookLoc);

        if (rook.isMoved() || king.isMoved())
            throw new InvalidMoveException("Cannot castle " + side.toString() + " because the rook or king has moved");

        if (side == Castle.KINGSIDE) {
            Location loc1 = new Location('f', rank);
            Location loc2 = new Location('g', rank);
            if (board.getBoardAt(loc1) == null && board.getBoardAt(loc2) == null) {
                List<Location> dangerTiles = board.getUnsafeTiles(turn);
                boolean isPossible = dangerTiles.stream().noneMatch(l -> l.equals(loc1) || l.equals(loc2) || l.equals(king.getLocation()));
                if (!isPossible)
                    throw new InvalidMoveException("Cannot castle " + side.toString() + " because the king is in check or passes through danger.");

                Move kingMove = new Move(king, loc2, MoveType.MOVE);
                Move rookMove = new Move(rook, loc1, MoveType.MOVE);
                board.move(kingMove);
                board.move(rookMove);
                nextTurn();
                checkWinner(turn);
                return;
            }
        } else {
            Location loc1 = new Location('d', rank);
            Location loc2 = new Location('c', rank);
            Location loc3 = new Location('b', rank);
            if (board.getBoardAt(loc1) == null && board.getBoardAt(loc2) == null && board.getBoardAt(loc3) == null) {
                List<Location> dangerTiles = board.getUnsafeTiles(turn);
                boolean isPossible = dangerTiles.stream().noneMatch(l -> l.equals(loc1) || l.equals(loc2) || l.equals(king.getLocation()));
                if (!isPossible)
                    throw new InvalidMoveException("Cannot castle " + side.toString() + " because the king is in check or passes through danger.");

                Move kingMove = new Move(king, loc2, MoveType.MOVE);
                Move rookMove = new Move(rook, loc1, MoveType.MOVE);
                board.move(kingMove);
                board.move(rookMove);
                nextTurn();
                checkWinner(turn);
                return;
            }
        }

        throw new InvalidMoveException(InvalidMoveException.CASTLE_UNHANDLED);
    }

    /**
     *
     * @param color The color to check if is checkmate
     */
    public void checkWinner(Color color) {
        List<Move> moveList = this.board.getPieces()
                .filter(p -> p.getColor() == this.turn)
                .flatMap(p -> p.getValidMoves(board).stream())
                .filter(m -> m.isSafe(board))
                .toList();

        var encodedPos = board.getEncodedBase64();
        var positionCount = positionCounts.get(encodedPos);
        var newPositionCount = (positionCount == null ? 0 : positionCount) + 1;
        positionCounts.put(encodedPos, newPositionCount);

        // Check for win by 50 move rule
        if (turnNumber - lastNonDrawTurn > 50 || newPositionCount > 2) {
            outcome = GameResultType.DRAW;

            if (winHandler != null)
                winHandler.handle(winner);

            inProgress = false;
        }

        // No valid moves, check for win or draw
        if (moveList.size() == 0 || board.getPieces().count() == 2) {
            if (board.kingInCheck(color)) {
                winner = color == Color.WHITE ? Color.BLACK : Color.WHITE;
                outcome = GameResultType.CHECKMATE;
            } else {
                outcome = GameResultType.DRAW;
            }

            if (winHandler != null)
                winHandler.handle(winner);

            inProgress = false;
        }

        // Check if the game is a draw by insufficient material
        if (insufficientMaterial(Color.WHITE) && insufficientMaterial(Color.BLACK)) {
            outcome = GameResultType.DRAW;

            if (winHandler != null)
                winHandler.handle(winner);

            inProgress = false;
        }
    }

    public void doRandomMove() {

        if (!inProgress)
            return;

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
            if ((move.getPiece().getColor() == Color.WHITE && move.getDestination().getRank() == '8' ||
                    move.getPiece().getColor() == Color.BLACK && move.getDestination().getRank() == '1')
                && move.getPiece().getCharacter() == 'p')
                move.setPromotionType(Queen.class);
            board.move(move);
            nextTurn();
            checkWinner(turn);
        }
    }

    // TODO: handle other cases
    private boolean insufficientMaterial(Color color) {
        var pieces = board.getPieces().filter(p -> p.getColor() == color);
        if (pieces.count() == 1)
            return true;
        return false;
    }

    public interface WinHandler {
        void handle(Color winner);
    }

}
