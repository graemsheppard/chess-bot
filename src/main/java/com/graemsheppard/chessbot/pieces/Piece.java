package com.graemsheppard.chessbot.pieces;

import com.graemsheppard.chessbot.Color;
import com.graemsheppard.chessbot.Location;
import com.graemsheppard.chessbot.MoveType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public abstract class Piece {

    @Getter
    protected char codePoint;

    public Piece (Color color, Location location) {
        this.color = color;
        this.location = location;
    }

    @Getter
    protected final int value = 0;

    @Getter
    protected boolean hasMoved = false;

    @Getter
    @Setter
    protected boolean alive = true;
    @Getter
    protected Location location = null;
    @Getter
    protected Color color = null;
    @Getter
    protected char character = ' ';

    public void move(Location location) {
        this.hasMoved = true;
        this.location = location;
    }
    public abstract String getImgPath();

    public abstract List<Location> getValidMoves(Piece[][] board, MoveType type);

    public int[] asIndex() {
        return this.location.asIndex();
    }

    public static Piece tileOccupied(Location location, Piece[][] board) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] != null && board[i][j].getLocation().equals(location))
                    return board[i][j];
            }
        }
        return null;
    }

    
}
