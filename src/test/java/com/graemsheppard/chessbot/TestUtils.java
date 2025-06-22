package com.graemsheppard.chessbot;

import com.graemsheppard.chessbot.enums.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestUtils {

    /**
     * @param filename name of the pgn file within resources/games
     * @return a list of moves we know are valid since the game comes from a real chess.com match
     */
    public static GameResult getGameResult(String filename) {
        var inputStream = getGameFile(filename);
        var inputStreamReader = new InputStreamReader(inputStream);
        var bufferedReader = new BufferedReader(inputStreamReader);

        Pattern resultPattern = Pattern.compile("(\\[Result \\\")(.*)(\\\"])");
        List<String> moveList = new ArrayList<>();
        Color winner = null;

        try {
            for (String line; (line = bufferedReader.readLine()) != null;) {
                var matcher = resultPattern.matcher(line);

                if (matcher.matches() && matcher.groupCount() == 3) {
                    var outcome = matcher.group(2);
                    if (outcome.equals("1-0"))
                        winner = Color.WHITE;
                    else if (outcome.equals("0-1"))
                        winner = Color.BLACK;
                }

                if (line.startsWith("[") || line.length() == 0)
                    continue;
                var words = line.split("\\s+");
                for (var word : words) {
                    if (word.matches("\\d+\\.") || word.equals("1/2-1/2") || word.equals("1-0") || word.equals("0-1"))
                        continue;
                    moveList.add(word);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new GameResult(filename, winner , moveList);
    }



    public static InputStream getGameFile(String filename) {
        try {
            return Thread.currentThread().getContextClassLoader().getResourceAsStream("games/" + filename);
        } catch (NullPointerException e) {
            throw new RuntimeException("File" + filename + " was not found in resource/games");
        }
    }

    @AllArgsConstructor
    public static class GameResult {

        @Getter
        private String fileName;

        @Getter
        private Color winner;

        @Getter
        private List<String> moveList;

    }
}
