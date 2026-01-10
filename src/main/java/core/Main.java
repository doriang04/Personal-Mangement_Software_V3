package core;

import database.DatabaseManager;
import gui.UIController;
import model.ServiceLocator;
import model.Employee;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

public class Main {
    private static DatabaseManager dbManager;
    private static UIController uiController;

    public static void main(String[] args) throws SQLException, IOException {
        dbManager = DatabaseManager.getInstance();
        uiController = UIController.getInstance();

        System.out.println("🚀 Personal Management System - Startup");

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

        // Shutdown Hook: Speichert Daten beim Beenden
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n🛑 Programm wird beendet...");
            dbManager.saveAllData();
            dbManager.close();
            System.out.println("👋 Bye Bye!");
        }));

        // GUI starten
        uiController.startApplication();
    }
}