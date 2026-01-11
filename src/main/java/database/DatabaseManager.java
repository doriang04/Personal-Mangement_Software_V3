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

public class DatabaseManager {

    private static DatabaseManager instance;

    private static final String DB_URL = "jdbc:h2:~/h2_db_files/personalmanagement;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private Connection connection;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    private DatabaseManager() {
        try {
            openConnection();
            initDatabase();
        } catch (SQLException e) {
            System.err.println("❌ Database initialisation failed: " + e.getMessage());
        }

    }

    private void openConnection() throws SQLException {
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
        }
    }

    private void initDatabase() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("db/schema.sql");
            if (is == null) {
                throw new RuntimeException("Schema file not found: db/schema.sql");
            }
            RunScript.execute(connection, new InputStreamReader(is));
            System.out.println("✅ Database schema initialized from schema.sql");
        } catch (Exception e) {
            System.err.println("❌ Schema init failed: " + e.getMessage());
        }
    }

    public void importFromJson(Path dir) throws IOException, SQLException {
        if (dir == null || !Files.isDirectory(dir)) {
            throw new IllegalArgumentException("dir must be an existing directory");
        }

        connection.setAutoCommit(false);
        try {
            saveCompanies(loadFromJson(dir.resolve("companies.json"), Company.class));
            saveDepartments(loadFromJson(dir.resolve("departments.json"), Department.class));
            saveTeams(loadFromJson(dir.resolve("teams.json"), Team.class));
            saveRoles(loadFromJson(dir.resolve("roles.json"), Role.class));
            saveSkills(loadFromJson(dir.resolve("skills.json"), Skill.class));
            saveTrainings(loadFromJson(dir.resolve("trainings.json"), Training.class));

            ArrayList<Employee> employees =
                    loadFromJson(dir.resolve("employees.json"), Employee.class);

            saveEmployees(employees);

            saveTrainingSkillsFromJson(dir.resolve("trainings.json"));
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

    private <T> ArrayList<T> loadFromJson(Path file, Class<T> clazz) throws IOException {
        return mapper.readValue(
                file.toFile(),
                mapper.getTypeFactory().constructCollectionType(ArrayList.class, clazz)
        );
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

                // Role History
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

                // Skill History
                for (var sh : e.getSkillManager().getSkillHistory()) {
                    skillStmt.setInt(1, e.getId());
                    skillStmt.setInt(2, sh.getSkillId());
                    skillStmt.setDate(3, Date.valueOf(sh.getAcquireDate()));
                    skillStmt.addBatch();
                }

                // Training History
                for (var th : e.getTrainingManager().getTrainingHistory()) {
                    trainingStmt.setInt(1, e.getId());
                    trainingStmt.setInt(2, th.getTrainingId());
                    trainingStmt.setString(3, th.isDone() ? "DONE" : "OPEN");

                    if (th.getAssignedAt() != null) {
                        trainingStmt.setDate(4, Date.valueOf(th.getAssignedAt()));
                    } else {
                        trainingStmt.setNull(4, Types.DATE);
                    }

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

    public void loadDataFromDb() throws SQLException {
        System.out.println("📥 Lade Daten aus der Datenbank in den Speicher...");

        loadCompanies();
        loadDepartments();
        loadTeams();
        loadRoles();
        loadSkills();
        loadTrainings();

        loadEmployees();

        loadRoleHistories();
        loadSkillHistories();
        loadTrainingHistories();

        System.out.println("✅ Alle Daten erfolgreich geladen!");
    }

    private void loadCompanies() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM companies")) {
            var container = ServiceLocator.getCompanyContainer();
            while (rs.next()) {
                Company c = new Company(rs.getInt("id"), rs.getString("name"));
                container.addCompany(c);
            }
        }
    }

    private void loadDepartments() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM departments")) {
            var container = ServiceLocator.getDepartmentContainer();
            while (rs.next()) {
                Department d = new Department(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("company_id")
                );
                container.addDepartment(d);
            }
        }
    }

    private void loadTeams() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM teams")) {
            var container = ServiceLocator.getTeamContainer();
            while (rs.next()) {
                Team t = new Team(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("department_id")
                );
                container.addTeam(t);
            }
        }
    }

    private void loadRoles() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM roles")) {
            var container = ServiceLocator.getRoleContainer();
            while (rs.next()) {
                Role r = new Role(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("system_permission")
                );
                container.addRole(r);
            }
        }
    }

    private void loadSkills() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM skills")) {
            var container = ServiceLocator.getSkillContainer();
            while (rs.next()) {
                Skill s = new Skill(
                        rs.getInt("id"),
                        rs.getInt("required_years"),
                        rs.getString("name"),
                        rs.getString("description")
                );
                container.addSkill(s);
            }
        }
    }

    private void loadTrainings() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM trainings")) {
            var container = ServiceLocator.getTrainingContainer();
            while (rs.next()) {
                Training t = new Training();
                t.setId(rs.getInt("id"));
                t.setTitle(rs.getString("title"));
                t.setDescription(rs.getString("description"));
                t.setLength(rs.getInt("duration_hours"));

                t.setSkills(new TrainingSkillManager(t));

                container.addTraining(t);
            }
        }

        String sql = "SELECT * FROM training_skills";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int tId = rs.getInt("training_id");
                int sId = rs.getInt("skill_id");

                Training training = ServiceLocator.getTrainingContainer().getTrainingById(tId);
                Skill skill = ServiceLocator.getSkillContainer().getSkillById(sId);

                if (training != null && skill != null) {
                    training.getSkills().addSkill(skill);
                }
            }
        }
    }

    private void loadEmployees() throws SQLException {
        String sql = "SELECT * FROM employees";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            var container = ServiceLocator.getEmployeeContainer();

            while (rs.next()) {
                Employee e = new Employee();
                e.setId(rs.getInt("id"));
                e.setUsername(rs.getString("username"));
                e.setPassword(rs.getString("password"));
                e.setFirstName(rs.getString("first_name"));
                e.setLastName(rs.getString("last_name"));
                e.setEMail(rs.getString("email"));
                e.setPhoneNumber(rs.getString("phone_number"));
                e.setAddress(rs.getString("address"));

                String genderStr = rs.getString("gender");
                if (genderStr != null && !genderStr.isEmpty()) {
                    e.setGender(genderStr.charAt(0));
                }

                e.setEmploymentStatus(rs.getBoolean("employment_active"));

                if (rs.getDate("date_of_birth") != null)
                    e.setDateOfBirth(new java.util.Date(rs.getDate("date_of_birth").getTime()));

                if (rs.getDate("hire_date") != null)
                    e.setHireDate(new java.util.Date(rs.getDate("hire_date").getTime()));

                int teamId = rs.getInt("team_id");
                if (!rs.wasNull()) e.setTeamId(teamId);

                int managerId = rs.getInt("manager_id");
                if (!rs.wasNull()) e.setManagerId(managerId);

                e.setRoleManager(new RoleManager(e));
                e.setSkillManager(new SkillManager(e));
                e.setTrainingManager(new TrainingManager(e));

                ServiceLocator.getRoleManagerContainer().addRoleManager(e.getRoleManager());
                ServiceLocator.getSkillManagerContainer().addSkillManager(e.getSkillManager());
                ServiceLocator.getTrainingManagerContainer().addTrainingManager(e.getTrainingManager());

                container.addEmployee(e);
            }
        }
    }

    private void loadRoleHistories() throws SQLException {
        String sql = "SELECT * FROM role_history ORDER BY assigned_at ASC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int empId = rs.getInt("employee_id");
                Employee emp = ServiceLocator.getEmployeeContainer().getEmployeeById(empId);

                if (emp != null) {
                    var entry = new RoleManager.RoleHistoryEntry();
                    entry.setHistoryId(rs.getInt("id"));
                    entry.setRoleId(rs.getInt("role_id"));
                    entry.setAcquireDate(rs.getDate("assigned_at").toLocalDate());

                    if (rs.getDate("ended_at") != null) {
                        entry.setEndDate(rs.getDate("ended_at").toLocalDate());
                    }

                    emp.getRoleManager().addRoleHistoryEntry(entry);
                }
            }
        }
    }

    private void loadSkillHistories() throws SQLException {
        String sql = "SELECT * FROM skill_history";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int empId = rs.getInt("employee_id");
                Employee emp = ServiceLocator.getEmployeeContainer().getEmployeeById(empId);

                if (emp != null) {
                    var entry = new SkillManager.SkillHistoryEntry();
                    entry.setHistoryId(rs.getInt("id"));
                    entry.setSkillId(rs.getInt("skill_id"));
                    entry.setAcquireDate(rs.getDate("acquire_date").toLocalDate());

                    emp.getSkillManager().addSkill(entry.getSkillId(), entry.getAcquireDate());
                }
            }
        }
    }

    private void loadTrainingHistories() throws SQLException {
        String sql = "SELECT * FROM training_history";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int empId = rs.getInt("employee_id");
                Employee emp = ServiceLocator.getEmployeeContainer().getEmployeeById(empId);

                if (emp != null) {
                    TrainingManager.Status status = TrainingManager.Status.valueOf(rs.getString("status"));

                    var entry = new TrainingManager.TrainingHistoryEntry(
                            rs.getInt("id"),
                            rs.getInt("training_id"),
                            status,
                            rs.getDate("assigned_at").toLocalDate(),
                            rs.getDate("completed_at") != null ? rs.getDate("completed_at").toLocalDate() : null
                    );

                    emp.getTrainingManager().getTrainingHistory().add(entry);
                }
            }
        }
    }

    public void saveAllData() {
        System.out.println("💾 Speichere Daten in die Datenbank...");
        try {
            openConnection();
            for (Employee e : ServiceLocator.getEmployeeContainer().getEmployees()) {
                updateEmployeeBaseData(e);
                updateRoleHistory(e);
                updateTrainingHistory(e);
            }
            System.out.println("✅ Speichern erfolgreich!");
        } catch (SQLException e) {
            System.err.println("❌ Fehler beim Speichern: " + e.getMessage());
        }
        closeConnection();
    }

    private void updateEmployeeBaseData(Employee e) throws SQLException {
        String sql = "UPDATE employees SET first_name=?, last_name=?, email=?, phone_number=?, address=?, password=?, team_id=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, e.getFirstName());
            stmt.setString(2, e.getLastName());
            stmt.setString(3, e.getEMail());
            stmt.setString(4, e.getPhoneNumber());
            stmt.setString(5, e.getAddress());
            stmt.setString(6, e.getPassword());

            if (e.getTeamId() > 0) stmt.setInt(7, e.getTeamId());
            else stmt.setNull(7, Types.INTEGER);

            stmt.setInt(8, e.getId());
            stmt.executeUpdate();
        }
    }

    private void updateRoleHistory(Employee e) throws SQLException { // TODO fix this method
        String deleteSql = "DELETE FROM role_history WHERE employee_id=?";
        try (PreparedStatement delStmt = connection.prepareStatement(deleteSql)) {
            delStmt.setInt(1, e.getId());
            delStmt.executeUpdate();
        }

        String insertSql = "INSERT INTO role_history (employee_id, role_id, assigned_at, ended_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            if (e.getRoleManager() != null) {
                for (RoleManager.RoleHistoryEntry entry : e.getRoleManager().getRoleHistory()) {
                    insertStmt.setInt(1, e.getId());
                    insertStmt.setInt(2, entry.getRoleId());
                    insertStmt.setDate(3, Date.valueOf(entry.getAcquireDate()));

                    if (entry.getEndDate() != null) {
                        insertStmt.setDate(4, Date.valueOf(entry.getEndDate()));
                    } else {
                        insertStmt.setNull(4, Types.DATE);
                    }
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }
        }
    }

    private void updateTrainingHistory(Employee e) throws SQLException { // TODO fix this method
        String deleteSql = "DELETE FROM training_history WHERE employee_id=?";
        try (PreparedStatement delStmt = connection.prepareStatement(deleteSql)) {
            delStmt.setInt(1, e.getId());
            delStmt.executeUpdate();
        }

        String insertSql = "INSERT INTO training_history (employee_id, training_id, status, assigned_at, completed_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            if (e.getTrainingManager() != null) {
                for (TrainingManager.TrainingHistoryEntry entry : e.getTrainingManager().getTrainingHistory()) {
                    insertStmt.setInt(1, e.getId());
                    insertStmt.setInt(2, entry.getTrainingId());

                    // Status als String speichern (OPEN/DONE)
                    String statusStr = (entry.getStatus() != null) ? entry.getStatus().name() : "OPEN";
                    insertStmt.setString(3, statusStr);

                    insertStmt.setDate(4, Date.valueOf(entry.getAssignedAt()));

                    if (entry.getCompletedAt() != null) {
                        insertStmt.setDate(5, Date.valueOf(entry.getCompletedAt()));
                    } else {
                        insertStmt.setNull(5, Types.DATE);
                    }
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }
        }
    }

    public void deleteEmployee(int id) {
        String sql = "DELETE FROM employees WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("✅ Mitarbeiter " + id + " aus DB gelöscht.");
        } catch (SQLException e) {
            System.err.println("❌ Fehler beim Löschen: " + e.getMessage());
        }
    }

    public void addEmployee(Employee e) {
        // Falls du andere Spaltennamen in der H2-DB hast, hier anpassen!
        String sql = "INSERT INTO employees (id, username, password, first_name, last_name, email, phone_number, " +
                "date_of_birth, address, gender, hire_date, employment_active, team_id, manager_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, e.getId());
            stmt.setString(2, e.getUsername());
            stmt.setString(3, e.getPassword());
            stmt.setString(4, e.getFirstName());
            stmt.setString(5, e.getLastName());
            stmt.setString(6, e.getEMail());
            stmt.setString(7, e.getPhoneNumber());
            // Datum konvertieren (java.util.Date -> java.sql.Date)
            stmt.setDate(8, new java.sql.Date(e.getDateOfBirth().getTime()));
            stmt.setString(9, e.getAddress());
            stmt.setString(10, String.valueOf(e.getGender()));
            stmt.setDate(11, new java.sql.Date(e.getHireDate().getTime()));
            stmt.setBoolean(12, e.isEmploymentStatus());

            // Bei 0 setzen wir NULL, falls kein Team/Manager existiert (optional)
            if (e.getTeamId() == 0) stmt.setNull(13, Types.INTEGER);
            else stmt.setInt(13, e.getTeamId());

            if (e.getManagerId() == 0) stmt.setNull(14, Types.INTEGER);
            else stmt.setInt(14, e.getManagerId());

            stmt.executeUpdate();
            System.out.println("✅ Mitarbeiter " + e.getId() + " in DB gespeichert.");
        } catch (SQLException ex) {
            System.err.println("❌ Fehler beim Speichern: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("❌ Error closing connection: " + e.getMessage());
        }
    }
}
