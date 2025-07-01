package com.graemsheppard.chessbot.Exceptions;

public class InvalidMoveException extends Exception {

    public final static String MUST_RESIGN_ON_OWN_TURN = "You must wait until it is your move to resign.";
    public final static String GAME_NOT_IN_PROGRESS = "The game has concluded, you may not make further moves.";
    public final static String NO_AVAILABLE_PIECE = "Invalid move, there is no piece that can make the move.";
    public final static String UNHANDLED = "The move could not be completed due to an unhandled exception.";
    public final static String NOT_ENOUGH_CHARACTERS = "Invalid move, at least 2 characters are required.";
    public final static String CANNOT_START_WITH_X = "Invalid move, an attack cannot start with 'x', it must at least indiate the file of the capturing piece.";
    public final static String NO_DESTINATION = "Invalid move, the move must at least indicate the destination square.";
    public final static String INVALID_DESTINATION = "Invalid move, the destination is outside the bounds of the chess board.";
    public final static String PAWN_NOT_ON_LAST_RANK = "Invalid move, the pawn must be on the last rank to promote.";
    public final static String PAWN_MUST_PROMOTE = "Invalid move, pawns must promote when reaching the last rank.";
    public final static String INVALID_CHARACTER_SEQUENCE = "Unexpected character sequence found in command.";
    public final static String CASTLE_UNHANDLED = "Cannot castle for unexpected reason";

    public InvalidMoveException(String message) {
        super(message);
    }
}
