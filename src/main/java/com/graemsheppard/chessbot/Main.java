package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.ui.Frame;
import discord4j.core.DiscordClient;
import reactor.core.publisher.Mono;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        final AppConfig config = AppConfig.getInstance();

//        DiscordClient client = DiscordClient.create(config.getString("discord.bot.token"));
//        Mono<Void> login = client.withGateway(ChessGateway::create);
//        login.block();
        ChessGame game = new ChessGame();
        Frame frame = new Frame(game);

        while (true) {
            game.doRandomMove();
            Thread.sleep(100);
            System.out.println("tgest");
            frame.redraw();
        }

    }





}
