package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.Exceptions.InvalidMoveException;
import com.graemsheppard.chessbot.enums.Color;
import com.graemsheppard.chessbot.pieces.Bishop;
import com.graemsheppard.chessbot.pieces.King;
import com.graemsheppard.chessbot.pieces.Queen;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChessGameTests {

    @Test
    public void queenShouldPinBishop() {
        var board = new TestBoard();
        var game = new ChessGame();
        game.setBoard(board);

        var wKing = new King(Color.WHITE, new Location('e', '1'));
        var bKing = new King(Color.BLACK, new Location('e', '8'));
        var wBishop = new Bishop(Color.WHITE, new Location('e', '4'));
        var bQueen = new Queen(Color.BLACK, new Location('e', '7'));

        board.setOnBoard(wKing);
        board.setOnBoard(bKing);
        board.setOnBoard(wBishop);
        board.setOnBoard(bQueen);

        // Cannot move out of pin
        assertThrows(InvalidMoveException.class, () -> {
            game.move("Bd5");
        });

        // Cannot move out of pin with check
        assertThrows(InvalidMoveException.class, () -> {
            game.move("Bc6+");
        });

        // Moving the king anywhere behind the bishop is fine
        assertDoesNotThrow(() -> {
            game.move("Ke2");
            game.setTurn(Color.WHITE);
            game.move("Kd2");
        });
    }

    @Test
    public void bishopShouldPinQueen() {
        var board = new TestBoard();
        var game = new ChessGame();
        game.setBoard(board);

        var wKing = new King(Color.WHITE, new Location('a', '1'));
        var bKing = new King(Color.BLACK, new Location('h', '8'));
        var bBishop = new Bishop(Color.BLACK, new Location('g', '7'));
        var wQueen = new Queen(Color.WHITE, new Location('b', '2'));

        board.setOnBoard(wKing);
        board.setOnBoard(bKing);
        board.setOnBoard(bBishop);
        board.setOnBoard(wQueen);

        // Cannot move out of pin
        assertThrows(InvalidMoveException.class, () -> {
            game.move("Qb8");
        });

        // Cannot move out of pin with check
        assertThrows(InvalidMoveException.class, () -> {
            game.move("Qb8+");
        });

        // Can move within the pin and capture the attacking bishop
        assertDoesNotThrow(() -> {
            game.move("Qf6");
            game.setTurn(Color.WHITE);
            game.move("Qxg7+");
        });

    }



}
