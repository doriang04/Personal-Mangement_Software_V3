-- src/main/resources/db/schema.sql
CREATE TABLE IF NOT EXISTS companies (
                                         id INT PRIMARY KEY AUTO_INCREMENT,
                                         name VARCHAR(255) NOT NULL
    );

CREATE TABLE IF NOT EXISTS departments (
                                           id INT PRIMARY KEY AUTO_INCREMENT,
                                           department_id INT NOT NULL,
                                           department_name VARCHAR(255) NOT NULL,
    company_id INT,
    FOREIGN KEY (company_id) REFERENCES companies(id)
    );

CREATE TABLE IF NOT EXISTS teams (
                                     id INT PRIMARY KEY AUTO_INCREMENT,
                                     department_id INT NOT NULL,
                                     team_id INT NOT NULL,
                                     team_name VARCHAR(255) NOT NULL,
    FOREIGN KEY (department_id) REFERENCES departments(id)
    );

CREATE TABLE IF NOT EXISTS roles (
                                     id INT PRIMARY KEY AUTO_INCREMENT,
                                     role_id INT NOT NULL UNIQUE,
                                     name VARCHAR(255) NOT NULL,
    description TEXT,
    permission VARCHAR(100)
    );

CREATE TABLE IF NOT EXISTS skills (
                                      id INT PRIMARY KEY AUTO_INCREMENT,
                                      skill_id INT NOT NULL UNIQUE,
                                      required_years VARCHAR(10),
    description TEXT,
    certifications TEXT  -- JSON Array as String
    );

CREATE TABLE IF NOT EXISTS employees (
                                         id INT PRIMARY KEY AUTO_INCREMENT,
                                         employee_id INT NOT NULL UNIQUE,
                                         username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255),
    phone_number VARCHAR(20),
    date_of_birth DATE,
    address TEXT,
    gender CHAR(1),
    hire_date DATE,
    employment_status BOOLEAN DEFAULT true,
    team_id INT,
    manager_id INT,
    role_id INT,
    FOREIGN KEY (team_id) REFERENCES teams(id),
    FOREIGN KEY (manager_id) REFERENCES employees(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
    );

CREATE TABLE IF NOT EXISTS trainings (
                                         id INT PRIMARY KEY AUTO_INCREMENT,
                                         training_id INT NOT NULL UNIQUE,
                                         title VARCHAR(255) NOT NULL,
    description TEXT,
    length TIME,
    assigning_manager_id INT,
    FOREIGN KEY (assigning_manager_id) REFERENCES employees(id)
    );
