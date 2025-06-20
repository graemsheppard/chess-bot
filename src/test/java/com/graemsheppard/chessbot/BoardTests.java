package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.enums.Color;
import com.graemsheppard.chessbot.enums.MoveType;
import com.graemsheppard.chessbot.pieces.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BoardTests {

    @Test
    public void boardInitializesDefaultConstructor() {
        Board board = new Board();

        // Basic board information
        assertEquals(board.getMoveCount(), 0);
        assertEquals(board.getPieces().count(), 32);
        assertEquals(board.getBKing().getLocation(), new Location('e', '8'));
        assertEquals(board.getWKing().getLocation(), new Location('e', '1'));

        var pieceOrder = new char[] { 'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R' };

        // Check black pieces in grid
        for (int i = 0; i < 8; i++) {
            var piece = board.getGrid()[i][7];
            assertEquals(pieceOrder[i], piece.getCharacter());
            assertEquals(Color.BLACK, piece.getColor());
        }

        // Check white pieces in grid
        for (int i = 0; i < 8; i++) {
            var piece = board.getGrid()[i][0];
            assertEquals(pieceOrder[i], piece.getCharacter());
            assertEquals(Color.WHITE, piece.getColor());
        }
    }

    @Test
    public void setOnBoardSucceeds() {
        var board = new Board();
        var location = new Location('e', '5');
        var pawn = new Pawn(Color.BLACK, location);
        board.setOnBoard(pawn);

        var placedPawn = board.getBoardAt(location);
        assertEquals(placedPawn, pawn);
    }

    @Test
    public void setBoardAtSucceeds() {
        var board = new Board();
        var location = new Location('e', '2');
        board.setBoardAt(location, null);

        var placedPawn = board.getBoardAt(location);
        assertNull(placedPawn);
    }

    @Test
    public void getPiecesSucceeds() {
        var board = new Board();

        assertEquals(board.getPieces().count(), 32);
        var location = new Location('e', '2');
        board.setBoardAt(location, null);

        assertEquals(board.getPieces().count(), 31);
    }

    @Test
    public void movePawnForward() {
        var board = new Board();
        var pawn = board.getBoardAt(new Location('a', '7'));
        var destination = new Location('a', '6');
        var moveHandler = mock(Move.MoveHandler.class);
        assertEquals(board.getMoveCount(), 0);
        board.move(new Move(pawn, destination, MoveType.MOVE, moveHandler));

        verify(moveHandler, times(1));
        assertEquals(board.getBoardAt(destination), pawn);
        assertEquals(pawn.getLocation(), destination);
        assertEquals(board.getMoveCount(), 1);
    }

    @Test
    public void movePawnForwardAndPromote() {
        var board = new Board();
        var pawn = board.getBoardAt(new Location('a', '7'));
        var destination = new Location('a', '8');
        board.setBoardAt(destination, null);
        board.setBoardAt(pawn.getLocation(), pawn);
        var move = new Move(pawn, destination, MoveType.MOVE);
        move.setPromotionType(Rook.class);
        assertEquals(board.getMoveCount(), 0);
        board.move(move);

        assertEquals(board.getBoardAt(destination).getClass(), Rook.class);
        assertNull(board.getBoardAt(new Location('a', '7')));
        assertEquals(board.getMoveCount(), 1);
    }

    @Test
    public void getUnsafeTilesDefault() {
        var board = new Board();
        var unsafeTilesWhite = board.getUnsafeTiles(Color.WHITE);
        var unsafeTilesBlack = board.getUnsafeTiles(Color.BLACK);

        assertEquals(unsafeTilesWhite.size(), 8);
        assertEquals(unsafeTilesBlack.size(), 8);
    }

    @Test
    public void getUnsafeTilesStandardOpening() {
        var board = new Board();
        var whiteMove = new Move(board.getBoardAt(new Location('d', '2')), new Location('d', '4'), MoveType.MOVE);
        var blackMove = new Move(board.getBoardAt(new Location('e', '7')), new Location('e', '6'), MoveType.MOVE);
        board.move(whiteMove);
        board.move(blackMove);

        var unsafeTilesWhite = board.getUnsafeTiles(Color.WHITE);
        var unsafeTilesBlack = board.getUnsafeTiles(Color.BLACK);

        assertEquals(unsafeTilesWhite.size(), 16);
        assertEquals(unsafeTilesBlack.size(), 14);
    }

    @Test
    public void getKingInCheck() {
        var board = new Board();

        // Remove pawns in front of kings
        board.setBoardAt(new Location('e', '2'), null);
        board.setBoardAt(new Location('e', '7'), null);

        var queen = board.getBoardAt(new Location('d', '1'));
        board.move(new Move(queen, new Location('e', '2'), MoveType.MOVE));

        assertTrue(board.kingInCheck(Color.BLACK));
        assertFalse(board.kingInCheck(Color.WHITE));
    }

}
