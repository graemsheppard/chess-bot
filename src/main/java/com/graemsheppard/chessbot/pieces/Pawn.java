package com.graemsheppard.chessbot.pieces;

import com.graemsheppard.chessbot.ChessGame;
import com.graemsheppard.chessbot.Color;
import com.graemsheppard.chessbot.Location;
import com.graemsheppard.chessbot.MoveType;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {

    private final int value = 1;

    public Pawn(Color color, Location location) {
        super(color, location);
        this.character = 'p';
        this.codePoint = this.color == Color.BLACK ? '\u265f' : '\u2657';
    }

    public List<Location> getValidMoves(Piece[][] board, MoveType type) {
        List<Location> possibleMoves = new ArrayList();

        if (color == Color.WHITE) {
            if (type == MoveType.MOVE) {
                Location newLoc1 = this.location.addRanks(1);
                // Piece is blocked, no need to check for double move
                if (ChessGame.getPieceAt(newLoc1, board) != null)
                    return possibleMoves;
                possibleMoves.add(newLoc1);
                if (!hasMoved) {
                    Location newLoc2 = this.location.addRanks(2);
                    if (ChessGame.getPieceAt(newLoc2, board) == null)
                        possibleMoves.add(newLoc2);
                }
            }
            else if (type == MoveType.ATTACK) {
                Location newLoc1 = this.location.addRanks(1).addFiles(1);
                if (newLoc1.isValid()) {
                    Piece piece1 = board[newLoc1.asIndex()[0]][newLoc1.asIndex()[1]];
                    if (piece1 != null && piece1.color != this.color)
                        possibleMoves.add(newLoc1);
                }
                Location newLoc2 = this.location.addRanks(1).addFiles(-1);
                if (newLoc2.isValid()) {
                    Piece piece2 = board[newLoc2.asIndex()[0]][newLoc2.asIndex()[1]];
                    if (piece2 != null && piece2.color != this.color)
                        possibleMoves.add(newLoc2);
                }
            }
        } else {
            if (type == MoveType.MOVE) {
                Location newLoc1 = this.location.addRanks(-1);
                // Piece is blocked, no need to check for double move
                if (ChessGame.getPieceAt(newLoc1, board) != null)
                    return possibleMoves;
                possibleMoves.add(newLoc1);
                if (!hasMoved) {
                    Location newLoc2 = this.location.addRanks(-2);
                    if (ChessGame.getPieceAt(newLoc2, board) == null)
                        possibleMoves.add(newLoc2);
                }
            } else if (type == MoveType.ATTACK) {
                Location newLoc1 = this.location.addRanks(-1).addFiles(1);
                if (newLoc1.isValid()) {
                    Piece piece1 = board[newLoc1.asIndex()[0]][newLoc1.asIndex()[1]];
                    if (piece1 != null && piece1.color != this.color)
                        possibleMoves.add(newLoc1);
                }
                Location newLoc2 = this.location.addRanks(-1).addFiles(-1);
                if (newLoc2.isValid()) {
                    Piece piece2 = board[newLoc2.asIndex()[0]][newLoc2.asIndex()[1]];
                    if (piece2 != null && piece2.color != this.color)
                        possibleMoves.add(newLoc2);
                }
            }
        }

        possibleMoves = possibleMoves.stream().filter(Location::isValid).toList();

        return possibleMoves;
    }

    public String getImgPath() {
        return this.color == Color.WHITE ? "wPawn.png" : "bPawn.png";
    }


}
