package database;

import model.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import org.h2.tools.RunScript;

// TODO check this class if it works as intended (gpt code cannot be trusted by itself lol

/**
 * DatabaseManager: initialisiert DB-Schema, schreibt Daten aus ServiceLocator in die DB
 * und bietet (Skelett-)Methoden zum Einlesen kompletter Daten aus JSON-Dateien.
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:h2:~/personalmanagement;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private Connection connection;

    public DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            initDatabase();
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
        }
    }

    /**
     * Lädt das Schema aus der externen .sql Datei
     */
    private void initDatabase() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("db/schema.sql");
            if (is == null) {
                throw new RuntimeException("Schema file not found: db/schema.sql");
            }
            // H2 RunScript führt das gesamte SQL File aus
            RunScript.execute(connection, new InputStreamReader(is));
            System.out.println("✅ Database schema initialized from schema.sql");
        } catch (Exception e) {
            System.err.println("❌ Schema init failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hauptmethode, um alle Daten aus dem ServiceLocator in die DB zu schreiben.
     * Die Operation läuft innerhalb einer DB-Transaktion; bei Fehlern wird zurückgerollt.
     */
    public void syncWithServiceLocator() {
        System.out.println("🔄 Syncing ServiceLocator data to database...");
        boolean previousAutoCommit = true;
        try {
            previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false); // Start transaction

            // Reihenfolge wichtig wg. FK-Constraints
            saveCompanies(ServiceLocator.getCompanyContainer().getCompanies());
            saveDepartments(ServiceLocator.getDepartmentContainer().getDepartments());
            saveTeams(ServiceLocator.getTeamContainer().getTeams());
            saveRoles(ServiceLocator.getRoleContainer().getRoles());
            saveSkills(ServiceLocator.getSkillContainer().getSkills());
            // Mitarbeiter hängen oft an Teams, daher erst nach Teams speichern
            saveEmployees(ServiceLocator.getEmployeeContainer().getEmployees());
            saveTrainings(ServiceLocator.getTrainingContainer().getTrainings());

            // Hinweis / TODO:
            // Falls euer Modell History- oder Join-Entitäten enthält (role_history, skill_history,
            // training_history, training_skills), dann hier die entsprechenden saveX(...) Methoden
            // aufrufen. Beispiel (falls vorhanden):
            // saveRoleHistory(ServiceLocator.getRoleHistoryContainer().getRoleHistories());
            // saveSkillHistory(ServiceLocator.getSkillHistoryContainer().getSkillHistories());
            // saveTrainingHistory(ServiceLocator.getTrainingHistoryContainer().getTrainingHistories());
            // saveTrainingSkills(ServiceLocator.getTrainingContainer().getTrainingSkills());

            connection.commit();
            System.out.println("✅ Sync complete!");
        } catch (SQLException e) {
            System.err.println("❌ Sync failed: " + e.getMessage());
            try {
                connection.rollback();
                System.err.println("↩️ Rolled back transaction.");
            } catch (SQLException ex) {
                System.err.println("❌ Rollback failed: " + ex.getMessage());
            }
        } finally {
            try {
                connection.setAutoCommit(previousAutoCommit);
            } catch (SQLException e) {
                System.err.println("❌ Could not restore auto-commit: " + e.getMessage());
            }
        }
    }

    private void saveCompanies(ArrayList<Company> companies) throws SQLException {
        if (companies == null || companies.isEmpty()) return;
        String sql = "MERGE INTO companies (id, name) KEY (id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Company c : companies) {
                pstmt.setInt(1, c.getId());
                pstmt.setString(2, c.getName());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private void saveDepartments(ArrayList<Department> departments) throws SQLException {
        if (departments == null || departments.isEmpty()) return;
        String sql = "MERGE INTO departments (id, name, company_id) KEY (id) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Department d : departments) {
                pstmt.setInt(1, d.getId());
                pstmt.setString(2, d.getName());
                pstmt.setInt(3, d.getCompanyId());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private void saveTeams(ArrayList<Team> teams) throws SQLException {
        if (teams == null || teams.isEmpty()) return;
        String sql = "MERGE INTO teams (id, name, department_id) KEY (id) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Team t : teams) {
                pstmt.setInt(1, t.getId());
                pstmt.setString(2, t.getName());
                pstmt.setInt(3, t.getDepartmentId());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private void saveRoles(ArrayList<Role> roles) throws SQLException {
        if (roles == null || roles.isEmpty()) return;
        String sql = "MERGE INTO roles (id, name, description, system_permission) KEY (id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Role r : roles) {
                pstmt.setInt(1, r.getId());
                pstmt.setString(2, r.getName());
                pstmt.setString(3, r.getDescription());
                pstmt.setString(4, r.getSystemPermission());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private void saveSkills(ArrayList<Skill> skills) throws SQLException {
        if (skills == null || skills.isEmpty()) return;
        String sql = "MERGE INTO skills (id, name, description, required_years) KEY (id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Skill s : skills) {
                // Annahme: Modell-Methoden getSkillId/getSkillName existieren wie im Original.
                pstmt.setInt(1, s.getSkillId());
                pstmt.setString(2, s.getSkillName());
                pstmt.setString(3, s.getDescription());
                pstmt.setInt(4, s.getRequiredYears());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private void saveEmployees(ArrayList<Employee> employees) throws SQLException {
        if (employees == null || employees.isEmpty()) return;
        String sql = """
            MERGE INTO employees (id, username, password, first_name, last_name,
            email, phone_number, date_of_birth, address, gender, hire_date, employment_active,
            team_id, manager_id) KEY (id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
           """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Employee e : employees) {
                pstmt.setInt(1, e.getId());
                pstmt.setString(2, e.getUsername());
                pstmt.setString(3, e.getPassword());
                pstmt.setString(4, e.getFirstName());
                pstmt.setString(5, e.getLastName());
                pstmt.setString(6, e.getEMail());
                pstmt.setString(7, e.getPhoneNumber());

                // Datum-Felder defensiv behandeln (können null sein)
                if (e.getDateOfBirth() != null) {
                    // erwartet wird ein java.time.LocalDate oder ähnliches -> anpassen falls Modell anders ist
                    pstmt.setDate(8, java.sql.Date.valueOf(e.getDateOfBirth().toString()));
                } else {
                    pstmt.setNull(8, Types.DATE);
                }

                pstmt.setString(9, e.getAddress());
                pstmt.setString(10, String.valueOf(e.getGender()));

                if (e.getHireDate() != null) {
                    pstmt.setDate(11, java.sql.Date.valueOf(e.getHireDate().toString()));
                } else {
                    pstmt.setNull(11, Types.DATE);
                }

                pstmt.setBoolean(12, e.isEmploymentStatus());

                // team_id und manager_id können null sein; setObject erlaubt null
                pstmt.setObject(13, e.getTeam(), Types.INTEGER);
                pstmt.setObject(14, e.getManagerId(), Types.INTEGER);

                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private void saveTrainings(ArrayList<Training> trainings) throws SQLException {
        if (trainings == null || trainings.isEmpty()) return;
        String sql = "MERGE INTO trainings (id, title, description, duration_hours) KEY (id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (Training t : trainings) {
                pstmt.setInt(1, t.getId());
                pstmt.setString(2, t.getTitle());
                pstmt.setString(3, t.getDescription());
                pstmt.setInt(4, t.getLength());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    /**
     * Importiert komplette Datensätze aus JSON-Dateien in einem Verzeichnis.
     * Erwartete Dateinamen (konventionell):
     * - companies.json
     * - departments.json
     * - teams.json
     * - roles.json
     * - skills.json
     * - employees.json
     * - trainings.json
     * - training_skills.json (optional)
     * - role_history.json (optional)
     * - skill_history.json (optional)
     * - training_history.json (optional)
     *
     * Die Loader-Methoden sind als SKELETTE implementiert (TODO).
     */
    public void importFromJson(Path dir) throws IOException, SQLException {
        if (dir == null || !Files.isDirectory(dir)) {
            throw new IllegalArgumentException("dir must be an existing directory");
        }

        // Beispiel: lade companies.json
        Path companiesFile = dir.resolve("companies.json");
        ArrayList<Company> companies;
        if (Files.exists(companiesFile)) {
            companies = loadCompaniesFromJson(companiesFile);
            saveCompanies(companies);
        }

        Path departmentsFile = dir.resolve("departments.json");
        if (Files.exists(departmentsFile)) {
            ArrayList<Department> departments = loadDepartmentsFromJson(departmentsFile);
            saveDepartments(departments);
        }

        Path teamsFile = dir.resolve("teams.json");
        if (Files.exists(teamsFile)) {
            ArrayList<Team> teams = loadTeamsFromJson(teamsFile);
            saveTeams(teams);
        }

        Path rolesFile = dir.resolve("roles.json");
        if (Files.exists(rolesFile)) {
            ArrayList<Role> roles = loadRolesFromJson(rolesFile);
            saveRoles(roles);
        }

        Path skillsFile = dir.resolve("skills.json");
        if (Files.exists(skillsFile)) {
            ArrayList<Skill> skills = loadSkillsFromJson(skillsFile);
            saveSkills(skills);
        }

        Path employeesFile = dir.resolve("employees.json");
        if (Files.exists(employeesFile)) {
            ArrayList<Employee> employees = loadEmployeesFromJson(employeesFile);
            saveEmployees(employees);
        }

        Path trainingsFile = dir.resolve("trainings.json");
        if (Files.exists(trainingsFile)) {
            ArrayList<Training> trainings = loadTrainingsFromJson(trainingsFile);
            saveTrainings(trainings);
        }

        // Optional: training_skills, role_history, skill_history, training_history
        // TODO: Falls ihr die Modelle/Container/Tabellen für diese Entities habt, implementiert Loader und Save-Methoden.
    }

    // ----------------------------
    // SKELETT-LADER (TODOs)
    // ----------------------------
    // Diese Methoden liefern aktuell leere Listen zurück. Füge hier die JSON-Parsing-Logik hinzu.
    // Empfehlung: Jackson ObjectMapper (com.fasterxml.jackson.databind.ObjectMapper) verwenden.
    // Beispiel (kommentiert):
    // ObjectMapper mapper = new ObjectMapper();
    // ArrayList<Company> companies = mapper.readValue(file.toFile(), new TypeReference<ArrayList<Company>>() {});

    private ArrayList<Company> loadCompaniesFromJson(Path file) throws IOException {
        // TODO: parse companies.json -> ArrayList<Company>
        // Beispiel (kommentiert):
        // ObjectMapper mapper = new ObjectMapper();
        // return mapper.readValue(file.toFile(), new TypeReference<ArrayList<Company>>() {});
        return new ArrayList<>();
    }

    private ArrayList<Department> loadDepartmentsFromJson(Path file) throws IOException {
        // TODO: parse departments.json -> ArrayList<Department>
        return new ArrayList<>();
    }

    private ArrayList<Team> loadTeamsFromJson(Path file) throws IOException {
        // TODO: parse teams.json -> ArrayList<Team>
        return new ArrayList<>();
    }

    private ArrayList<Role> loadRolesFromJson(Path file) throws IOException {
        // TODO: parse roles.json -> ArrayList<Role>
        return new ArrayList<>();
    }

    private ArrayList<Skill> loadSkillsFromJson(Path file) throws IOException {
        // TODO: parse skills.json -> ArrayList<Skill>
        return new ArrayList<>();
    }

    private ArrayList<Employee> loadEmployeesFromJson(Path file) throws IOException {
        // TODO: parse employees.json -> ArrayList<Employee>
        // Achtung: JSON-Datumsformat ggf. anpassen (z.B. "yyyy-MM-dd") und in das Modell (LocalDate) mappen.
        return new ArrayList<>();
    }

    private ArrayList<Training> loadTrainingsFromJson(Path file) throws IOException {
        // TODO: parse trainings.json -> ArrayList<Training>
        return new ArrayList<>();
    }

    // ----------------------------
    // Helper / cleanup
    // ----------------------------
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("❌ Error closing connection: " + e.getMessage());
        }
    }
}
