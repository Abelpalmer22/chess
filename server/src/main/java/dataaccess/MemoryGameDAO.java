package dataaccess;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> gameData = new HashMap<>();
    private int gameID = 1;

    public GameData createGame(GameData game) throws DataAccessException {
        if (gameData.get(game.gameID()) != null) throw new DataAccessException("game already exists");
        gameData.put(game.gameID(), game);
        return game;
    }

    public GameData getGame(int gameID) throws DataAccessException {
        if (gameData.get(gameID) == null) throw new DataAccessException("bad request");
        return gameData.get(gameID);
    }

    public void updateGame(GameData game) throws DataAccessException {
        if (!gameData.containsKey(game.gameID())) throw new DataAccessException("Game not found");
        gameData.put(game.gameID(), game);
    }

    public Collection<GameData> listGames() {
        return gameData.values();
    }

    public int makeNewID() {
        return gameID++;
    }

    public void clear() {
        gameData.clear();
    }
}
