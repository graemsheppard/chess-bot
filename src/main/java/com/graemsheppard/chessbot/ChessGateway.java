package com.graemsheppard.chessbot;


import com.graemsheppard.chessbot.Exceptions.InvalidMoveException;
import com.graemsheppard.chessbot.ui.MainPanel;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommand;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.core.spec.*;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChessGateway {

    private static final ConcurrentHashMap<Snowflake, DiscordChessGame> activeGames = new ConcurrentHashMap<>();

    public static Publisher<?> create(GatewayDiscordClient gateway) {

        return registerCommands(gateway)
                .and(commandHandler(gateway))
                .and(messageHandler(gateway));
    }

    /**
     * Registers slash commands with discord to autofill
     * @param gateway
     * @return A handler
     */
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

    private static Mono<Void> deleteGame(DiscordChessGame game) {
        activeGames.remove(game.getThread().getId());
        return game.getThread().delete();
    }

    /**
     * Handler for /resign
     * @param gateway
     * @param event
     * @return
     */
    private static Mono<Void> resignHandler(GatewayDiscordClient gateway, ChatInputInteractionEvent event) {
        return !event.getCommandName().equals("resign") ? Mono.empty() :
                event.getInteraction()
                .getChannel()
                .ofType(ThreadChannel.class)
                .filter(thread -> activeGames.containsKey(thread.getId()))
                .flatMap(thread -> Mono.just(activeGames.get(thread.getId())))
                .filter(game -> game.getCurrentUser().getId().equals(event.getInteraction().getUser().getId()))
                .flatMap(ChessGateway::deleteGame);
    }

    /**
     * Handler for /chess, starts a new game against a user or the bot
     * @param gateway
     * @param event
     * @return
     */
    private static Mono<Void> chessHandler (GatewayDiscordClient gateway,  ChatInputInteractionEvent event) {
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

            DiscordChessGame game = new DiscordChessGame(user1, user2);

            String fullName1 =  game.getWhite().getUsername();
            String fullName2 = game.getBlack().getUsername();

            String gameName = fullName1 + " vs. " + fullName2 + " on " + dtf.format(LocalDateTime.now());

            if (gateway.getSelfId().equals(game.getWhite().getId()))
                game.doRandomMove();

            MainPanel panel = new MainPanel(game);

            MessageCreateFields.File f = MessageCreateFields.File.of("game.png", panel.getImageStream());


            // Create the reply with the image and embed
            Mono<Void> replyWithImage = event.reply(InteractionApplicationCommandCallbackSpec.builder()
                    .files(List.of(f))
                    .addEmbed(mainEmbed(game))
                    .build());

            // Create the thread and adds the buttons to the original message
            Mono<Void> startThread = event.getReply().flatMap(r -> {
                game.setMessage(r);
                return r.startThread(StartThreadFromMessageSpec.builder()
                                .name(gameName)
                                .autoArchiveDuration(ThreadChannel.AutoArchiveDuration.DURATION2)
                                .build())
                        .flatMap(t -> {
                            game.setThread(t);
                            activeGames.put(t.getId(), game);
                            Button btnResign = Button.secondary(getResignButtonId(game), "Resign");
                            Button btnDraw = Button.secondary("btn-draw", "Draw");
                            return r.edit().withComponents(ActionRow.of(btnResign, btnDraw)).then();
                        });
            });

            return replyWithImage.then(startThread);
        }
        return Mono.empty();
    }

    private static Mono<Void> commandHandler(GatewayDiscordClient gateway) {
        return gateway.on(ChatInputInteractionEvent.class, event ->
                        resignHandler(gateway, event)).then()
                .and(gateway.on(ChatInputInteractionEvent.class, event ->
                        chessHandler(gateway, event)).then())
                .and(gateway.on(ButtonInteractionEvent.class, event ->
                        resignButtonHandler(gateway, event)));
    }

    /**
     * Creates the listener for message create and filters to thread messages where the thread is in activeGames
     * Calls the move handler for messages meeting these criteria
     * @param gateway
     * @return A handler
     */
    private static Mono<Void> messageHandler(GatewayDiscordClient gateway) {
        return gateway.on(MessageCreateEvent.class, event ->
                event.getMessage().getChannel()
                .ofType(ThreadChannel.class)
                .filter(thread -> activeGames.containsKey(thread.getId()))
                .flatMap(thread -> moveHandler(gateway, event, thread)))
                .then();
    }

    /**
     * Called when a message is sent to an active game channel, verifies user messaging is the current player
     * before attempting the requested move
     * @param event a MessageCreateEvent
     * @param thread the ThreadChannel of the current game
     * @return A handler
     */
    private static Mono<Void> moveHandler(GatewayDiscordClient gateway, MessageCreateEvent event, ThreadChannel thread) {

        User user = event.getMember().get();
        DiscordChessGame game = activeGames.get(thread.getId());

        if (!user.getId().equals(game.getCurrentUser().getId()))
            return event.getMessage().delete();

        else {
            try {
                game.move(event.getMessage().getContent());
            } catch (InvalidMoveException e) {
                return event.getMessage().delete();
                // TODO: return error to user without cluttering the thread
            }
        }

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

        return userEdit
                .then(botEdit)
                .then();
    }

    private static Mono<Void> resignButtonHandler(GatewayDiscordClient gateway, ButtonInteractionEvent event) {
        ButtonEvent btnEvent = getButtonEventFromButtonId(event.getCustomId());
        if (btnEvent == null)
            return Mono.empty();

        if (btnEvent.type == ButtonEventType.RESIGN) {
            DiscordChessGame game = activeGames.get(btnEvent.gameId);
            if (game == null)
                return Mono.empty();
            Optional<User> user = game.getUsers().filter(u -> u.getId().equals(event.getInteraction().getUser().getId())).findAny();
            if (user.isEmpty())
                return Mono.empty();
            com.graemsheppard.chessbot.enums.Color userColor = game.getWhite().getId().equals(user.get().getId()) ?
                    com.graemsheppard.chessbot.enums.Color.WHITE :
                    com.graemsheppard.chessbot.enums.Color.BLACK;
            game.resign(userColor);
            return game.getMessage().edit().withEmbeds(mainEmbed(game)).then();
        }

        return Mono.empty();
    }

    private enum ButtonEventType {
        RESIGN,
        DRAW
    }

    @AllArgsConstructor
    private static class ButtonEvent {
        @Getter
        private ButtonEventType type;

        @Getter
        private Snowflake gameId;
    }

    private static ButtonEvent getButtonEventFromButtonId(String customId) {
        String[] splitString = customId.split(":");
        if (splitString.length != 2)
            return null;
        ButtonEventType type = splitString[0].equals("resign") ? ButtonEventType.RESIGN :
                splitString[0].equals("draw") ? ButtonEventType.DRAW : null;
        if (type == null)
            return null;
        return new ButtonEvent(type, Snowflake.of(splitString[1]));
    }

    private static String getResignButtonId(DiscordChessGame game) {
        return "resign:" + game.getThread().getId().asString();
    }

    private static EmbedCreateSpec mainEmbed(DiscordChessGame game) {
        String fullName1 =  game.getWhite().getUsername();
        String fullName2 = game.getBlack().getUsername();
        User winner = game.getWinnerAsUser();
        String title = winner == null ? "Chess Match Started" : "Winner: " + winner.getUsername();
        return EmbedCreateSpec.builder()
                .image("attachment://game.png")
                .color(turnColor(game))
                .title(title)
                .addField(":white_medium_square: White ", fullName1, true)
                .addField(":black_medium_square: Black ", fullName2, true)
                .build();
    }

    private static Color turnColor(DiscordChessGame game) {
        return game.getTurn() == com.graemsheppard.chessbot.enums.Color.BLACK ? Color.BLACK : Color.WHITE;
    }
}
