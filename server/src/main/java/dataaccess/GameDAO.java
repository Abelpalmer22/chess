package dataaccess;

import model.GameData;

public interface GameDAO {
    void createGame(GameData game);
    GameData getGame(int gameID);
    void updateGame(GameData game);
    void clear();
}
