package model;

import java.util.ArrayList;
import java.util.Date;

public class TrainingSkillManager {

    public static class TrainingSkillEntry {
        private String skillId;
        private Date acquireDate;

        public TrainingSkillEntry(String skillId, Date acquireDate) {
            this.skillId = skillId;
            this.acquireDate = acquireDate;
        }

        public String getSkillId() {
            return skillId;
        }

        public Date getAcquireDate() {
            return acquireDate;
        }
    }

    private ArrayList<TrainingSkillEntry> skillList;

    public ArrayList<TrainingSkillEntry> getSkillList() {
        return skillList;
    }

    public void setSkillList(ArrayList<TrainingSkillEntry> skillList) {
        this.skillList = skillList;
    }

    // Platzhalter laut Diagramm
    public ArrayList<TrainingSkillEntry> getSkillHistory() { return null; }
    public void addAktiveSkillHistory(Skill skill, Date date) {}
    public void addInactiveSkillHistory(Skill skill, Date date) {}
    public void removeAktiveSkillHistory(ArrayList<TrainingSkillEntry> list) {}
    public void removeInactiveSkillHistory(ArrayList<TrainingSkillEntry> list) {}
    public Training getTraining() { return null; }
    public void setTraining(Training training, boolean booleanFlag) {}
    public void updateSelf() {}
}

