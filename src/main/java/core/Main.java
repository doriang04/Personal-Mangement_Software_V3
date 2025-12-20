package core;

import database.DatabaseManager;
import model.*;

import java.util.List;
import model.Role;

public class Main {
    private static DatabaseManager dbManager = new DatabaseManager();

    public static void main(String[] args) {
        System.out.println("🚀 Personal Management System - Datenbank Loader");

        // 1. JSON → Database laden (einmalig)
        dbManager.loadJsonData();

        // 2. Alle Daten als Java Klassen laden
        Company company = loadCompany();
        List<Department> departments = loadDepartments();
        List<Team> teams = loadTeams();
        List<Role> roles = loadRoles();
        List<Skill> skills = loadSkills();
        List<Employee> employees = loadEmployees();

        // 3. Daten anzeigen + Statistiken
        displayStatistics(company, departments, teams, roles, skills, employees);

        // 4. Beispiel: Spezifische Abfragen
        displayExampleQueries(employees);

        dbManager.close();
        System.out.println("✅ System bereit - Alle Java Klassen geladen!");
    }

    private static Company loadCompany() {
        Company company = new Company(1, "Bauunternehmen XYZ GmbH");
        System.out.println("🏢 Company: " + company.getName());
        return company;
    }

    private static List<Department> loadDepartments() {
        List<Department> departments = dbManager.getAllDepartments();
        System.out.println("🏛️  Departments: " + departments.size());
        departments.forEach(d ->
                System.out.println("  → " + d.getDepartmentId() + ": " + d.getDepartmentName())
        );
        return departments;
    }

    private static List<Team> loadTeams() {
        List<Team> teams = dbManager.getAllTeams();
        System.out.println("👥 Teams: " + teams.size());
        teams.subList(0, Math.min(5, teams.size())).forEach(team ->
                System.out.println("  → Team " + team.getTeamId() + ": " + team.getTeamName())
        );
        return teams;
    }

    private static List<Role> loadRoles() {
        List<Role> roles = dbManager.getAllRoles();
        System.out.println("🎭 Roles: " + roles.size());
        roles.subList(0, Math.min(5, roles.size())).forEach(role ->
                System.out.println("  → Role " + role.getId() + ": " + role.getName())
        );
        return roles;
    }

    private static List<Skill> loadSkills() {
        List<Skill> skills = dbManager.getAllSkills();
        System.out.println("🎓 Skills: " + skills.size());
        skills.subList(0, Math.min(5, skills.size())).forEach(skill ->
                System.out.println("  → Skill " + skill.getSkillId() + ": " + skill.getDescription() +
                        " (" + skill.getRequiredYears() + " Jahre)")
        );
        return skills;
    }

    private static List<Employee> loadEmployees() {
        List<Employee> employees = dbManager.getAllEmployees();
        System.out.println("👷 Employees: " + employees.size());
        employees.subList(0, Math.min(5, employees.size())).forEach(emp ->
                System.out.println("  → " + emp.getFirstName() + " " + emp.getLastName() +
                        " (Role: " + emp.getRole().getRolemanagerid() + ", Team: " + emp.getTeamId() + ")")
        );
        return employees;
    }

    private static void displayStatistics(Company company, List<Department> departments,
                                          List<Team> teams, List<Role> roles,
                                          List<Skill> skills, List<Employee> employees) {
        System.out.println("\n📊 STATISTIKEN:");
        System.out.println("🏢 Unternehmen: " + company.getName());
        System.out.println("🏛️  Abteilungen: " + departments.size());
        System.out.println("👥 Teams: " + teams.size());
        System.out.println("🎭 Rollen: " + roles.size());
        System.out.println("🎓 Skills: " + skills.size());
        System.out.println("👷 Mitarbeiter: " + employees.size());

        // Team-Belegung
        long teamsWithEmployees = employees.stream()
                .map(Employee::getTeamId)
                .distinct().count();
        System.out.println("📈 Teams mit Mitarbeitern: " + teamsWithEmployees + "/" + teams.size());
    }

    private static void displayExampleQueries(List<Employee> employees) {
        System.out.println("\n🔍 BEISPIEL-ABFRAGEN:");

        // Bauleiter finden
        long bauleiter = employees.stream()
                .filter(emp -> emp.getRole().getRolemanagerid() == 2)
                .count();
        System.out.println("👷 Bauleiter: " + bauleiter);

        // IT Mitarbeiter (Team 21-24)
        long itEmployees = employees.stream()
                .filter(emp -> emp.getTeamId() >= 21 && emp.getTeamId() <= 24)
                .count();
        System.out.println("💻 IT Mitarbeiter: " + itEmployees);

        // Mitarbeiter mit >3 Skills
        long skilledEmployees = employees.stream()
                .filter(emp -> emp.getSkill().getAktiveSkillHistory().size() > 3)
                .count();
        System.out.println("⭐ Hochqualifizierte (>3 Skills): " + skilledEmployees);

        // Offene Trainings
        long openTrainings = employees.stream()
                .mapToLong(emp -> emp.getTraining().getOpenTrainings().size())
                .sum();
        System.out.println("📚 Offene Trainings: " + openTrainings);
    }
}

