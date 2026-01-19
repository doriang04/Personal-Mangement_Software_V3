package model;

import core.ServiceLocator;

public class Skill {

    private int id;
    private String name;
    private String description;
    private int required_years;


    public Skill(int required_years, String name, String description) {
        this.id = ServiceLocator.getSkillContainer().getNextFreeId();
        this.name = name;
        this.description = description;
        this.required_years = required_years;
    }

    public Skill() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRequired_years() {
        return required_years;
    }

    public void setRequired_years(int required_years) {
        this.required_years = required_years;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return getId() + "_(" + getName() + ")";
    }

    public boolean hasReferences() {
        for (TrainingSkillManager tsm: ServiceLocator.getTrainingSkillManagerContainer().getTrainingSkillManagers()) {
            for (TrainingSkillManager.TrainingSkillEntry tse: tsm.getSkills()) {
                if (tse.getSkillId() == getId()) return true;
            }
        }
        return false;
    }
}

