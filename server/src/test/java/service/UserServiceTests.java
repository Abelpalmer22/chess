package service;

import Requests.LoginRequest;
import Requests.LogoutRequest;
import Requests.RegisterRequest;
import Results.LoginResult;
import Results.RegisterResult;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import org.eclipse.jetty.util.log.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;

public class UserServiceTests {
    MemoryUserDAO userDAO = new MemoryUserDAO();
    MemoryAuthDAO authDAO = new MemoryAuthDAO();
    MemoryGameDAO gameDAO = new MemoryGameDAO();
    UserService userService = new UserService(gameDAO, authDAO, userDAO);

    @Test
    void registerPositive() throws DataAccessException {
        RegisterResult re = userService.register(new RegisterRequest("usernameabel", "passwordpalmer", "abelpalmer22"));
        Assertions.assertEquals("usernameabel", re.username());
        Assertions.assertNotNull(re.authToken());
        Assertions.assertNotNull(userDAO.getUser("usernameabel"));
        Assertions.assertDoesNotThrow(() -> authDAO.getAuth(re.authToken()));
    }

    @Test
    void registerNegative() throws DataAccessException {
        RegisterRequest req = new RegisterRequest("ichhassedas", "passwort", "email");
        RegisterResult re = userService.register(req);
        Assertions.assertThrows(DataAccessException.class, () -> userService.register(req)); // try to register again
    }

    @Test
    void loginPositive() throws DataAccessException {
        RegisterRequest req = new RegisterRequest("runningoutofusernames", "somepassword", "email");
        RegisterResult re = userService.register(req);
        LoginRequest logreq = new LoginRequest("runningoutofusernames", "somepassword");
        LoginResult logre = userService.login(logreq);
        Assertions.assertEquals("runningoutofusernames", re.username());
        Assertions.assertNotNull(re.authToken());
        Assertions.assertDoesNotThrow(() -> authDAO.getAuth(re.authToken()));
    }

    @Test
    void loginNegative() throws DataAccessException {
        LoginRequest req = new LoginRequest("runningoutofusernames", "somepassword");
        Assertions.assertThrows(DataAccessException.class, () -> userService.login(req));
    }

    @Test
    void logoutPositive() throws DataAccessException {
        RegisterRequest req = new RegisterRequest("runningoutofusernames", "somepassword", "email");
        RegisterResult re = userService.register(req);
        LogoutRequest logoutRequest = new LogoutRequest(re.authToken());
        Assertions.assertDoesNotThrow(() -> userService.logout(logoutRequest));
    }

    @Test
    void logoutNegative() throws DataAccessException {
        LogoutRequest nullLogoutRequest = new LogoutRequest(null);
        LogoutRequest logoutRequest = new LogoutRequest("someauthtoken");
        Assertions.assertThrows(DataAccessException.class, () -> userService.logout(nullLogoutRequest));
        Assertions.assertThrows(DataAccessException.class, () -> userService.logout(logoutRequest));
    }
}
