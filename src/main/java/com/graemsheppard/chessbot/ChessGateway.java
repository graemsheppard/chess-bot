package com.graemsheppard.chessbot;


import com.graemsheppard.chessbot.ui.MainPanel;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommand;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.core.spec.*;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChessGateway {

    private static ConcurrentHashMap<Snowflake, DiscordChessGame> activeGames = new ConcurrentHashMap();

    public static Publisher<?> create(GatewayDiscordClient gateway) {

        ApplicationCommandRequest cmdRequest = ApplicationCommandRequest.builder()
                .name("chess")
                .description("Challenge a friend to a chess game")
                .type(ApplicationCommand.Type.CHAT_INPUT.getValue())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("user")
                        .description("User to play against")
                        .type(ApplicationCommandOption.Type.USER.getValue())
                        .required(true)
                        .build()
                )
                .build();

        // Register commands with discord
        Mono<Void> createCommand = gateway.getRestClient().getApplicationService()
                .createGlobalApplicationCommand((long) Main.getConfigValue("discord.application.client_id"), cmdRequest)
                .then();

        Mono<Void> onCommand = gateway.on(ChatInputInteractionEvent.class, e -> commandHandler(gateway, e)).then();

        Mono<Void> onThreadMessage = gateway.on(MessageCreateEvent.class, event -> {
            User user = event.getMessage().getAuthor().get();

            return event.getMessage().getChannel()
                    .filter(c -> c instanceof ThreadChannel)
                    .filter(c -> activeGames.containsKey(c.getId()))
                    .flatMap(c -> {

                        DiscordChessGame game = activeGames.get(c.getId());

                        if (game.getWhite().getId().equals(gateway.getSelfId())) { // Check if bot game
                            if (user.getId().equals(gateway.getSelfId())) { // Message from bot
                                if (game.getCurrentUser().getId().equals(gateway.getSelfId())) { // Bot's turn
                                    try {
                                        c.type();
                                        Thread.sleep(1200);
                                    } catch (Exception e) {

                                    }
                                    game.doRandomMove();
                                } else {
                                    return Mono.empty();
                                }
                            } else if (!game.move(event.getMessage().getContent())) { // Player's turn
                                return Mono.empty();
                            }

                        } else if (game.getCurrentUser().getId().equals(user.getId())) {
                            if (!game.move(event.getMessage().getContent())) {
                                return Mono.empty();
                            }
                        } else {
                            return Mono.empty();
                        }

                        MainPanel panel = new MainPanel(game.getBoard());
                        return c.createMessage(MessageCreateSpec.builder()
                                .addFile("game.png", panel.getImageStream())
                                .addEmbed(EmbedCreateSpec.builder()
                                        .image("attachment://game.png")
                                        .color(game.getTurn() == com.graemsheppard.chessbot.Color.BLACK ? discord4j.rest.util.Color.BLACK : discord4j.rest.util.Color.WHITE)
                                        .build())
                                .build());
                    });
        }).then();
        return createCommand.and(onCommand).and(onThreadMessage);
    }

    private static Mono<Void> commandHandler(GatewayDiscordClient gateway, ChatInputInteractionEvent event) {
        if (event.getCommandName().equals("chess")) {
            User user1 = event.getOption("user").get().getValue().get().asUser().block();
            User user2 = event.getInteraction().getUser();

            if (user1.getId().equals(user2.getId())) {
                return event.reply(InteractionApplicationCommandCallbackSpec.builder()
                        .ephemeral(true)
                        .content("You cannot challenge yourself")
                        .build());
            }

            if (activeGames.values().stream().anyMatch(game -> game.getWhite().getId().equals(user1.getId()) && game.getBlack().getId().equals(user2.getId())
                    || game.getWhite().getId().equals(user2.getId()) && game.getBlack().getId().equals(user1.getId()))) {
                return event.reply(InteractionApplicationCommandCallbackSpec.builder()
                        .ephemeral(true)
                        .content("You already have a game against that user")
                        .build());
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            String fullName1 =  user1.getUsername() + "#" + user1.getDiscriminator();
            String fullName2 = user2.getUsername() + "#" + user2.getDiscriminator();

            String gameName = fullName1 + " vs. " + fullName2 + " on " + dtf.format(LocalDateTime.now());

            DiscordChessGame game1 = new DiscordChessGame(user1, user2);

            if (gateway.getSelfId().equals(game1.getWhite().getId()))
                game1.doRandomMove();

            MainPanel panel = new MainPanel(game1.getBoard());
            InputStream is = panel.getImageStream();

            Mono<Void> replyWithImage = event.reply(InteractionApplicationCommandCallbackSpec.builder()
                    .files(List.of(MessageCreateFields.File.of("game.png", is)))
                    .addEmbed(EmbedCreateSpec.builder()
                            .image("attachment://game.png")
                            .color(game1.getTurn() == com.graemsheppard.chessbot.Color.BLACK ? discord4j.rest.util.Color.BLACK : discord4j.rest.util.Color.WHITE)
                            .title("Chess Match Started")
                            .addField(":white_medium_square: White ", fullName1, true)
                            .addField(":black_medium_square: Black ", fullName2, true)
                            .build())
                    .build());

            Mono<Void> startThread = event.getReply().flatMap(r -> {
                game1.setMessage(r);
                return r.startThread(StartThreadSpec.builder()
                                .name(gameName)
                                .autoArchiveDuration(ThreadChannel.AutoArchiveDuration.DURATION2)
                                .build())
                        .flatMap(t -> {
                            activeGames.put(t.getId(), game1);
                            return Mono.empty();
                        }).then();
            });

            return replyWithImage.then(startThread);
        }
        return Mono.empty();
    }
}
