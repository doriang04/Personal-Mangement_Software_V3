package model;

import java.util.Date;

public interface IEmployee {

    int getId();
    void setId(int id);

    String getUsername();
    void setUsername(String username);

    String getPassword();
    void setPassword(String password);

    String getFirstName();
    void setFirstName(String firstName);

    String getLastName();
    void setLastName(String lastName);

    String geteMail();
    void seteMail(String eMail);

    int getPhoneNumber();
    void setPhoneNumber(int phoneNumber);

    Date getDateOfBirth();
    void setDateOfBirth(Date dateOfBirth);

    String getAddress();
    void setAddress(String address);

    char getGender();
    void setGender(char gender);

    Date getHireDate();
    void setHireDate(Date hireDate);

    boolean isEmploymentStatus();
    void setEmploymentStatus(boolean employmentStatus);

    int getTeamId();
    void setTeamId(int teamId);

    int getManagerId();
    void setManagerId(Employee managerId);

    SkillManager getSkill();
    void setSkill(SkillManager skill);

    TrainingManager getTraining();
    void setTraining(TrainingManager training);

    RoleManager getRole();
    void setRole(RoleManager role);
}

