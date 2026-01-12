package model;

import core.ServiceLocator;

import java.util.ArrayList;
import java.util.Iterator;

public class TrainingSkillManager {

    public static class TrainingSkillEntry {
        private int trainingId;
        private int skillId;

        public TrainingSkillEntry(int trainingId, int skillId) {
            this.trainingId = trainingId;
            this.skillId = skillId;
        }

        public TrainingSkillEntry() {

        }

        public int getTrainingId() {
            return trainingId;
        }

        public int getSkillId() {
            return skillId;
        }
    }

    private int trainingId; // war mal final
    private final ArrayList<TrainingSkillEntry> skills = new ArrayList<>();

    public TrainingSkillManager(Training training) {
        this.trainingId = training.getId();
    }

    public TrainingSkillManager() {

    }

    public int getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(int trainingId) {
        this.trainingId = trainingId;
    }

    public ArrayList<TrainingSkillEntry> getSkills() {
        return new ArrayList<>(skills);
    }

    public void addSkill(Skill skill) {
        if (skill == null) return;
        skills.add(new TrainingSkillEntry(trainingId, skill.getId()));
    }

    public void removeSkill(Skill skill) {
        if (skill == null) return;

        int skillId = skill.getId();
        Iterator<TrainingSkillEntry> it = skills.iterator();
        while (it.hasNext()) {
            if (it.next().getSkillId() == skillId) {
                it.remove();
            }
        }
    }

    public void setSkills(ArrayList<TrainingSkillEntry> entries) {
        skills.clear();
        if (entries != null) {
            skills.addAll(entries);
        }
    }

    public boolean hasReferences() {
        for (Training training: ServiceLocator.getTrainingContainer().getTrainings()) {
            if (training.getId() == getTrainingId()) return true;
        }
        return false;
    }
}
