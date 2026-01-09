package database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
    private static final String DB_URL = "jdbc:h2:~/h2_db_files/personalmanagement;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private Connection connection;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

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
                pstmt.setInt(1, s.getId());
                pstmt.setString(2, s.getName());
                pstmt.setString(3, s.getDescription());
                pstmt.setInt(4, s.getRequired_years());
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
                    pstmt.setDate(8, new java.sql.Date(e.getDateOfBirth().getTime()));
                } else {
                    pstmt.setNull(8, Types.DATE);
                }

                pstmt.setString(9, e.getAddress());
                pstmt.setString(10, String.valueOf(e.getGender()));

                if (e.getHireDate() != null) {
                    pstmt.setDate(11, new java.sql.Date(e.getHireDate().getTime()));
                } else {
                    pstmt.setNull(11, Types.DATE);
                }

                pstmt.setBoolean(12, e.isEmploymentStatus());

                // team_id und manager_id können null sein; setObject erlaubt null
                pstmt.setObject(13, e.getTeamId(), Types.INTEGER);
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

    public void importFromJson(Path dir) throws IOException, SQLException {
        if (dir == null || !Files.isDirectory(dir)) {
            throw new IllegalArgumentException("dir must be an existing directory");
        }

        connection.setAutoCommit(false);
        try {
            saveCompanies(loadCompaniesFromJson(dir.resolve("companies.json")));
            saveDepartments(loadDepartmentsFromJson(dir.resolve("departments.json")));
            saveTeams(loadTeamsFromJson(dir.resolve("teams.json")));
            saveRoles(loadRolesFromJson(dir.resolve("roles.json")));
            saveSkills(loadSkillsFromJson(dir.resolve("skills.json")));
            saveTrainings(loadTrainingsFromJson(dir.resolve("trainings.json")));

            // Employees + Histories
            ArrayList<Employee> employees = loadEmployeesFromJson(dir.resolve("employees.json"));
            saveEmployees(employees);

            // Trainings → Skills (N:M)
            saveTrainingSkillsFromJson(dir.resolve("trainings.json"));

            // This last to avoid Foreign Key Issues
            saveEmployeeHistoriesFromJson(employees);

            connection.commit();
            System.out.println("✅ JSON → DB import complete");
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // TODO find a way to do this in a pretty way
    private ArrayList<Company> loadCompaniesFromJson(Path file) throws IOException {
        return mapper.readValue(file.toFile(),
                new TypeReference<>() {
                });
    }

    private ArrayList<Department> loadDepartmentsFromJson(Path file) throws IOException {
        return mapper.readValue(file.toFile(),
                new TypeReference<>() {
                });
    }

    private ArrayList<Team> loadTeamsFromJson(Path file) throws IOException {
        return mapper.readValue(file.toFile(),
                new TypeReference<>() {
                });
    }

    private ArrayList<Role> loadRolesFromJson(Path file) throws IOException {
        return mapper.readValue(file.toFile(),
                new TypeReference<>() {
                });
    }

    private ArrayList<Skill> loadSkillsFromJson(Path file) throws IOException {
        return mapper.readValue(file.toFile(),
                new TypeReference<>() {
                });
    }

    private ArrayList<Employee> loadEmployeesFromJson(Path file) throws IOException {
        return mapper.readValue(file.toFile(),
                new TypeReference<>() {
                });
    }

    private ArrayList<Training> loadTrainingsFromJson(Path file) throws IOException {
        return mapper.readValue(file.toFile(),
                new TypeReference<>() {
                });
    }

    private void saveEmployeeHistoriesFromJson(ArrayList<Employee> employees) throws SQLException {
        String roleSql = """
        INSERT INTO role_history (employee_id, role_id, assigned_at, ended_at)
        VALUES (?, ?, ?, ?)
    """;

        String skillSql = """
        INSERT INTO skill_history (employee_id, skill_id, acquire_date)
        VALUES (?, ?, ?)
    """;

        String trainingSql = """
        INSERT INTO training_history
        (employee_id, training_id, status, assigned_at, completed_at)
        VALUES (?, ?, ?, ?, ?)
    """;

        try (
                PreparedStatement roleStmt = connection.prepareStatement(roleSql);
                PreparedStatement skillStmt = connection.prepareStatement(skillSql);
                PreparedStatement trainingStmt = connection.prepareStatement(trainingSql)
        ) {
            for (Employee e : employees) {

                // -------- ROLE HISTORY --------
                var rm = e.getRoleManager();

                for (var entry : rm.getRoleHistory()) {
                    roleStmt.setInt(1, e.getId());
                    roleStmt.setInt(2, entry.getRoleId());
                    roleStmt.setDate(3, Date.valueOf(entry.getAcquireDate()));
                    if (entry.getEndDate() != null) {
                        roleStmt.setDate(4, Date.valueOf(entry.getEndDate()));
                    } else {
                        roleStmt.setNull(4, Types.DATE);
                    }
                    roleStmt.addBatch();
                }

                // -------- SKILL HISTORY --------
                for (var sh : e.getSkillManager().getSkillHistory()) {
                    skillStmt.setInt(1, e.getId());
                    skillStmt.setInt(2, sh.getSkillId());
                    skillStmt.setDate(3, Date.valueOf(sh.getAcquireDate()));
                    skillStmt.addBatch();
                }

                // -------- TRAINING HISTORY --------
                for (var th : e.getOpenTrainingManager().getTrainingHistory()) {
                    trainingStmt.setInt(1, e.getId());
                    trainingStmt.setInt(2, th.getTrainingId());
                    trainingStmt.setString(3, th.isDone() ? "DONE" : "OPEN");
                    trainingStmt.setDate(4, Date.valueOf(th.getAssignedAt()));
                    if (th.getCompletedAt() != null) {
                        trainingStmt.setDate(5, Date.valueOf(th.getCompletedAt()));
                    } else {
                        trainingStmt.setNull(5, Types.DATE);
                    }
                    trainingStmt.addBatch();
                }
            }

            roleStmt.executeBatch();
            skillStmt.executeBatch();
            trainingStmt.executeBatch();
        }
    }

    private void saveTrainingSkillsFromJson(Path file) throws IOException, SQLException {
        JsonNode root = mapper.readTree(file.toFile());

        String sql = "INSERT INTO training_skills (training_id, skill_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (JsonNode training : root) {
                int trainingId = training.get("id").asInt();
                JsonNode skills = training.path("skill").path("aktiveSkillHistory");

                for (JsonNode s : skills) {
                    stmt.setInt(1, trainingId);
                    stmt.setInt(2, s.get("skillId").asInt());
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        }
    }

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
