package com.graemsheppard.chessbot.ui;

import com.graemsheppard.chessbot.Board;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

class BoardPanel extends JPanel {

    private final Board board;

    private HashMap<String, Image> images;
    public BoardPanel(Board board) {

        this.images = new HashMap<>();
        for (String color : new String[] { "w", "b" }) {
            for (String piece : new String[] { "King", "Queen", "Rook", "Bishop", "Knight", "Pawn" }) {
                String path = color + piece + ".png";
                try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
                    Image img = ImageIO.read(is).getScaledInstance(Constants.BOARD_SCALE, Constants.BOARD_SCALE, Image.SCALE_SMOOTH);
                    this.images.put(path, img);
                } catch (IOException e) {

                }
            }
        }
        this.board = board;
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
                if (this.board.getGrid()[i][j] != null)
                    g2d.drawImage(this.images.get(this.board.getGrid()[i][j].getImgPath()), i * Constants.BOARD_SCALE, (7 - j) * Constants.BOARD_SCALE, null);
            }
        }
    }
}
