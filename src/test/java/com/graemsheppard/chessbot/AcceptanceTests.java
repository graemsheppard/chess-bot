package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.Exceptions.InvalidMoveException;
import com.graemsheppard.chessbot.enums.Color;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AcceptanceTests {

    @ParameterizedTest
    @MethodSource("getValidGames")
    public void fullGamePlayTest(String filename) throws InvalidMoveException {
        ChessGame game = new ChessGame();
        var gameResult = TestUtils.getGameResult(filename);

        assertTrue(game.isInProgress());
        assertEquals(game.getTurn(), Color.WHITE);
        assertNull(game.getWinner());

        for (int i = 0; i < gameResult.getMoveList().size(); i++) {
            var move = gameResult.getMoveList().get(i);
            game.move(move);
            var expectedTurn = (i + 1) % 2 == 0 ? Color.WHITE : Color.BLACK;
            assertEquals(game.getTurn(), expectedTurn);
        }

        assertFalse(game.isInProgress());
        assertEquals(gameResult.getWinner(), game.getWinner());
        assertEquals(gameResult.getResultType(), game.getOutcome());
    }

    private static List<String> getValidGames() {
        return List.of(new String[] {
                "blackWinsCheckmate1.pgn",
                "blackWinsCheckmate2.pgn",
                "blackWinsCheckmate3.pgn",
                "whiteWinsCheckmate1.pgn" ,
                "whiteWinsCheckmate2.pgn",
                "whiteWinsResignation1.pgn",
                "drawStalemate1.pgn",
                "draw50MoveRule1.pgn"
        });
    }
}
