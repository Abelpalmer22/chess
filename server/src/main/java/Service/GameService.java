package Service;
import dataaccess.GameDAO;
import dataaccess.MemoryGameDAO;
import model.GameData;
import model.AuthData;
import java.util.Map;

public class GameService {
    private final GameDAO gameDAO = new MemoryGameDAO();

    public GameData createGame(Map<String, Object> gameInfo, AuthData auth) {
        String creator = auth.username();
        String opponent = (String) gameInfo.get("opponent");
        GameData newGame = new GameData();
        gameDAO.createGame(newGame);
        return newGame;
    }

    public GameData joinGame(int gameID, AuthData auth) {
        String username = auth.username();
        GameData oldGame = gameDAO.getGame(gameID);
        if (oldGame == null) {throw new RuntimeException("No such game");}
        GameData updated = new GameData(
                oldGame.gameID(),
                oldGame.whiteUsername(),
                auth.username(),
                oldGame.gameName(),
                oldGame.game()
        );
        gameDAO.updateGame(updated);
        return updated;
    }

    public void clearGames() {gameDAO.clear();}
}
