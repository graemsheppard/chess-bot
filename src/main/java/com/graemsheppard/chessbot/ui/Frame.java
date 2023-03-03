package com.graemsheppard.chessbot.ui;

import com.graemsheppard.chessbot.Board;
import com.graemsheppard.chessbot.ChessGame;

import javax.swing.*;
import java.awt.Color;

public class Frame extends JFrame {
    public Frame(ChessGame game) {

        MainPanel mainPanel = new MainPanel(game);

        this.add(mainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setTitle("Chess Board");
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setResizable(false);
        this.setBackground(Color.WHITE);
    }

    public void redraw() {
        this.update(this.getGraphics());
    }






}
