package service;

import Requests.LoginRequest;
import Requests.LogoutRequest;
import Requests.RegisterRequest;
import Results.LoginResult;
import Results.RegisterResult;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;

public class UserService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private final UserDAO userDAO;

    public UserService(GameDAO gameDAO, AuthDAO authDAO, UserDAO userDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        this.userDAO = userDAO;
    }

    public RegisterResult register(RegisterRequest registerRequest)  throws DataAccessException {
        if (registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null) {
            throw new DataAccessException("bad request");
        }
        try {
            UserData user = userDAO.getUser(registerRequest.username());
        } catch (DataAccessException e) {
            userDAO.createUser(new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email()));
            AuthData newAuth = authDAO.createAuth(registerRequest.username());
            return new RegisterResult(registerRequest.username(), newAuth.authToken());
        }
        throw new DataAccessException("already taken");
    }

    public LoginResult login(LoginRequest req) throws DataAccessException {
        if (req.username() == null || req.password() == null) throw new DataAccessException("bad request");
        try {
            UserData user = userDAO.getUser(req.username());
            if (!user.password().equals(req.password())) throw new DataAccessException("unauthorized");
        } catch (DataAccessException e) {
            throw new DataAccessException("unauthorized");
        }
        AuthData newAuth = authDAO.createAuth(req.username());

        return new LoginResult(req.username(), newAuth.authToken());
    }

    public void logout(LogoutRequest req) throws DataAccessException {
        if (req.authToken() == null) throw new DataAccessException("unauthorized");
        AuthData auth = authDAO.getAuth(req.authToken());
        if (auth == null) throw new DataAccessException("unauthorized");
        authDAO.deleteAuth(req.authToken());
    }

}