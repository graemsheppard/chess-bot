package com.graemsheppard.chessbot;


import com.graemsheppard.chessbot.ui.MainPanel;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommand;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.core.spec.*;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Color;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChessGateway {

    private static final ConcurrentHashMap<Snowflake, DiscordChessGame> activeGames = new ConcurrentHashMap<>();

    public static Publisher<?> create(GatewayDiscordClient gateway) {

        return registerCommands(gateway)
                .and(commandHandler(gateway))
                .and(messageHandler(gateway));
    }

    private static Mono<Void> registerCommands(GatewayDiscordClient gateway) {
        final AppConfig config = AppConfig.getInstance();
        ApplicationCommandRequest chessRequest = ApplicationCommandRequest.builder()
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

        ApplicationCommandRequest resignRequest = ApplicationCommandRequest.builder()
                .name("resign")
                .description("Resign from game taking place in this thread, losing instantly")
                .type(ApplicationCommand.Type.CHAT_INPUT.getValue())
                .build();

        long clientId = (long) config.getValue("discord.application.client_id");

        // Register commands with discord
        return gateway.getGuilds().flatMap(g ->
                gateway.getRestClient()
                        .getApplicationService()
                        .createGuildApplicationCommand(clientId, g.getId().asLong(), chessRequest)
                        .and(
                                gateway.getRestClient()
                                        .getApplicationService()
                                        .createGuildApplicationCommand(clientId, g.getId().asLong(), resignRequest)
                        )
        ).then();

    }

    private static Mono<Void> commandHandler(GatewayDiscordClient gateway) {
        return gateway.on(ChatInputInteractionEvent.class, event -> {
            if (event.getCommandName().equals("resign")) {
                return event.getInteraction().getChannel()
                        .ofType(ThreadChannel.class)
                        .flatMap(t -> {
                            if (activeGames.containsKey(t.getId())) {
                                DiscordChessGame game = activeGames.get(t.getId());
                                if (game.getWhite().getId().equals(event.getInteraction().getUser().getId())
                                        || game.getBlack().getId().equals(event.getInteraction().getUser().getId())) {
                                    activeGames.remove(t.getId());
                                    return t.delete();
                                }
                            }
                            return Mono.empty();
                        });
            } else if (event.getCommandName().equals("chess")) {
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

                DiscordChessGame game = new DiscordChessGame(user1, user2);

                String fullName1 =  game.getWhite().getUsername() + "#" + game.getWhite().getDiscriminator();
                String fullName2 = game.getBlack().getUsername() + "#" + game.getBlack().getDiscriminator();

                String gameName = fullName1 + " vs. " + fullName2 + " on " + dtf.format(LocalDateTime.now());

                if (gateway.getSelfId().equals(game.getWhite().getId()))
                    game.doRandomMove();

                MainPanel panel = new MainPanel(game);

                InputStream is = panel.getImageStream();

                MessageCreateFields.File f = MessageCreateFields.File.of("game.png", panel.getImageStream());

                Mono<Void> replyWithImage = event.reply(InteractionApplicationCommandCallbackSpec.builder()
                        .files(List.of(f))
                        .addEmbed(mainEmbed(game))
                        .build());

                Mono<Void> startThread = event.getReply().flatMap(r -> {
                    game.setMessage(r);
                    return r.startThread(StartThreadSpec.builder()
                                    .name(gameName)
                                    .autoArchiveDuration(ThreadChannel.AutoArchiveDuration.DURATION2)
                                    .build())
                            .flatMap(t -> {
                                activeGames.put(t.getId(), game);
                                return Mono.empty();
                            }).then();
                });

                return replyWithImage.then(startThread);
            }
            return Mono.empty();
        }).then();
    }



    private static Mono<Void> messageHandler(GatewayDiscordClient gateway) {
        return gateway.on(MessageCreateEvent.class, event -> {
            User user = event.getMessage().getAuthor().get();
            return event.getMessage().getChannel()
                    .filter(c -> c instanceof ThreadChannel)
                    .filter(c -> activeGames.containsKey(c.getId()))
                    .flatMap(c -> {

                        DiscordChessGame game = activeGames.get(c.getId());

                        if (!user.getId().equals(game.getCurrentUser().getId()))
                            return event.getMessage().delete();

                        else if (!game.move(event.getMessage().getContent()))
                            return event.getMessage().delete();

                        MainPanel panel = new MainPanel(game);

                        Mono<Message> userEdit = game.getMessage().edit()
                                .withFiles(MessageCreateFields.File.of("game.png", panel.getImageStream()))
                                .withEmbeds(mainEmbed(game).withColor(turnColor(game)))
                                .withAttachments();

                        Mono<Message> botEdit = Mono.fromRunnable(() -> {
                            if (game.getCurrentUser().getId().equals(gateway.getSelfId())) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                game.doRandomMove();
                                game.getMessage().edit()
                                        .withFiles(MessageCreateFields.File.of("game.png", panel.getImageStream()))
                                        .withEmbeds(mainEmbed(game).withColor(turnColor(game)))
                                        .withAttachments()
                                        .block();
                            }
                        });

                        return userEdit.then(botEdit);
                    });
        }).then();
    }

    private static EmbedCreateSpec mainEmbed(DiscordChessGame game) {
        String fullName1 =  game.getWhite().getUsername() + "#" + game.getWhite().getDiscriminator();
        String fullName2 = game.getBlack().getUsername() + "#" + game.getBlack().getDiscriminator();
        return EmbedCreateSpec.builder()
                .image("attachment://game.png")
                .color(turnColor(game))
                .title("Chess Match Started")
                .addField(":white_medium_square: White ", fullName1, true)
                .addField(":black_medium_square: Black ", fullName2, true)
                .build();
    }

    private static Color turnColor(DiscordChessGame game) {
        return game.getTurn() == com.graemsheppard.chessbot.enums.Color.BLACK ? Color.BLACK : Color.WHITE;
    }
}
