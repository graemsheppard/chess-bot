package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.enums.Color;
import com.graemsheppard.chessbot.pieces.*;
import lombok.Getter;

import java.util.*;
import java.util.stream.Stream;

public class Board {

    @Getter
    protected Piece[][] grid;

    @Getter
    protected King wKing;

    @Getter
    protected King bKing;

    @Getter
    private int moveCount = 0;

    private static final HashMap<Character, Character> encodeMap = getEncodeMap();

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
        if (move.getHandler() != null) {
            move.getHandler().handle();
        }
        setOnBoard(piece);
        moveCount++;
    }

    /**
     * @param color The color for which these tiles are unsafe
     * @return A list of locations that are unsafe
     */
    public List<Location> getUnsafeTiles(Color color) {
        return this.getPieces()
                .filter(p -> p.getColor() != color)
                .flatMap(p -> p.getAttackingTiles(this).stream())
                .distinct()
                .toList();
    }

    /**
     * @param color The color of the king that may be in check
     * @return true if the king of the specified color is in check
     */
    public boolean kingInCheck(Color color) {
        King king = color == Color.WHITE ? wKing : bKing;
        return this.getUnsafeTiles(color)
                .stream().anyMatch(l -> l.equals(king.getLocation()));
    }

    /**
     * Encodes the current position into a 32-byte array
     * @return A byte array where every 4 bits represents a square and piece
     */
    public byte[] getEncoded() {
        byte[] data = new byte[32];
        for (int k = 0; k < 64; k += 2) {
            Piece piece1 = grid[k % 8][k / 8];
            Piece piece2 = grid[(k + 1) % 8][(k + 1) / 8];
            char character1 = piece1 != null ? piece1.getDescriptor() : '0';
            char character2 = piece2 != null ? piece2.getDescriptor() : '0';

            byte value = (byte) ((Character.digit(encodeMap.get(character1), 16) << 4) + (Character.digit(encodeMap.get(character2), 16)));
            data[k / 2] = value;
        }
        return data;
    }

    /**
     * Gets the current position encoded as a base64 string
     */
    public String getEncodedBase64() {
        var encoder = Base64.getEncoder();
        return encoder.encodeToString(getEncoded());
    }

    public String getEncodedHex() {
        StringBuilder hexString = new StringBuilder();
        var bytes = getEncoded();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }


    private static HashMap<Character, Character> getEncodeMap() {
        var map = new HashMap<Character, Character>();
        map.put('0', '0');
        map.put('p', '1');
        map.put('n', '2');
        map.put('b', '3');
        map.put('r', '4');
        map.put('q', '5');
        map.put('k', '6');
        map.put('P', '7');
        map.put('N', '8');
        map.put('B', '9');
        map.put('R', 'a');
        map.put('Q', 'b');
        map.put('K', 'c');
        return map;
    }
}
