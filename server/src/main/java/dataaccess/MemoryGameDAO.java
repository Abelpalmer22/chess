package dataaccess;

import dataaccess.GameDAO;
import model.GameData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextID = 1;

    @Override
    public GameData createGame(GameData game) {
        int newID = nextID++;
        GameData gameToAdd = new GameData(newID, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        games.put(newID, gameToAdd);
        return gameToAdd;
    }

    public int createNewGameID() {return nextID++;}

    @Override
    public void updateGame(GameData game) {
        games.put(game.gameID(), game);
    }


    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public void clear() {
        games.clear();
        nextID = 1;
    }

    @Override
    public List<GameData> listAllGames() {
        return new ArrayList<>(games.values());
    }
}

