package com.graemsheppard.chessbot;
import discord4j.core.DiscordClient;
import reactor.core.publisher.Mono;

public class Main {

    public static void main(String[] args) throws Exception {

        final AppConfig config = AppConfig.getInstance();

        DiscordClient client = DiscordClient.create(config.getString("discord.bot.token"));
        Mono<Void> login = client.withGateway(ChessGateway::create);
        login.block();
    }





}
