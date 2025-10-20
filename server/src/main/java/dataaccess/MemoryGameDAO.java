package dataaccess;
import model.GameData;
import java.util.*;

public class MemoryGameDAO {
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextID = 1;

    public void createGame(GameData game) {
        game.setGameID(nextID++);
        games.put(game.getGameID(), game);
    }

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public void updateGame(GameData game) {
        games.put(game.getGameID(), game);
    }

    public void clear() {
        games.clear();
        nextID = 1;
    }
}
