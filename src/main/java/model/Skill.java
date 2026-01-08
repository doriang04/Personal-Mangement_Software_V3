package model;

import java.util.ArrayList;

public class Skill {

    private int skillId;
    private String requiredYears;
    private ArrayList<String> certification;
    private String description;

    public Skill(int skillId, String requiredYears, ArrayList<String> certification, String description) {
        this.skillId = skillId;
        this.requiredYears = requiredYears;
        this.certification = certification;
        this.description = description;
    }

    public Skill() {

    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public String getRequiredYears() {
        return requiredYears;
    }


    public void setRequiredYears(String requiredYears) {
        this.requiredYears = requiredYears;
    }

    public ArrayList<String> getCertification() {
        return certification;
    }

    public void setCertification(ArrayList<String> certification) {
        this.certification = certification;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

