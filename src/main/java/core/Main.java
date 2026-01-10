package core;

import database.DatabaseManager;
import gui.UIController;
import model.*;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import model.Role;

public class Main {
    private static DatabaseManager dbManager;
    private static UIController uiController;

    public static void main(String[] args) throws SQLException, IOException {
        dbManager = DatabaseManager.getInstance();
        uiController = UIController.getInstance();

        dbManager.importFromJson(Path.of("src/main/resources/json"));
        try {
            dbManager.loadDataFromDb();

        } catch (Exception e) {
            System.out.println(" ~ ERROR: " + e);
            e.printStackTrace();
        }
        dbManager.close();

        // This is for returning all employees with username and password (so its easier to see who is who
        System.out.println("Employees:");
        for (Employee employee : ServiceLocator.getEmployeeContainer().getEmployees()) {
            System.out.println(" - " + employee.getRoleManager().getActiveRole().getSystemPermission() + " | " + employee.getUsername() + " | " + employee.getPassword());
        }

        uiController.startApplication();
    }

}

