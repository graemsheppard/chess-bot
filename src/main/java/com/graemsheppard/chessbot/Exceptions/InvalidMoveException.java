package com.graemsheppard.chessbot.Exceptions;

public class InvalidMoveException extends RuntimeException {

    public InvalidMoveException(String message) {
        super(message);
    }
}
