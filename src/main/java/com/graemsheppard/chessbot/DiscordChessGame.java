package com.graemsheppard.chessbot;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import com.graemsheppard.chessbot.enums.Color;
import discord4j.core.object.entity.channel.ThreadChannel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class DiscordChessGame extends ChessGame {

    @Getter
    private final User white;
    @Getter
    private final User black;
    @Getter
    @Setter
    private Message message;

    @Getter
    @Setter
    private ThreadChannel thread;

    @Getter
    private final UUID gameId;

    public DiscordChessGame(User white, User black) {
        this.gameId = UUID.randomUUID();
        this.white = white;
        this.black = black;
    }

    public User getCurrentUser() {
        if (this.getTurn() == Color.WHITE)
            return this.white;
        else
            return this.black;
    }

    public Stream<User> getUsers() {
        return Stream.of(white, black);
    }

    public User getWinnerAsUser() {
        if (getWinner() == null)
            return null;
        if (getWinner() == Color.WHITE)
            return white;
        else
            return black;
    }
}
