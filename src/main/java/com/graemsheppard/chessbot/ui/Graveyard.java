package com.graemsheppard.chessbot.ui;

import com.graemsheppard.chessbot.pieces.Piece;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;

class Graveyard extends JPanel {
    private final List<Piece> pieces;

    private final HashMap<String, Image> images;

    private final com.graemsheppard.chessbot.Color color;
    public Graveyard(List<Piece> pieces, HashMap<String, Image> images, com.graemsheppard.chessbot.Color color) {
        this.color = color;
        this.pieces = pieces;
        this.images = images;
        this.setPreferredSize(new Dimension(2 * Constants.BOARD_SCALE, 8 * Constants.BOARD_SCALE));
        this.setBackground(Color.getHSBColor(0.6f, 0.5f, 0.7f));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        for (int i = 0; i < pieces.size(); i++) {
            int offset = color == com.graemsheppard.chessbot.Color.WHITE ? (i / 8) * Constants.BOARD_SCALE : Constants.BOARD_SCALE - (i / 8) * Constants.BOARD_SCALE;
            g2d.drawImage(this.images.get(pieces.get(i).getImgPath()), offset, (i % 8) * Constants.BOARD_SCALE, null);
        }
    }
}
