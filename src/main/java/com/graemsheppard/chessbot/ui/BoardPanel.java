package com.graemsheppard.chessbot.ui;

import com.graemsheppard.chessbot.pieces.Piece;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

class BoardPanel extends JPanel {

    private final Piece[][] board;

    private HashMap<String, Image> images;
    public BoardPanel(Piece[][] board, HashMap<String, Image> images) {
        this.board = board;
        this.images = images;
        this.setSize(Constants.BOARD_DIM);
        this.setPreferredSize(Constants.BOARD_DIM);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.getHSBColor(0.6f, 0.5f, 0.6f));
        for (int i = 0; i < 8; i ++) {
            for (int j = 0; j < 8; j++) {
                if ((j % 2 == 0 && i % 2 == 0) || (j % 2 == 1 && i % 2 == 1))
                    g2d.fillRect(i * Constants.BOARD_SCALE, (7 - j) * Constants.BOARD_SCALE, Constants.BOARD_SCALE, Constants.BOARD_SCALE);
                if (this.board[i][j] != null)
                    g2d.drawImage(this.images.get(this.board[i][j].getImgPath()), i * Constants.BOARD_SCALE, (7 - j) * Constants.BOARD_SCALE, null);
            }
        }
    }
}
