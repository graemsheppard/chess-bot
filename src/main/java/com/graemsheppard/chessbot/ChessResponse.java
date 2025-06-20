package com.graemsheppard.chessbot;

import lombok.Getter;

public class ChessResponse {

    @Getter
    private final ChessResponseType type;

    @Getter
    private final String message;

    public ChessResponse(ChessResponseType type) {
        this.type = type;
        this.message = null;
    }

    public ChessResponse(ChessResponseType type, String message) {
        this.type = type;
        this.message = message;
    }

    private enum ChessResponseType {
        SUCCESS,
        ERROR,
        WIN
    }
}
