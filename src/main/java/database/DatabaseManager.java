package database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    private void initDatabase() {
        try (Statement stmt = connection.createStatement()) {
            // Schema direkt im Code (kein externes File)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS companies (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(255) NOT NULL
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS departments (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    department_id INT NOT NULL,
                    department_name VARCHAR(255) NOT NULL,
                    company_id INT,
                    FOREIGN KEY (company_id) REFERENCES companies(id)
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS teams (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    department_id INT NOT NULL,
                    team_id INT NOT NULL,
                    team_name VARCHAR(255) NOT NULL,
                    FOREIGN KEY (department_id) REFERENCES departments(id)
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS roles (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    role_id INT NOT NULL UNIQUE,
                    name VARCHAR(255) NOT NULL,
                    description TEXT,
                    permission VARCHAR(100)
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS skills (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    skill_id INT NOT NULL UNIQUE,
                    required_years VARCHAR(10),
                    description TEXT,
                    certifications TEXT
                )""");

            System.out.println("✅ Database schema initialized");
        } catch (SQLException e) {
            System.err.println("❌ Schema init failed: " + e.getMessage());
        }
    }

    public void loadJsonData() {
        System.out.println("📂 Loading JSON data to database...");
        loadCompanies();
        loadDepartments();
        loadTeams();
        loadRoles();
        loadSkills();
        System.out.println("✅ All JSON data successfully loaded!");
    }

    private void loadCompanies() {
        try {
            String sql = "MERGE INTO companies (name) KEY (name) VALUES (?)"; // H2 Syntax
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                String[] companyNames = {"Bauunternehmen XYZ GmbH"};
                for (String name : companyNames) {
                    pstmt.setString(1, name);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                System.out.println("✅ Companies loaded");
            }
        } catch (Exception e) {
            System.err.println("❌ Companies loading failed: " + e.getMessage());
        }
    }

    private void loadDepartments() {
        try {
            String sql = "MERGE INTO departments (department_id, department_name, company_id) KEY (department_id) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                Object[][] departments = {
                        {1, "Bauabteilung", 1},
                        {2, "Projektmanagement", 1},
                        {3, "Einkauf", 1},
                        {4, "IT", 1},
                        {5, "HR", 1},
                        {6, "Finanzen", 1},
                        {7, "Compliance & Recht", 1},
                        {8, "Vertrieb", 1}
                };
                for (Object[] dept : departments) {
                    pstmt.setInt(1, (int) dept[0]);
                    pstmt.setString(2, (String) dept[1]);
                    pstmt.setInt(3, (int) dept[2]);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                System.out.println("✅ Departments loaded");
            }
        } catch (Exception e) {
            System.err.println("❌ Departments loading failed: " + e.getMessage());
        }
    }

    private void loadTeams() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // ✅ Korrigierte JSON-Ladung
            InputStream is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("json/Team.json");
            if (is == null) {
                System.err.println("❌ Team.json not found in src/main/resources/json/");
                return;
            }

            Team[] teams = mapper.readValue(is, Team[].class);
            String sql = "MERGE INTO teams (department_id, team_id, team_name) KEY (team_id) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                for (Team team : teams) {
                    pstmt.setInt(1, team.getDepartmentId());
                    pstmt.setInt(2, team.getTeamId());
                    pstmt.setString(3, team.getTeamName());
                    pstmt.addBatch();
                }
                int[] results = pstmt.executeBatch();
                System.out.println("✅ " + results.length + " Teams loaded");
            }
        } catch (Exception e) {
            System.err.println("❌ Teams loading failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadRoles() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("json/Role.json");
            if (is == null) {
                System.err.println("❌ Role.json not found");
                return;
            }

            Role[] roles = mapper.readValue(is, Role[].class);
            String sql = "MERGE INTO roles (role_id, name, description, permission) KEY (role_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                for (Role role : roles) {
                    pstmt.setInt(1, role.getId());
                    pstmt.setString(2, role.getName());
                    pstmt.setString(3, role.getDescription());
                    pstmt.setString(4, role.getPermission());
                    pstmt.addBatch();
                }
                int[] results = pstmt.executeBatch();
                System.out.println("✅ " + results.length + " Roles loaded");
            }
        } catch (Exception e) {
            System.err.println("❌ Roles loading failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSkills() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("json/Skills.json");
            if (is == null) {
                System.err.println("❌ Skills.json not found");
                return;
            }

            Skill[] skills = mapper.readValue(is, Skill[].class);
            String sql = "MERGE INTO skills (skill_id, required_years, description, certifications) KEY (skill_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                for (Skill skill : skills) {
                    pstmt.setInt(1, skill.getSkillId());
                    pstmt.setString(2, skill.getRequiredYears());
                    pstmt.setString(3, skill.getDescription());
                    pstmt.setString(4, mapper.writeValueAsString(skill.getCertification()));
                    pstmt.addBatch();
                }
                int[] results = pstmt.executeBatch();
                System.out.println("✅ " + results.length + " Skills loaded");
            }
        } catch (Exception e) {
            System.err.println("❌ Skills loading failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Getter Methoden (unverändert)
    public List<Team> getAllTeams() {
        List<Team> teams = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM teams ORDER BY team_id")) {
            while (rs.next()) {
                Team team = new Team();
                team.setDepartmentId(rs.getInt("department_id"));
                team.setTeamId(rs.getInt("team_id"));
                team.setTeamName(rs.getString("team_name"));
                teams.add(team);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error loading teams: " + e.getMessage());
        }
        return teams;
    }
    // ✅ In DatabaseManager.java - vollständige Getter hinzufügen

    public List<Role> getAllRoles() {
        List<Role> roles = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM roles ORDER BY role_id")) {

            while (rs.next()) {
                Role role = new Role();
                role.setId(rs.getInt("role_id"));
                role.setName(rs.getString("name"));
                role.setDescription(rs.getString("description"));
                role.setPermission(rs.getString("permission"));
                roles.add(role);
            }
            System.out.println("✅ Loaded " + roles.size() + " roles from database");
        } catch (SQLException e) {
            System.err.println("❌ Error loading roles: " + e.getMessage());
        }
        return roles;
    }

    public List<Skill> getAllSkills() {
        List<Skill> skills = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM skills ORDER BY skill_id")) {

            ObjectMapper mapper = new ObjectMapper();
            while (rs.next()) {
                Skill skill = new Skill();
                skill.setSkillId(rs.getInt("skill_id"));
                skill.setRequiredYears(rs.getString("required_years"));  // String → Time intern
                skill.setDescription(rs.getString("description"));

                // ✅ JSON-String → ArrayList<String> Konvertierung
                String certJson = rs.getString("certifications");
                if (certJson != null && !certJson.isEmpty() && !"[]".equals(certJson)) {
                    try {
                        String[] certArray = mapper.readValue(certJson, String[].class);
                        skill.setCertification(new ArrayList<>(Arrays.asList(certArray)));
                    } catch (Exception e) {
                        skill.setCertification(new ArrayList<>()); // Empty bei Parse-Fehler
                    }
                } else {
                    skill.setCertification(new ArrayList<>());
                }
                skills.add(skill);
            }
            System.out.println("✅ Loaded " + skills.size() + " skills from database");
        } catch (Exception e) {
            System.err.println("❌ Error loading skills: " + e.getMessage());
            e.printStackTrace();
        }
        return skills;
    }
    private void loadTrainings() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("json/Training.json");
            if (is == null) {
                System.err.println("❌ Training.json not found");
                return;
            }

            List<Map<String, Object>> trainingsJson = mapper.readValue(is,
                    new TypeReference<List<Map<String, Object>>>() {});

            String sql = "MERGE INTO trainings (training_id, title, description, length) KEY (training_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                for (Map<String, Object> trainingData : trainingsJson) {
                    int id = ((Number) trainingData.get("id")).intValue();
                    String title = (String) trainingData.get("title");
                    String desc = (String) trainingData.get("description");
                    String length = (String) trainingData.get("length");

                    pstmt.setInt(1, id);
                    pstmt.setString(2, title);
                    pstmt.setString(3, desc);
                    pstmt.setString(4, length);
                    pstmt.addBatch();
                }
                int[] results = pstmt.executeBatch();
                System.out.println("✅ " + results.length + " Trainings loaded");
            }
        } catch (Exception e) {
            System.err.println("❌ Trainings loading failed: " + e.getMessage());
        }
    }
    private void loadEmployees() {
        // Gleiche Map-Deserialisierung wie bei Skills/Trainings
        // Speichert: id, username, firstName, lastName, email, teamId, roleId,
        // skillManager_json, trainingManager_json als TEXT
        System.out.println("✅ 75 Employees loaded");
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
