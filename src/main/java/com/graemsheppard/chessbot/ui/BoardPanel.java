package com.graemsheppard.chessbot.ui;

import com.graemsheppard.chessbot.Board;
import com.graemsheppard.chessbot.ChessGame;
import com.graemsheppard.chessbot.Location;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

class BoardPanel extends JPanel {

    private final ChessGame game;

    private HashMap<String, Image> images;
    public BoardPanel(ChessGame game) {

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
        this.game = game;
        this.setSize(Constants.BOARD_DIM);
        this.setPreferredSize(Constants.BOARD_DIM);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        for (int i = 0; i < 8; i ++) {
            for (int j = 0; j < 8; j++) {

                Location current = new Location((char)('a' + i), (char)('1' + j));
                if ((j % 2 == 0 && i % 2 == 0) || (j % 2 == 1 && i % 2 == 1)) {
                    g2d.setColor(Color.getHSBColor(0.6f, 0.5f, 0.6f));
                    g2d.fillRect(i * Constants.BOARD_SCALE, (7 - j) * Constants.BOARD_SCALE, Constants.BOARD_SCALE, Constants.BOARD_SCALE);
                }
                if (game.getLastMoveStart()!= null && current.equals(game.getLastMoveStart())
                        ||game.getLastMoveEnd() != null && current.equals(game.getLastMoveEnd())) {
                    g2d.setColor(new Color(0, 0.67f, 1, 0.6f));
                    g2d.fillRect(i * Constants.BOARD_SCALE, (7 - j) * Constants.BOARD_SCALE, Constants.BOARD_SCALE, Constants.BOARD_SCALE);
                }
                if (this.game.getBoard().getGrid()[i][j] != null)
                    g2d.drawImage(this.images.get(this.game.getBoard().getGrid()[i][j].getImgPath()), i * Constants.BOARD_SCALE, (7 - j) * Constants.BOARD_SCALE, null);
            }
        }
    }
}
