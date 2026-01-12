package model;

import core.ServiceLocator;

public class Training {

    private int id;
    private String title;
    private String description;
    private int length;
    private TrainingSkillManager skillList;
    private Employee assigningManager;

    public Training(String title, String description, int length, TrainingSkillManager skillList) {
        this.id = ServiceLocator.getTrainingContainer().getNextFreeId();
        this.title = title;
        this.description = description;
        this.length = length;
        this.skillList = skillList;
    }

    public Training() {

    }

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

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public TrainingSkillManager getSkills() {
        return skillList;
    }

    public Employee getAssigningManager() {
        return assigningManager;
    }

    public void setAssigningManager(Employee assigningManager) {
        this.assigningManager = assigningManager;
    }

    public void setSkills(TrainingSkillManager skillList) {
        this.skillList = skillList;
    }   

    public void setSkillList(TrainingSkillManager skillList) {
        this.skillList = skillList;
    }

    public TrainingSkillManager getSkillList() {
        return skillList;
    }

    public boolean hasReferences() {
        for (TrainingManager tm: ServiceLocator.getTrainingManagerContainer().getTrainingManagers()) {
            for (TrainingManager.TrainingHistoryEntry the: tm.getTrainingHistory()) {
                if (the.getTrainingId() == getId()) return true;
            }
        }
        return false;
    }
}

