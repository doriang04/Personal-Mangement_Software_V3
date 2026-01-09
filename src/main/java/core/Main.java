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
        dbManager = new DatabaseManager(); // TODO change this to be singleton?
        uiController = UIController.getInstance();

        // ------------------------------------- div line --------------------------------------------------

        // TODO kann man den code below (db initializing code) in eine funktion packen?
        System.out.println("🚀 Personal Management System - Datenbank Loader");

        // 1. JSON → Database laden (einmalig)
        try {
        } catch (Exception e) {
            System.out.println(" ~ ERROR: " + e);
        }
        dbManager.importFromJson(Path.of("src/main/resources/json"));
        dbManager.close();
        System.out.println("✅ System bereit - Alle Java Klassen geladen!");

        // ------------------------------------- div line --------------------------------------------------

        // ===================== Employees =====================
        System.out.println("Employees:");
        for (Employee employee : ServiceLocator.getEmployeeContainer().getEmployees()) {
            System.out.println(" - " + employee.getUsername() + " | " + employee.getPassword());
        }

        // ===================== Roles =====================
        System.out.println("\nRoles:");
        for (Role role : ServiceLocator.getRoleContainer().getRoles()) {
            System.out.println(" - " + role.getId() + " | " + role.getName());
        }

        // ===================== Role Managers =====================
        System.out.println("\nRole Managers:");
        for (RoleManager rm : ServiceLocator.getRoleManagerContainer().getRoleManagers()) {
            System.out.println(" - EmployeeId: " + rm.getEmployeeId());
        }

        // ===================== Teams =====================
        System.out.println("\nTeams:");
        for (Team team : ServiceLocator.getTeamContainer().getTeams()) {
            System.out.println(" - " + team.getId() + " | " + team.getName());
        }

        // ===================== Departments =====================
        System.out.println("\nDepartments:");
        for (Department dept : ServiceLocator.getDepartmentContainer().getDepartments()) {
            System.out.println(" - " + dept.getId() + " | " + dept.getName());
        }

        // ===================== Companies =====================
        System.out.println("\nCompanies:");
        for (Company company : ServiceLocator.getCompanyContainer().getCompanies()) {
            System.out.println(" - " + company.getId() + " | " + company.getName());
        }

        // ===================== Skills =====================
        System.out.println("\nSkills:");
        for (Skill skill : ServiceLocator.getSkillContainer().getSkills()) {
            System.out.println(" - " + skill.getId() + " | " + skill.getName());
        }

        // ===================== Skill Managers =====================
        System.out.println("\nSkill Managers:");
        for (SkillManager sm : ServiceLocator.getSkillManagerContainer().getSkillManagers()) {
            System.out.println(" - EmployeeId: " + sm.getEmployeeId());
        }

        // ===================== Trainings =====================
        System.out.println("\nTrainings:");
        for (Training training : ServiceLocator.getTrainingContainer().getTrainings()) {
            System.out.println(" - " + training.getId() + " | " + training.getTitle());
        }

        // ===================== Training Managers =====================
        System.out.println("\nTraining Managers:");
        for (TrainingManager tm : ServiceLocator.getTrainingManagerContainer().getTrainingManagers()) {
            System.out.println(" - EmployeeId: " + tm.getEmployeeId());
        }

        // ===================== Session =====================
        System.out.println("\nSession:");
        System.out.println(" - Current user: "
                + (ServiceLocator.getSessionManager().getUserFirstNameAndLastName() != null
                ? ServiceLocator.getSessionManager().getUserFirstNameAndLastName()
                : "none"));
    }

}

