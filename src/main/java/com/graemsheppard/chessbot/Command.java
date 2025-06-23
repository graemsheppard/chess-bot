package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.Exceptions.InvalidMoveException;
import com.graemsheppard.chessbot.enums.Castle;
import com.graemsheppard.chessbot.enums.MoveType;
import com.graemsheppard.chessbot.pieces.*;
import lombok.Getter;

/**
 * Utility class to make it easier to work with command strings
 */
public class Command {

    @Getter
    private Location destination;

    @Getter
    private Class<? extends Piece> promotionType;

    @Getter
    private Class<? extends Piece> pieceType;

    @Getter
    private MoveType moveType;

    @Getter
    private boolean check;

    @Getter
    private boolean checkmate;

    @Getter
    private char rank;

    @Getter
    private char file;

    @Getter
    private Castle castleSide;

    @Getter
    private final String text;

    /**
     * Takes a move command and processes it by trimming the string character by character
     * leaving the unprocessed parts
     * @param command A string in FIDE algebraic chess notation
     */
    public Command(String command) throws InvalidMoveException {
        text = command;
        moveType = MoveType.MOVE;

        if (command.length() < 2)
            throw new InvalidMoveException("Invalid move, at least 2 characters are required");

        if (command.charAt(0) == 'x')
            throw new InvalidMoveException("Invalid move, an attack cannot start with 'x', it must at least indiate the file of the capturing piece.");

        // Check if castling
        if (command.equals("O-O")) {
            castleSide = Castle.KINGSIDE;
            return;
        }

        if (command.equals("O-O-O")) {
            castleSide = Castle.QUEENSIDE;
            return;
        }

        // Find out what type of piece we are moving
        boolean isPawn = false;
        switch (command.charAt(0)) {
            case 'R' -> pieceType = Rook.class;
            case 'N' -> pieceType = Knight.class;
            case 'B' -> pieceType = Bishop.class;
            case 'Q' -> pieceType = Queen.class;
            case 'K' -> pieceType = King.class;
            default -> {
                pieceType = Pawn.class;
                isPawn = true;
            }
        }

        // Trim the piece type from the command
        if (!isPawn) {
            command = command.substring(1);
        }

        char lastChar = command.charAt(command.length() - 1);

        // Check for + and # then remove from the end of the string
        if (lastChar == '+') {
            check = true;
            command = command.substring(0, command.length() - 1);
            lastChar = command.charAt(command.length() - 1);
        } else if (lastChar == '#') {
            checkmate = true;
            command = command.substring(0, command.length() - 1);
            lastChar = command.charAt(command.length() - 1);
        }

        // Check for promotion
        if (isPawn) {
            switch (lastChar) {
                case 'R' -> promotionType = Rook.class;
                case 'N' -> promotionType = Knight.class;
                case 'B' -> promotionType = Bishop.class;
                case 'Q' -> promotionType = Queen.class;
                default -> promotionType = null;
            }

            // Remove promotion character if present
            if (promotionType != null) {
                command = command.substring(0, command.length() - 1);
            }
        }


        // Check command length again
        if (command.length() < 2) {
            throw new InvalidMoveException("Invalid move, the move must at least indicate the destination square.");
        }

        // The last two characters now should be the location
        String locString = command.substring(command.length() - 2);
        Location location = new Location(locString);

        if (!location.isValid()) {
            throw new InvalidMoveException("Invalid move, the destination is outside the bounds of the chess board");
        }

        boolean onLastRank = location.getRank() == '1' || location.getRank() == '8';

        // Ensure if we are promoting, the piece is eligible to do so
        if (promotionType != null && !onLastRank)
            throw new InvalidMoveException("Invalid move, the pawn must be on the last rank to promote");

        // Ensure we are promoting if reaching last rank
        if (isPawn && promotionType == null && onLastRank)
            throw new InvalidMoveException("Invalid move, pawns must promote when reaching the last rank");

        // Remove location part of command
        command = command.substring(0, command.length() - 2);
        destination = location;

        // If there are still characters we are not done parsing
        // Check for attack
        if (command.length() > 0) {
            lastChar = command.charAt(command.length() - 1);

            if (lastChar == 'x') {
                moveType = MoveType.ATTACK;
                command = command.substring(0, command.length() - 1);
            }
        }

        // Check for disambiguation
        if (command.length() > 0) {
            lastChar = command.charAt(command.length() - 1);
            if (lastChar >= '1' && lastChar <= '8') {
                rank = lastChar;
            } else if (lastChar >= 'a' && lastChar <= 'h') {
                file = lastChar;
            } else {
                throw new InvalidMoveException("Unexpected character found in command: " + lastChar);
            }
            command = command.substring(0, command.length() - 1);
        }

        // Should now have no characters left
        if (command.length() != 0)
            throw new InvalidMoveException("Unexpected character sequence found in command: " + command);
    }
}
