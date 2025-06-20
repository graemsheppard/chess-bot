package com.graemsheppard.chessbot;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

public class ChessContextMock implements ChessContext {

    private HashMap<Long, List<String>> database;

    private ChessContextMock() {
        database = new HashMap<>();
        database.put(1L, List.of("e4", "e5", "d4", "xd4"));
        database.put(2L, List.of("d4", "d5", "c4", "xc4"));
    }

    @Override
    public Board getBoard(long id) {
        Board cached = getCachedBoard(id);
        if (cached != null)
            return cached;

        return buildBoard(id);
    }

    @Override
    public Board getCachedBoard(long id) {
        return null;
    }

    @Override
    public Board buildBoard(long id) {
        List<String> moves = database.get(id);

        List<Command> commands = moves.stream().map(Command::new).toList();



        return null;
    }

    @Override
    public boolean addMove(String move) {
        return false;
    }
}
