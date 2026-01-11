package core;

import model.Employee;

import javax.naming.AuthenticationException;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class SessionManager {

    private static SessionManager instance;

    private Employee loggedInUser;
    private boolean maintenanceModeActive;

    private static final String PROPERTIES_DIR = "src/main/resources/db";
    private static final String FILE_NAME = "system.properties";
    private static final String MAINTENANCE_KEY = "maintenanceMode";

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    private SessionManager() {
        loggedInUser = null;
        loadMaintenanceMode();
    }

    public boolean isMaintenanceModeActive() {
        return maintenanceModeActive;
    }

    public void setMaintenanceModeActive(boolean active) {
        this.maintenanceModeActive = active;
        saveMaintenanceMode();
    }

    private void loadMaintenanceMode() {
        Properties props = new Properties();
        Path path = Paths.get(PROPERTIES_DIR, FILE_NAME);

        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                props.load(in);
                maintenanceModeActive = Boolean.parseBoolean(
                        props.getProperty(MAINTENANCE_KEY, "false")
                );
            } catch (IOException e) {
                maintenanceModeActive = false;
            }
        } else {
            maintenanceModeActive = false;
            saveMaintenanceMode();
        }
    }

    private void saveMaintenanceMode() {
        Properties props = new Properties();
        props.setProperty(MAINTENANCE_KEY, String.valueOf(maintenanceModeActive));

        try {
            Files.createDirectories(Paths.get(PROPERTIES_DIR));
            try (OutputStream out = Files.newOutputStream(Paths.get(PROPERTIES_DIR, FILE_NAME))) {
                props.store(out, "System configuration");
            }
        } catch (IOException ignored) {
        }
    }

    public String getUserPermission() {
        if (isSessionActive())
            return loggedInUser.getRoleManager().getActiveRole().getSystemPermission();
        return null;
    }

    public String getUserFirstNameAndLastName() {
        if (isSessionActive())
            return loggedInUser.getFirstName() + " " + loggedInUser.getLastName();
        return null;
    }

    public boolean isSessionActive() {
        return loggedInUser != null;
    }

    public void logout() {
        loggedInUser = null;
    }
    public Employee getCurrentUser() {
        return loggedInUser;
    }

    public void login(String username, String password) throws AuthenticationException {
        AuthenticationException error_normal =
                new AuthenticationException("Username or Password do not match. Please provide correct credentials.");
        AuthenticationException error_maintenance =
                new AuthenticationException("Maintenance mode is active. Login only possible for admins.");

        for (Employee employee : ServiceLocator.getEmployeeContainer().getEmployees()) {
            if (employee.getUsername().equals(username)) {
                if (employee.getPassword().equals(password)) {
                    if (isMaintenanceModeActive()) {
                         if (employee.getRoleManager().getActiveRole().getSystemPermission().equals("ADMIN")) {
                             loggedInUser = employee;
                             return;
                         }
                         throw error_maintenance;
                    }
                    loggedInUser = employee;
                    return;
                }
                throw error_normal;
            }
        }
        throw error_normal;
    }
}
