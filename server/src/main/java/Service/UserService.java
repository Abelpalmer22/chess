package Service;

import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import io.javalin.http.UnauthorizedResponse;
import model.AuthData;
import model.UserData;
import Requests.RegisterRequest;
import Requests.LoginRequest;
import Requests.LogoutRequest;
import Results.RegisterResult;
import Results.LoginResult;

import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
        if (userDAO.getUser(registerRequest.username()) != null) {
            throw new IllegalStateException("Username already taken");
        }
        UserData user = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
        authDAO.createAuth(user.username());
        String authToken = authDAO.createAuth(user.username());
        return new RegisterResult(user.username(), authToken);
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        UserData user = userDAO.getUser(loginRequest.username());
        if (user == null) throw new DataAccessException("Benutzer existiert nicht");
        if (!user.password().equals(loginRequest.password())) {throw new DataAccessException("Falsches Passwort");}
        String authToken = authDAO.createAuth(user.username());
        return new LoginResult(user.username(), authToken);
    }



    public void logout(LogoutRequest logoutRequest) {
        authDAO.deleteAuth(logoutRequest.authToken());
    }
}
