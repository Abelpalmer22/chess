package dataaccess;
import java.util.List;
import model.GameData;

public interface GameDAO {
    GameData createGame(GameData game);
    GameData getGame(int gameID);
    void updateGame(GameData game);
    int createNewGameID();
    void clear();

    List<GameData> listAllGames();
}
