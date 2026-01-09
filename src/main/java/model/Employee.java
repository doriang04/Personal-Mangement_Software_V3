package model;

import java.util.Date;
public class Employee {

    private int id;
    private String username;
    private String password;

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Date dateOfBirth;
    private String address;
    private char gender;

    private Date hireDate;
    private boolean employmentStatus;

    private int teamId;
    private int managerId;

    private SkillManager skillManager;
    private TrainingManager openTrainingManager;
    private TrainingManager doneTrainingManager;
    private RoleManager roleManager;
    
    public Employee(int id, int teamId, String username, String password,
                    String firstName, String lastName, String email, Date dateOfBirth, String adress, char gender, Date hireDate,
                    int managerId, boolean employmentStatus, String phoneNumber, SkillManager skillManager, TrainingManager openTrainingManager, TrainingManager doneTrainingManager) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.address = adress;
        this.gender = gender;
        this.hireDate = hireDate;
        this.teamId = teamId;
        this.managerId = managerId;
        this.employmentStatus = employmentStatus;
        this.phoneNumber = phoneNumber;
        this.skillManager = skillManager;
        this.openTrainingManager = openTrainingManager;
        this.doneTrainingManager = doneTrainingManager;
        this.roleManager = new RoleManager(this);
    }
    public Employee() {

    }

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

    public String getEMail() {
        return email;
    }

    public void setEMail(String eMail) {
        this.email = eMail;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
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

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public boolean isEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(boolean employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getManagerId() {
        return managerId;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public void setSkillManager(SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    public TrainingManager getOpenTrainingManager() {
        return openTrainingManager;
    }

    public void setOpenTrainingManager(TrainingManager openTrainingManager) {
        this.openTrainingManager = openTrainingManager;
    }

    public TrainingManager getDoneTrainingManager() {
        return doneTrainingManager;
    }

    public void setDoneTrainingManager(TrainingManager doneTrainingManager) {
        this.doneTrainingManager = doneTrainingManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public void setRoleManager(RoleManager roleManager) {
        this.roleManager = roleManager;
    }
}

