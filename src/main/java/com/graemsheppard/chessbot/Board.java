package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.pieces.*;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Board {

    @Getter
    private final Piece[][] grid;

    public Board() {
        grid = new Piece[8][8];

        // Initialize Pawns
        for (int i = 0; i < 8; i++) {
            Pawn wPawn = new Pawn(Color.WHITE, new Location('a', '2').addFiles(i));
            Pawn bPawn = new Pawn(Color.BLACK, new Location('a', '7').addFiles(i));

            setOnBoard(wPawn);
            setOnBoard(bPawn);

        }

        // Initialize Rooks
        Rook wRook1 = new Rook(Color.WHITE, new Location('a', '1'));
        Rook wRook2 = new Rook(Color.WHITE, new Location('h', '1'));
        setOnBoard(wRook1);
        setOnBoard(wRook2);

        Rook bRook1 = new Rook(Color.BLACK, new Location('a', '8'));
        Rook bRook2 = new Rook(Color.BLACK, new Location('h', '8'));
        setOnBoard(bRook1);
        setOnBoard(bRook2);

        // Initialize Knights
        Knight wKnight1 = new Knight(Color.WHITE, new Location('b', '1'));
        Knight wKnight2 = new Knight(Color.WHITE, new Location('g', '1'));
        setOnBoard(wKnight1);
        setOnBoard(wKnight2);

        Knight bKnight1 = new Knight(Color.BLACK, new Location('b', '8'));
        Knight bKnight2 = new Knight(Color.BLACK, new Location('g', '8'));
        setOnBoard(bKnight1);
        setOnBoard(bKnight2);

        // Initialize Bishops
        Bishop wBishop1 = new Bishop(Color.WHITE, new Location('c', '1'));
        Bishop wBishop2 = new Bishop(Color.WHITE, new Location('f', '1'));
        setOnBoard(wBishop1);
        setOnBoard(wBishop2);

        Bishop bBishop1 = new Bishop(Color.BLACK, new Location('c', '8'));
        Bishop bBishop2 = new Bishop(Color.BLACK, new Location('f', '8'));
        setOnBoard(bBishop1);
        setOnBoard(bBishop2);

        // Initialize Queens
        Queen wQueen = new Queen(Color.WHITE, new Location('d', '1'));
        Queen bQueen = new Queen(Color.BLACK, new Location('d', '8'));
        setOnBoard(wQueen);
        setOnBoard(bQueen);

        // Initialize Kings
        King wKing = new King(Color.WHITE, new Location('e', '1'));
        King bKing = new King(Color.BLACK, new Location('e', '8'));
        setOnBoard(wKing);
        setOnBoard(bKing);
    }

    public void setOnBoard(Piece piece) {
        grid[piece.getLocation().asIndex()[0]][piece.getLocation().asIndex()[1]] = piece;
    }

    public void setBoardAt(Location location, Piece piece) {
        grid[location.asIndex()[0]][location.asIndex()[1]] = piece;
    }

    public Piece getBoardAt(Location location) {
        return grid[location.asIndex()[0]][location.asIndex()[1]];
    }

    public Stream<Piece> getPieces() {
        return Arrays.stream(this.grid).flatMap(Arrays::stream).filter(Objects::nonNull);
    }

    public void move(Move move) {
        setBoardAt(move.getPiece().getLocation(), null);
        move.getPiece().move(move.getDestination());
        move.getPiece().setMoved(true);
        setOnBoard(move.getPiece());
    }
}
