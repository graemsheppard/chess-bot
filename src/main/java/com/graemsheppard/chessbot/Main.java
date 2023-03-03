package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.ui.Frame;
import discord4j.core.DiscordClient;

import org.yaml.snakeyaml.Yaml;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.*;

public class Main {



    public static void main(String[] args) {

        DiscordClient client = DiscordClient.create(getConfigString("discord.bot.token"));
        Mono<Void> login = client.withGateway(ChessGateway::create);
        login.block();
        ChessGame game = new ChessGame();
        Scanner scanner = new Scanner(System.in);
        Frame frame = new Frame(game);

        while (true) {
            System.out.print("\n" + game.getTurn().toString() + " MOVE: ");
            String command = scanner.nextLine().trim();
            if (game.move(command)) {}
                frame.redraw();
        }

    }

     static HashMap<String, Object> getConfig() {
        InputStream inputStream = Main.class.getClassLoader()
                .getResourceAsStream("application.yml");
        Yaml yaml = new Yaml();
        return yaml.load(inputStream);
    }

    public static Object getConfigValue(String path) {
        HashMap<String, Object> base = getConfig();
        String[] keys = path.split("\\.");
        HashMap<String, Object> current = base;
        for (int i = 0; i < keys.length - 1; i++) {
            current = (HashMap<String, Object>) current.get(keys[i]);
        }
        return current.get(keys[keys.length - 1]);
    }

    public static String getConfigString(String path) {
        return (String) getConfigValue(path);
    }

}
