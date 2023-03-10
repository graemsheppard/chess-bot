package com.graemsheppard.chessbot;

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
    private boolean valid;

    @Getter
    private char rank;

    @Getter
    private char file;

    @Getter
    private Castle castleSide;

    /**
     * Takes a move command and processes it by trimming the string character by character
     * leaving the unprocessed parts
     * @param command A string in FIDE algebraic chess notation
     */
    public Command(String command) {

        moveType = MoveType.MOVE;

        if (command.length() < 2) {
            return;
        }

        // Check if castling
        if (command.equals("0-0")) {
            castleSide = Castle.KINGSIDE;
            valid = true;
            return;
        }

        if (command.equals("0-0-0")) {
            castleSide = Castle.QUEENSIDE;
            valid = true;
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
            return;
        }

        // The last two characters now should be the location
        Location location = new Location(command.substring(command.length() - 2));

        if (!location.isValid()) {
            return;
        }

        boolean onLastRank = location.getRank() == '1' || location.getRank() == '8';

        // Ensure if we are promoting, the piece is eligible to do so
        if (promotionType != null && !onLastRank)
            return;

        // Ensure we are promoting if reaching last rank
        if (isPawn && promotionType == null && onLastRank)
            return;

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
                return;
            }
            command = command.substring(0, command.length() - 1);
        }

        // Should now have no characters left
        if (command.length() == 0)
            valid = true;
    }
}
