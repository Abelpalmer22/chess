package dataaccess;
import model.GameData;
import java.util.*;

public class MemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextID = 1;

    public void createGame(GameData game) {
        GameData withID = new GameData(
                nextID++,
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                game.game()
        );
        games.put(withID.gameID(), withID);
    }


    public GameData getGame(int gameID) {
        return games.get(gameID);
    }


    public void updateGame(GameData game) {
        games.put(game.gameID(), game);
    }

    public void clear() {
        games.clear();
        nextID = 1;
    }
}
