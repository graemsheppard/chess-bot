package com.graemsheppard.chessbot;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import lombok.Getter;
import lombok.Setter;

public class DiscordChessGame extends ChessGame {

    @Getter
    private final User white;
    @Getter
    private final User black;
    @Getter
    @Setter
    private Message message;

    public DiscordChessGame(User white, User black) {
        this.white = white;
        this.black = black;
    }

    public User getCurrentUser() {
        if (this.getTurn() == Color.WHITE)
            return this.white;
        else
            return this.black;

    }
}
