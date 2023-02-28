package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.pieces.*;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Board {

    @Getter
    private final Piece[][] grid;

    @Getter
    private final King wKing;

    @Getter
    private final King bKing;

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
        this.wKing = wKing;
        this.bKing = bKing;
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
        Piece piece = move.getPiece();
        if (move.getPromotionType() != null) {
            try {
                piece = move.getPromotionType().getDeclaredConstructor(Color.class, Location.class).newInstance(piece.getColor(), piece.getLocation());
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        setBoardAt(piece.getLocation(), null);
        piece.move(move.getDestination());
        setOnBoard(piece);
    }

    /**
     * @param color The color for which these tiles are unsafe
     * @return A list of locations that are unsafe
     */
    public List<Location> getUnsafeTiles(Color color) {
        return this.getPieces()
                .filter(p -> p.getColor() != color)
                .flatMap(p -> p.getAttackingTiles(this).stream())
                .toList();
    }

    public boolean kingInCheck(Color color) {
        King king = color == Color.WHITE ? wKing : bKing;
        return this.getUnsafeTiles(color)
                .stream().anyMatch(l -> l.equals(king.getLocation()));
    }
}
