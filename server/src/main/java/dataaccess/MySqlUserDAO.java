package dataaccess;

import model.UserData;
import org.eclipse.jetty.server.Authentication;

import javax.xml.crypto.Data;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

public class MySqlUserDAO implements UserDAO {
    public static void createTable() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection(); var stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS user (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(255) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL)
            """);
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create user table", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection(); var stmt = conn.prepareStatement("TRUNCATE TABLE user")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to clear user table", e);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

        System.out.println(">>> MySqlUserDAO.createUser() called for " + user.username());

        var sql = "INSERT INTO user (username, password_hash, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());

            stmt.setString(1, user.username());
            stmt.setString(2, hashedPassword);
            stmt.setString(3, user.email());
            stmt.executeUpdate();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DataAccessException("already taken", e);
            }
            throw new DataAccessException("Unable to insert user", e);
        }
    }

    public UserData getUser(String username) throws DataAccessException {
        var sql = "SELECT username, password_hash, email FROM user WHERE username=?";
        try (var conn = DatabaseManager.getConnection(); var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UserData(rs.getString("username"), rs.getString("password_hash"), rs.getString("email"));
                }
                throw new DataAccessException("User not found");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to read user", e);
        }
    }
}


