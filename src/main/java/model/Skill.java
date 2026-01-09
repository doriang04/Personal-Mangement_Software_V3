package model;

import java.util.ArrayList;

public class Skill {

    private int skillId;
    private String skillName;
    private String description;
    private int requiredYears;


    public Skill(int skillId, int requiredYears,String skillName, String description) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.description = description;
        this.requiredYears = requiredYears;
    }

    public Skill() {

    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public int getRequiredYears() {
        return requiredYears;
    }


    public void setRequiredYears(int requiredYears) {
        this.requiredYears = requiredYears;
    }

    public String getSkillName() {
        return skillName;
    }
    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

