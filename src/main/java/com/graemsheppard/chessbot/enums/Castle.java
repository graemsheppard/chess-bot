package com.graemsheppard.chessbot.enums;

public enum Castle {
    KINGSIDE ("Kingside"),
    QUEENSIDE("Queenside");

    private final String name;

    private Castle(String value) {
        name = value;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
