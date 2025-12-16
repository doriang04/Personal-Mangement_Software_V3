package model;

public class Employee {

    // Attribute
    private int id;
    private Team team;
    private Employee managerId;
    private String username;
    private String password;
    private String permissionString;
    private String firstName;
    private String lastName;
    private String eMail;
    private int phoneNumber;
    private java.util.Date dateOfBirth;
    private String address;
    private char gender;
    private java.util.Date hireDate;
    private boolean employmentStatus;
    private SkillManager skill;
    private TrainingManager training;
    private RoleManager role;

    // Methoden
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPermissionString() {
        return permissionString;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public java.util.Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(java.util.Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public char getGender() {
        return gender;
    }

    public void setGender(char gender) {
        this.gender = gender;
    }

    public java.util.Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(java.util.Date hireDate) {
        this.hireDate = hireDate;
    }

    public boolean isEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(boolean employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public int getTeamId() {
        return team != null ? team.getTeamId() : 0;
    }

    public void setTeamId(int teamId) {
        // ggf. Team über ID laden und zuweisen
    }

    public int getManagerId() {
        return managerId != null ? managerId.getId() : 0;
    }

    public void setManagerId(Employee managerId) {
        this.managerId = managerId;
    }

    public SkillManager getSkill() {
        return skill;
    }

    public void setSkill(SkillManager skill) {
        this.skill = skill;
    }

    public TrainingManager getTraining() {
        return training;
    }

    public void setTraining(TrainingManager training) {
        this.training = training;
    }

    public RoleManager getRole() {
        return role;
    }

    public void setRole(RoleManager role) {
        this.role = role;
    }
}

