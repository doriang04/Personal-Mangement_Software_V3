package core;

import database.DatabaseManager;
import gui.UIController;
import model.Employee;

import java.nio.file.Path;

public class Main {

    private static DatabaseManager dbManager;
    private static UIController uiController;

    public static void main(String[] args) {

        dbManager = DatabaseManager.getInstance();
        uiController = UIController.getInstance();

        try {
            dbManager.loadDataFromDb();

            if (ServiceLocator.getEmployeeContainer().getEmployees().isEmpty()) {
                dbManager.importFromJson(Path.of("src/main/resources/json"));
                dbManager.loadDataFromDb();
            }

        } catch (Exception e) {
            System.out.println("Fehler beim Starten/Initialisieren des Programmes: \n" + e);
            e.printStackTrace();
        }

        System.out.println("Mitarbeiter:");
        for (Employee employee : ServiceLocator.getEmployeeContainer().getEmployees()) {
            String roleName = employee.getRoleManager().getActiveRole() != null
                    ? employee.getRoleManager().getActiveRole().getSystemPermission()
                    : "NONE";

            System.out.println(" - " + roleName + " | " + employee.getUsername() + " | " + employee.getPassword());
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            try {
                dbManager.saveAllDataOnce();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        uiController.startApplication();
    }
}