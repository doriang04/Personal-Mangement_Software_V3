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

    private Training training;
    private int id;
    
    public TrainingSkillManager(Training training) {
        this.skillList = new ArrayList<>();
        this.training = training;
        this.id = training.getId();
    }

    private ArrayList<TrainingSkillEntry> skillList;

    public ArrayList<TrainingSkillEntry> getSkillList() {
        return skillList;
    }

    public void setSkillList(ArrayList<TrainingSkillEntry> skillList) {
        this.skillList = skillList;
    }

    // Platzhalter laut Diagramm
    public void addSkill(Skill skill, Date date) {
        TrainingSkillEntry entry = new TrainingSkillEntry(String.valueOf(skill.getSkillId()), date);
        skillList.add(entry);
    }

    public void removeSkill(Skill skill) {
        if (skill == null) {
            return;
        }
        String id = String.valueOf(skill.getSkillId());
        for (java.util.Iterator<TrainingSkillEntry> it = skillList.iterator(); it.hasNext();) {
            TrainingSkillEntry entry = it.next();
            if (id.equals(entry.getSkillId())) {
                it.remove();
            }
        }
    }
    public Training getTraining() {
        return training;
    }
    public void setTraining(Training training) {
        this.training = training;
    }
}

