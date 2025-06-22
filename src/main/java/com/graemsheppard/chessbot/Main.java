package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.ui.Frame;
import discord4j.core.DiscordClient;
import reactor.core.publisher.Mono;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        final AppConfig config = AppConfig.getInstance();

        DiscordClient client = DiscordClient.create(config.getString("discord.bot.token"));
        Mono<Void> login = client.withGateway(ChessGateway::create);
        login.block();
//        ChessGame game = new ChessGame();
//        Scanner scanner = new Scanner(System.in);
//        Frame frame = new Frame(game);
//
//        while (true) {
//            System.out.print("\n" + game.getTurn().toString() + " MOVE: ");
//            String command = scanner.nextLine().trim();
//            if (game.move(command)) {}
//                frame.redraw();
//        }

    }





}
