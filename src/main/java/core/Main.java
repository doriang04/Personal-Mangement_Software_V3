package core;

import database.DatabaseManager;
import gui.UIController;
import model.Employee;

import java.nio.file.Path;

public class Main {
    private static DatabaseManager dbManager;
    private static UIController uiController;

    public static void main(String[] args) {
        System.out.println("🚀 Personal Management System - Startup");

        dbManager = DatabaseManager.getInstance();
        uiController = UIController.getInstance();

        try {
            // 1. Versuch: Bestehende Daten aus der Datenbank laden
            dbManager.loadDataFromDb();

            // 2. Prüfung: Haben wir Daten? (Wir prüfen exemplarisch auf Mitarbeiter)
            if (ServiceLocator.getEmployeeContainer().getEmployees().isEmpty()) {
                System.out.println("⚠️ Datenbank scheint leer zu sein. Starte JSON-Import...");

                // Import aus JSON durchführen (schreibt in die H2 DB)
                dbManager.importFromJson(Path.of("src/main/resources/json"));

                // WICHTIG: Nach dem Import müssen wir die Daten neu in den RAM laden!
                dbManager.loadDataFromDb();

                System.out.println("✅ JSON-Import erfolgreich & Daten geladen.");
            } else {
                System.out.println("✅ Vorhandene Daten aus Datenbank geladen. Kein Import nötig.");
            }

        } catch (Exception e) {
            System.out.println(" ~ ERROR während des Startvorgangs: " + e);
            e.printStackTrace();
        }

        // Debug Ausgabe der Mitarbeiter
        System.out.println("Employees:");
        for (Employee employee : ServiceLocator.getEmployeeContainer().getEmployees()) {
            String roleName = (employee.getRoleManager().getActiveRole() != null)
                    ? employee.getRoleManager().getActiveRole().getSystemPermission()
                    : "NONE";
            System.out.println(" - " + roleName + " | " + employee.getUsername() + " | " + employee.getPassword());
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("⚠️ ShutdownHook (Cmd+Q / OS Quit)");

            try {
                database.DatabaseManager.getInstance().saveAllDataOnce();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        // GUI starten
        uiController.startApplication();
    }
}