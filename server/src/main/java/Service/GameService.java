package Service;

import Requests.CreateGameRequest;
import Results.CreateGameResult;
import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.GameData;
import dataaccess.GameDAO;

import javax.xml.crypto.Data;
import java.util.*;

public class GameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final UserDAO userDAO;

    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public CreateGameResult createGame(CreateGameRequest req) throws DataAccessException {
        if (req.playerUsername() == null) {
            throw new DataAccessException("Ersteller existiert nicht");
        }
        ChessGame newChessGame = new ChessGame();
        GameData newGame = new GameData(gameDAO.createNewGameID(), newGame.whiteUsername(), newGame.blackUsername(), req.playerUsername(), newChessGame);
        gameDAO.createGame(newGame);
        return new CreateGameResult(newGame.gameID());
    }
}
