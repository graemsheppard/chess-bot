package com.graemsheppard.chessbot.ui;

import com.graemsheppard.chessbot.Board;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class MainPanel extends JPanel {

    public MainPanel (Board board) {

        this.setSize(new Dimension(9 * Constants.BOARD_SCALE, 9 * Constants.BOARD_SCALE));
        this.setPreferredSize(new Dimension(9 * Constants.BOARD_SCALE, 9 * Constants.BOARD_SCALE));
        this.setBackground(Color.getHSBColor(0.6f, 0.5f, 0.7f));
        this.setLayout(new GridBagLayout());
        BoardPanel boardPanel = new BoardPanel(board);
        this.add(boardPanel);
        this.doLayout();
    }

    public BufferedImage createImage() {
        BufferedImage bi = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g2d = bi.createGraphics();
        this.paint(g2d);
        return bi;
    }

    public InputStream getImageStream() {
        BufferedImage img = this.createImage();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "png", outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    @Override
    public void paint (Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.getHSBColor(0.13f, 0.33f, 1));
        for (int x = 0; x < 8; x++) {
            char letter = (char) ('a' + x);
            g2d.drawString(String.valueOf(letter), Constants.BOARD_SCALE * (x + 0.9f), 8.85f * Constants.BOARD_SCALE);
        }

        for (int y = 0; y < 8; y++) {
            char letter = (char) ('1' + y);
            g2d.drawString(String.valueOf(letter), 0.15f * Constants.BOARD_SCALE, Constants.BOARD_SCALE * (8.1f - y));
        }

    }

}
