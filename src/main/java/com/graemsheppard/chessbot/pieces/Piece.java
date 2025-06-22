package com.graemsheppard.chessbot.pieces;

import com.graemsheppard.chessbot.Board;
import com.graemsheppard.chessbot.Location;
import com.graemsheppard.chessbot.Move;
import com.graemsheppard.chessbot.enums.Color;
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
    protected int value = 0;

    @Getter
    @Setter
    protected boolean moved = false;

    @Getter
    protected String imgPath;

    @Getter
    @Setter
    protected Location location = null;
    @Getter
    protected Color color = null;
    @Getter
    protected char character = ' ';

    public void move(Location location) {
        this.moved = true;
        this.location = location;
    }

    /**
     * @return All possible moves that can be made by this piece.
     */
    public abstract List<Move> getValidMoves(Board board);

    /**
     * @return The list of locations under attack by this piece. Includes locations that are occupied by the same color
     * (defended) and pieces that are occupied by the opposite color (attacked). Queens, Rooks, and Bishops "see through"
     * opposite colored Kings.
     */
    public abstract List<Location> getAttackingTiles(Board board);



}
