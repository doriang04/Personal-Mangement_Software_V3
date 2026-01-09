package core;

import model.Employee;
import model.ServiceLocator;

import javax.naming.AuthenticationException;

public class SessionManager {

    private static SessionManager instance;

    private Employee loggedInUser;
    private boolean maintenanceModeActive;

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    private SessionManager() {
        loggedInUser = null;
        maintenanceModeActive = false; // TODO hier eine persistente speichermethode verwenden um es über programm restart hinaus drin zu lassen
    }

    public String getUserPermission() {
        if (isSessionActive()) return loggedInUser.getRole().getActiveRole().getPermissionString();
        return null;
    }

    public String getUserFirstNameAndLastName() {
        if (isSessionActive()) return loggedInUser.getFirstName() + loggedInUser.getLastName();
        return null;
    }

    public boolean isSessionActive() {
        return loggedInUser != null;
    }

    public void logout() {
        loggedInUser = null;
    }

    public boolean isMaintenanceModeActive() {
        return maintenanceModeActive;
    }

    public void login(String username, String password) throws AuthenticationException {
        AuthenticationException error_to_throw = new AuthenticationException("Username or Password do not match. Please provide correct credentials.");
        for (Employee employee: ServiceLocator.getEmployeeManagerContainer().getEmployees()) {
            if (employee.getUsername().equals(username)) {
                if (employee.getPassword().equals(password)) {
                    loggedInUser = employee;
                    break;
                }
                throw error_to_throw;
            }
        }
        throw error_to_throw;
    }

}
