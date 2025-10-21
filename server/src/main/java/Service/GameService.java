package Service;
import dataaccess.GameDAO;
import dataaccess.MemoryGameDAO;
import model.GameData;
import model.AuthData;
import chess.ChessGame;
import java.util.Map;

public class GameService {
    private final GameDAO gameDAO = new MemoryGameDAO();

    public GameData createGame(Map<String, Object> gameInfo, AuthData auth) {
        String creator = auth.username();
        String opponent = (String) gameInfo.get("opponent");
        String gameName = (String) gameInfo.get("gameName");
        GameData newGame = new GameData(0, creator, opponent, gameName, new ChessGame());
        gameDAO.createGame(newGame);
        return newGame;
    }

    public GameData joinGame(int gameID, AuthData auth) {
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
