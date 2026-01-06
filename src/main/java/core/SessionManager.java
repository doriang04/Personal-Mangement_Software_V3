package core;

import model.Employee;

public class SessionManager {

    private static SessionManager instance;

    private Employee loggedInUser;

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    private SessionManager() {
        loggedInUser = null;
    }

    public String getUserPermission() {
        if (isSessionActive()) return loggedInUser.getPermissionString();
        return null;
    }

    public String getUserFirstNameAndLastName() {
        if (isSessionActive()) return loggedInUser.getFirstName() + loggedInUser.getLastName();
        return null;
    }

    public boolean isSessionActive() {
        return loggedInUser != null;
    }
}
