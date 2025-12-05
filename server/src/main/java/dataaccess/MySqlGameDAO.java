package dataaccess;

import com.google.gson.Gson;
import model.GameData;
import chess.ChessGame;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class MySqlGameDAO implements GameDAO {
    private static final Gson GSON = new Gson();

    public static void createTable() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {

            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS game (
                    id INT PRIMARY KEY,
                    whiteUsername VARCHAR(255),
                    blackUsername VARCHAR(255),
                    gameName VARCHAR(255),
                    game TEXT,
                    gameOver BOOLEAN NOT NULL DEFAULT FALSE
                )
            """);

        } catch (SQLException e) {
            throw new DataAccessException("Unable to create game table", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement("TRUNCATE TABLE game")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to clear game table", e);
        }
    }

    @Override
    public GameData createGame(GameData game) throws DataAccessException {
        var sql = "INSERT INTO game (id, whiteUsername, blackUsername, gameName, game, gameOver) VALUES (?, ?, ?, ?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, game.gameID());
            stmt.setString(2, game.whiteUsername());
            stmt.setString(3, game.blackUsername());
            stmt.setString(4, game.gameName());
            stmt.setString(5, GSON.toJson(game.game()));
            stmt.setBoolean(6, game.gameOver());   // NEW FIELD

            stmt.executeUpdate();

            return game;

        } catch (SQLException e) {
            throw new DataAccessException("Unable to insert game", e);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var sql = "SELECT id, whiteUsername, blackUsername, gameName, game, gameOver FROM game WHERE id=?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, gameID);

            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    var white = rs.getString("whiteUsername");
                    var black = rs.getString("blackUsername");
                    var name = rs.getString("gameName");
                    var json = rs.getString("game");
                    var chessGame = GSON.fromJson(json, ChessGame.class);
                    var gameOver = rs.getBoolean("gameOver");   // NEW FIELD

                    return new GameData(gameID, white, black, name, chessGame, gameOver);

                } else {
                    throw new DataAccessException("bad request");
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Unable to read game", e);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        var sql = """
                UPDATE game 
                SET whiteUsername=?, blackUsername=?, gameName=?, game=?, gameOver=? 
                WHERE id=?
                """;

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, game.gameName());
            stmt.setString(4, GSON.toJson(game.game()));
            stmt.setBoolean(5, game.gameOver());   // NEW FIELD
            stmt.setInt(6, game.gameID());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Game not found");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Unable to update game", e);
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        var sql = "SELECT id, whiteUsername, blackUsername, gameName, game, gameOver FROM game";
        var games = new ArrayList<GameData>();

        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql);
             var rs = stmt.executeQuery()) {

            while (rs.next()) {
                var id = rs.getInt("id");
                var white = rs.getString("whiteUsername");
                var black = rs.getString("blackUsername");
                var name = rs.getString("gameName");
                var chess = GSON.fromJson(rs.getString("game"), ChessGame.class);
                var gameOver = rs.getBoolean("gameOver");

                games.add(new GameData(id, white, black, name, chess, gameOver));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Unable to list games", e);
        }
        return games;
    }

    @Override
    public int makeNewID() throws DataAccessException {
        var sql = "SELECT MAX(id) AS max_id FROM game";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql);
             var rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("max_id") + 1;
            } else {
                return 1;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Unable to get next game ID", e);
        }
    }
}
