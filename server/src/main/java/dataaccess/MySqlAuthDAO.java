package dataaccess;

import model.AuthData;
import java.sql.SQLException;
import java.util.UUID;

public class MySqlAuthDAO implements AuthDAO {
    public static void createTable() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS auth (
                    token VARCHAR(255) PRIMARY KEY,
                    username VARCHAR(255) NOT NULL
                )
            """);
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create auth table", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement("DELETE FROM auth")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to clear auth table", e);
        }
    }

    @Override
    public AuthData createAuthentication(String username) throws DataAccessException {
        // generate a random token
        String token = UUID.randomUUID().toString();

        var sql = "INSERT INTO auth (token, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.setString(2, username);
            stmt.executeUpdate();
            return new AuthData(token, username);
        } catch (SQLException e) {
            throw new DataAccessException("Unable to insert auth token", e);
        }
    }

    @Override
    public AuthData getAuthentication(String authToken) throws DataAccessException {
        var sql = "SELECT token, username FROM auth WHERE token=?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AuthData(rs.getString("token"), rs.getString("username"));
                } else {
                    throw new DataAccessException("Auth token not found");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to read auth token", e);
        }
    }

    @Override
    public void deleteAuthentication(String authToken) throws DataAccessException {
        var sql = "DELETE FROM auth WHERE token=?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new DataAccessException("Auth token not found");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to delete auth token", e);
        }
    }
}
