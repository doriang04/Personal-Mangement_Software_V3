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
        if (isSessionActive()) return loggedInUser.getRoleManager().getActiveRole().getSystemPermission();
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

        System.out.println("\n--- login attempt ---");

        for (Employee employee: ServiceLocator.getEmployeeContainer().getEmployees()) {
            // Debugging Output
            // System.out.println("checking: " + employee.getUsername());

            if (employee.getUsername().equals(username)) {
                if (employee.getPassword().equals(password)) {
                    // 1. User setzen
                    loggedInUser = employee;
                    System.out.println("✅ Login success for: " + username);

                    // 2. WICHTIG: Methode hier verlassen!
                    // Nicht 'break', sonst läuft er unten in den Fehler rein.
                    return;
                }

                // Passwort falsch -> Exception werfen
                System.out.println(" -> password incorrect");
                throw error_to_throw;
            }
        }

        // Wenn der Loop durchläuft ohne return, wurde der Username nicht gefunden
        System.out.println(" -> user not found in database");
        throw error_to_throw;
    }

}
