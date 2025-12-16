package model;

import java.sql.Time;

public class Training {

    private int id;
    private String title;
    private String description;
    private Time length;
    private SkillManager skill;
    private Employee assigningManager;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Time getLength() {
        return length;
    }

    public void setLength(Time length) {
        this.length = length;
    }

    public SkillManager getSkill() {
        return skill;
    }

    public void setSkill(SkillManager skill) {
        this.skill = skill;
    }

    public Employee getAssigningManager() {
        return assigningManager;
    }

    public void setAssigningManager(Employee assigningManager) {
        this.assigningManager = assigningManager;
    }
}

