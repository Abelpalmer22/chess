package dataaccess;

import model.AuthData;

public interface AuthDAO {
    /** Create and store a new auth token for this username; return the stored AuthData. */
    AuthData createAuthentication(String username) throws DataAccessException;

    /** Look up an auth token; return the AuthData (username + token) if it exists. */
    AuthData getAuthentication(String authToken) throws DataAccessException;

    /** Delete/Invalidate this auth token. */
    void deleteAuthentication(String authToken) throws DataAccessException;

    void clear();
}
