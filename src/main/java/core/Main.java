package core;

import database.DatabaseManager;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static DatabaseManager dbManager = new DatabaseManager();

    public static void main(String[] args) {
        // Lädt alle JSONs beim Start (nur einmal)
        dbManager.loadJsonData();

        // Daten abrufen
        System.out.println("Teams: " + dbManager.getAllTeams().size());
        System.out.println("Roles: " + dbManager.getAllRoles().size());
        System.out.println("Skills: " + dbManager.getAllSkills().size());

        // Daten persistieren über Neustarts hinweg ✅
    }
}

