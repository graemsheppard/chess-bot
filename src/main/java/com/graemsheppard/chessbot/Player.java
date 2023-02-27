package com.graemsheppard.chessbot;

import lombok.Getter;

public class Player {

    @Getter
    private final String name;

    public Player(String name) {
        this.name = name;
    }

}
