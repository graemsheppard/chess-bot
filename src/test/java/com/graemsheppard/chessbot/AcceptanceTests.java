package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.enums.Color;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class AcceptanceTests {

    @ParameterizedTest
    @ValueSource(strings = { "blackWinsCheckmate1.pgn", "blackWinsCheckmate2.pgn", "whiteWinsCheckmate1.pgn" })
    public void fullGameEndsWithCheckmate(String filename) {
        ChessGame game = new ChessGame();
        var gameResult = TestUtils.getGameResult(filename);

        assertTrue(game.isInProgress());
        assertEquals(game.getTurn(), Color.WHITE);
        assertNull(game.getWinner());

        for (int i = 0; i < gameResult.getMoveList().size(); i++) {
            var move = gameResult.getMoveList().get(i);
            var moveResult = game.move(move);
            var expectedTurn = (i + 1) % 2 == 0 ? Color.WHITE : Color.BLACK;
            assertTrue(moveResult);
            assertEquals(game.getTurn(), expectedTurn);
        }

        assertFalse(game.isInProgress());
        assertEquals(game.getWinner(), gameResult.getWinner());
    }
}
