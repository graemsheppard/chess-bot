package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.pieces.*;
import lombok.Getter;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.stream.Collectors;

public class ChessGame {

    @Getter
    private Piece[][] board;

    @Getter
    private List<Piece> rooks;

    @Getter
    private List<Piece> bishops;

    @Getter
    private List<Piece> knights;

    @Getter
    private List<Piece> queens;

    @Getter
    private List<Piece> kings;

    @Getter
    private List<Piece> pawns;

    @Getter
    private List<Piece> capturedWhites;

    @Getter
    private List<Piece> capturedBlacks;

    @Getter
    private Color turn = Color.WHITE;

    public ChessGame() {
        capturedWhites = new ArrayList<>(15);
        capturedBlacks = new ArrayList<>(15);

        board = new Piece[8][8];

        pawns = new ArrayList<>(16);

        // Initialize Pawns
        for (int i = 0; i < 8; i++) {
            Pawn wPawn = new Pawn(Color.WHITE, new Location('a', '2').addFiles(i));
            Pawn bPawn = new Pawn(Color.BLACK, new Location('a', '7').addFiles(i));

            int[] wIdx = wPawn.asIndex();
            int[] bIdx = bPawn.asIndex();

            pawns.add(wPawn);
            pawns.add(bPawn);

            board[wIdx[0]][wIdx[1]] = wPawn;
            board[bIdx[0]][bIdx[1]] = bPawn;

        }

        // Initialize Rooks
        Rook wRook1 = new Rook(Color.WHITE, new Location('a', '1'));
        Rook wRook2 = new Rook(Color.WHITE, new Location('h', '1'));
        board[wRook1.asIndex()[0]][wRook1.asIndex()[1]] = wRook1;
        board[wRook2.asIndex()[0]][wRook2.asIndex()[1]] = wRook2;

        Rook bRook1 = new Rook(Color.BLACK, new Location('a', '8'));
        Rook bRook2 = new Rook(Color.BLACK, new Location('h', '8'));
        board[bRook1.asIndex()[0]][bRook1.asIndex()[1]] = bRook1;
        board[bRook2.asIndex()[0]][bRook2.asIndex()[1]] = bRook2;

        rooks = new ArrayList<>(List.of(wRook1, wRook2, bRook1, bRook2));

        // Initialize Knights
        Knight wKnight1 = new Knight(Color.WHITE, new Location('b', '1'));
        Knight wKnight2 = new Knight(Color.WHITE, new Location('g', '1'));
        board[wKnight1.asIndex()[0]][wKnight1.asIndex()[1]] = wKnight1;
        board[wKnight2.asIndex()[0]][wKnight2.asIndex()[1]] = wKnight2;

        Knight bKnight1 = new Knight(Color.BLACK, new Location('b', '8'));
        Knight bKnight2 = new Knight(Color.BLACK, new Location('g', '8'));
        board[bKnight1.asIndex()[0]][bKnight1.asIndex()[1]] = bKnight1;
        board[bKnight2.asIndex()[0]][bKnight2.asIndex()[1]] = bKnight2;

        knights = new ArrayList<>(List.of(wKnight1, wKnight2, bKnight1, bKnight2));

        // Initialize Bishops
        Bishop wBishop1 = new Bishop(Color.WHITE, new Location('c', '1'));
        Bishop wBishop2 = new Bishop(Color.WHITE, new Location('f', '1'));
        board[wBishop1.asIndex()[0]][wBishop1.asIndex()[1]] = wBishop1;
        board[wBishop2.asIndex()[0]][wBishop2.asIndex()[1]] = wBishop2;

        Bishop bBishop1 = new Bishop(Color.BLACK, new Location('c', '8'));
        Bishop bBishop2 = new Bishop(Color.BLACK, new Location('f', '8'));
        board[bBishop1.asIndex()[0]][bBishop1.asIndex()[1]] = bBishop1;
        board[bBishop2.asIndex()[0]][bBishop2.asIndex()[1]] = bBishop2;

        bishops = new ArrayList<>(List.of(wBishop1, wBishop2, bBishop1, bBishop2));

        // Initialize Queens
        Queen wQueen = new Queen(Color.WHITE, new Location('d', '1'));
        Queen bQueen = new Queen(Color.BLACK, new Location('d', '8'));
        board[wQueen.asIndex()[0]][wQueen.asIndex()[1]] = wQueen;
        board[bQueen.asIndex()[0]][bQueen.asIndex()[1]] = bQueen;

        queens = new ArrayList<>(List.of(wQueen, bQueen));

        // Initialize Kings
        King wKing = new King(Color.WHITE, new Location('e', '1'));
        King bKing = new King(Color.BLACK, new Location('e', '8'));
        board[wKing.asIndex()[0]][wKing.asIndex()[1]] = wKing;
        board[bKing.asIndex()[0]][bKing.asIndex()[1]] = bKing;

        kings = new ArrayList<>(List.of(wKing, bKing));

    }

    public boolean move(String command) {
        if (command == null || command.length() < 2)
            return false;

        if (command.equals("0-0")) {
            return castleKingside();
        }

        if (command.equals("0-0-0")) {
            return castleQueenside();
        }

        Piece pieceToMove;
        boolean isPromotion = false;
        MoveType moveType;
        Location destination;
        if ("QNBR".contains(String.valueOf(command.charAt(command.length() - 1)))) {
            isPromotion = true;
            destination = new Location(command.substring(command.length() - 3, command.length() - 1));
            if (command.length() >= 4 && command.charAt(command.length() - 4) == 'x')
                moveType = MoveType.ATTACK;
            else
                moveType = MoveType.MOVE;
        } else {
            destination = new Location(command.substring(command.length() - 2, command.length()));
            if (command.length() >= 3 && command.charAt(command.length() - 3) == 'x')
                moveType = MoveType.ATTACK;
            else
                moveType = MoveType.MOVE;
        }

        if (!destination.isValid())
            return false;

        switch (command.charAt(0)) {
            case 'R':
                pieceToMove = getPieceToMove(rooks, destination, moveType, isPromotion, command);
                break;
            case 'B':
                pieceToMove = getPieceToMove(bishops, destination, moveType, isPromotion, command);
                break;
            case 'N':
                pieceToMove = getPieceToMove(knights, destination, moveType, isPromotion, command);
                break;
            case 'Q':
                pieceToMove = getPieceToMove(queens, destination, moveType, isPromotion, command);
                break;
            case 'K':
                pieceToMove = getPieceToMove(kings, destination, moveType, isPromotion, command);
                break;
            default:
                pieceToMove = getPieceToMove(pawns, destination, moveType, isPromotion, command);
                break;
        }

        Piece promotedPiece = null;
        List<Piece> targetList = null;
        if (isPromotion) {
            switch (command.charAt(command.length() - 1)) {
                case 'R':
                    promotedPiece = new Rook(turn, destination);
                    targetList = rooks;
                    break;
                case 'B':
                    promotedPiece = new Bishop(turn, destination);
                    targetList = bishops;
                    break;
                case 'Q':
                    promotedPiece = new Queen(turn, destination);
                    targetList = queens;
                    break;
                case 'N':
                    promotedPiece = new Knight(turn ,destination);
                    targetList = knights;
                    break;
            }
        }

        if (pieceToMove == null) {
            return false;
        }

        Piece[][] backupBoard = new Piece[8][8];
        for (int i = 0; i < 8; i++)
            backupBoard[i] = Arrays.copyOf(board[i], 8);

        Piece occupier = board[destination.asIndex()[0]][destination.asIndex()[1]];
        if (occupier != null) {
            occupier.setAlive(false);
            if (occupier.getColor() == Color.WHITE)
                capturedWhites.add(occupier);
            else
                capturedBlacks.add(occupier);
        }
        board[pieceToMove.asIndex()[0]][pieceToMove.asIndex()[1]] = null;

        if (isPromotion && pieceToMove instanceof Pawn) {
            board[destination.asIndex()[0]][destination.asIndex()[1]] = promotedPiece;
            pawns.remove(pieceToMove);
            targetList.add(promotedPiece);
        } else {
            board[destination.asIndex()[0]][destination.asIndex()[1]] = pieceToMove;
        }
        Location backupLocation = pieceToMove.getLocation();
        pieceToMove.move(destination);

        King king = (King) kings.stream().filter(k -> k.getColor() == turn).findFirst().get();
        if (tileInCheck(king.getLocation())) {
            board = backupBoard;
            pieceToMove.move(backupLocation);
            pieceToMove.setAlive(true);
            if (occupier != null) {
                occupier.setAlive(true);
            }
            return false;
        }
        turn = turn == Color.WHITE ? Color.BLACK : Color.WHITE;

        return true;
    }

    private boolean castleKingside() {

        int row = turn == Color.BLACK ? 7 : 0;
        Location rookLoc = new Location('h', (char)('0' + row + 1));
        Location kingLoc = new Location('e', (char)('0' + row + 1));
        King king = (King) kings.stream().filter(k -> k.getColor() == turn).findFirst().get();
        if (king.isHasMoved())
            return false;

        Optional<Piece> rook = rooks.stream().filter(r -> r.getLocation().equals(rookLoc)).findFirst();

        if (rook.isEmpty() || rook.get().isHasMoved())
            return false;

        ArrayList<Piece> pieces = new ArrayList<>(bishops);
        pieces.addAll(knights);
        pieces.addAll(kings);
        pieces.addAll(queens);
        pieces.addAll(pawns);
        pieces.addAll(rooks);


        if (board[5][row] != null || board[6][row] != null)
            return false;

        Piece[][] tempBoard = new Piece[8][8];
        for (int i = 0; i < 8; i++)
            tempBoard[i] = Arrays.copyOf(board[i], 8);

        Pawn filler1 = new Pawn(turn, new Location('f', (char) ('0' + row + 1)));
        Pawn filler2 = new Pawn(turn, new Location('g', (char) ('0' + row + 1)));

        tempBoard[5][row] = filler1;
        tempBoard[6][row] = filler2;

        boolean squaresUnsafe = pieces.stream()
                .filter(Piece::isAlive)
                .filter(p -> p.getColor() != turn)
                .map(p -> p.getValidMoves(tempBoard, MoveType.ATTACK))
                .flatMap(Collection::stream)
                .anyMatch(l -> l.equals(filler1.getLocation()) || l.equals(filler2.getLocation()));

        if (squaresUnsafe)
            return false;

        king.move(filler2.getLocation());
        rook.get().move(filler1.getLocation());

        board[king.asIndex()[0]][king.asIndex()[1]] = king;
        board[rook.get().asIndex()[0]][rook.get().asIndex()[1]] = rook.get();
        board[kingLoc.asIndex()[0]][kingLoc.asIndex()[1]] = null;
        board[rookLoc.asIndex()[0]][rookLoc.asIndex()[1]] = null;
        turn = turn == Color.WHITE ? Color.BLACK : Color.WHITE;
        return true;
    }

    private boolean castleQueenside() {

        int row = turn == Color.BLACK ? 7 : 0;
        Location rookLoc = new Location('a', (char)('0' + row + 1));
        Location kingLoc = new Location('e', (char)('0' + row + 1));
        King king = (King) kings.stream().filter(k -> k.getColor() == turn).findFirst().get();
        if (king.isHasMoved())
            return false;

        Optional<Piece> rook = rooks.stream().filter(r -> r.getLocation().equals(rookLoc)).findFirst();

        if (rook.isEmpty() || rook.get().isHasMoved())
            return false;

        ArrayList<Piece> pieces = new ArrayList<>(bishops);
        pieces.addAll(knights);
        pieces.addAll(kings);
        pieces.addAll(queens);
        pieces.addAll(pawns);
        pieces.addAll(rooks);


        if (board[1][row] != null || board[2][row] != null && board[3][row] != null)
            return false;

        Piece[][] tempBoard = new Piece[8][8];
        for (int i = 0; i < 8; i++)
            tempBoard[i] = Arrays.copyOf(board[i], 8);

        Pawn filler1 = new Pawn(turn, new Location('c', (char) (row + '0' + 1)));
        Pawn filler2 = new Pawn(turn, new Location('d', (char) (row + '0' + 1)));

        tempBoard[2][row] = filler1;
        tempBoard[3][row] = filler2;

        boolean squaresUnsafe = pieces.stream()
                .filter(Piece::isAlive)
                .filter(p -> p.getColor() != turn)
                .map(p -> p.getValidMoves(tempBoard, MoveType.ATTACK))
                .flatMap(Collection::stream)
                .anyMatch(l -> l.equals(filler1.getLocation()) || l.equals(filler2.getLocation()));

        if (squaresUnsafe)
            return false;

        king.move(filler1.getLocation());
        rook.get().move(filler2.getLocation());

        board[king.asIndex()[0]][king.asIndex()[1]] = king;
        board[rook.get().asIndex()[0]][rook.get().asIndex()[1]] = rook.get();
        board[kingLoc.asIndex()[0]][kingLoc.asIndex()[1]] = null;
        board[rookLoc.asIndex()[0]][rookLoc.asIndex()[1]] = null;
        turn = turn == Color.WHITE ? Color.BLACK : Color.WHITE;
        return true;
    }

    private Piece getPieceToMove(List<Piece> pieces, Location destination, MoveType moveType, boolean promotion, String command) {

        Piece[] candidates = pieces.stream()
                .filter (p -> p.getColor() == turn)
                .filter(Piece::isAlive)
                .filter(p -> p.getValidMoves(board, moveType).stream().anyMatch(p2 -> p2.equals(destination)))
                .toArray(Piece[]::new);

        int desiredLength = promotion ? 3 : 2;
        boolean isPawn = pieces.get(0).getCharacter() == 'p';
        if (!isPawn)
            desiredLength += 1;

        if (getPieceAt(destination) != null)
            desiredLength += 1;

        if (candidates.length == 0)
            return null;
        if (candidates.length == 1) {
            if (command.length() == desiredLength)
                return candidates[0];
        }

        // Disambiguate moves
        if (candidates.length == 2) {
            // Pieces are not on the same file, file should be specified
            if (candidates[0].getLocation().getFile() != candidates[1].getLocation().getFile()) {
                desiredLength += 1;
                if (command.length() == desiredLength) {
                    char file = isPawn ? command.charAt(0) : command.charAt(1);
                    Piece[] finalCandidates = Arrays.stream(candidates).filter(p -> p.getLocation().getFile() == file).toArray(Piece[]::new);
                    if (finalCandidates.length == 1)
                        return finalCandidates[0];
                }
            // Pieces are on same file, but different rank, rank should be specified
            } else if (candidates[0].getLocation().getRank() != candidates[1].getLocation().getRank()) {
                desiredLength += 1;
                if (command.length() == desiredLength) {
                    char rank = isPawn ? command.charAt(0) : command.charAt(1);
                    Piece[] finalCandidates = Arrays.stream(candidates).filter(p -> p.getLocation().getRank() == rank).toArray(Piece[]::new);
                    if (finalCandidates.length == 1)
                        return finalCandidates[0];
                }
            }
        }
        // TODO: Handle case where file and rank must be specified

        return null;
    }

    private Piece getPieceAt(Location location) {
        int[] idx = location.asIndex();
        return this.board[idx[0]][idx[1]];
    }

    public static Piece getPieceAt(Location location, Piece[][] board) {
        int[] idx = location.asIndex();
        return board[idx[0]][idx[1]];
    }

    public boolean doRandomMove() {
        Object[] randomMoveData = getRandomMove();
        Piece pieceToMove = (Piece) randomMoveData[0];
        Location destination = (Location) randomMoveData[1];
        Piece occupier = board[destination.asIndex()[0]][destination.asIndex()[1]];
        if (occupier != null) {
            occupier.setAlive(false);
            if (occupier.getColor() == Color.WHITE)
                capturedWhites.add(occupier);
            else
                capturedBlacks.add(occupier);
        }
        board[pieceToMove.asIndex()[0]][pieceToMove.asIndex()[1]] = null;
        pieceToMove.move(destination);
        board[destination.asIndex()[0]][destination.asIndex()[1]] = pieceToMove;
        turn = turn == Color.WHITE ? Color.BLACK : Color.WHITE;

        return true;
    }

    private Object[] getRandomMove() {
        ArrayList<Piece> pieces = new ArrayList<>(bishops);
        pieces.addAll(knights);
        pieces.addAll(kings);
        pieces.addAll(queens);
        pieces.addAll(pawns);
        pieces.addAll(rooks);

        pieces = new ArrayList<Piece>(pieces.stream().filter(p -> p.getColor() == this.turn && p.isAlive()).toList());
        Collections.shuffle(pieces);

        Location location = null;
        Piece piece = null;
        int idx = 0;
        while (location == null && idx < pieces.size()) {
            piece = pieces.get(idx);
            ArrayList<Location> validMoves = new ArrayList<Location>(piece.getValidMoves(board, MoveType.MOVE));
            validMoves.addAll(piece.getValidMoves(board, MoveType.ATTACK));
            validMoves = new ArrayList<>(validMoves.stream().filter(Location::isValid).toList());
            if (validMoves.size() > 0) {
                Random random = new Random();
                int k = random.nextInt(0, validMoves.size());
                location = validMoves.get(k);
            }
            idx += 1;
        }

        return new Object[] { piece, location };
    }

    public String getCapturedString(Color color) {
        ArrayList<Piece> pieces = new ArrayList<>(bishops);
        pieces.addAll(knights);
        pieces.addAll(kings);
        pieces.addAll(queens);
        pieces.addAll(pawns);
        pieces.addAll(rooks);

        pieces = new ArrayList<>(pieces.stream()
                .filter(p -> !p.isAlive() && p.getColor().equals(color)).toList());

        StringBuilder builder = new StringBuilder();
        pieces.stream().sorted((p1, p2) -> p1.getValue() > p2.getValue() ? 1 : -1)
                .forEach(p -> {
                    builder.append(p.getCodePoint());
                });
        return builder.toString();
    }

    public boolean tileInCheck(Location location) {
        Piece filler = new Pawn(turn, location);
        Piece[][] tempBoard = new Piece[8][8];
        for (int i = 0; i < 8; i++) {
            tempBoard[i] = Arrays.copyOf(board[i], 8);
        }

        tempBoard[filler.asIndex()[0]][filler.asIndex()[1]] = filler;

        ArrayList<Piece> pieces = new ArrayList<>(bishops);
        pieces.addAll(knights);
        pieces.addAll(kings);
        pieces.addAll(queens);
        pieces.addAll(pawns);
        pieces.addAll(rooks);

        return pieces.stream()
                .filter(Piece::isAlive)
                .filter(p -> p.getColor() != turn)
                .flatMap(p -> p.getValidMoves(tempBoard, MoveType.ATTACK).stream())
                .anyMatch(l -> l.equals(location));
    }


}
