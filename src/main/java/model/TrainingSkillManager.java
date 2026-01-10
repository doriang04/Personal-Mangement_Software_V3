package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// TODO: and again, test gpt code (or adjust, not like i care lol)

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
    private final List<TrainingSkillEntry> skills = new ArrayList<>();

    public TrainingSkillManager(Training training) {
        this.trainingId = training.getId();
    }

    public TrainingSkillManager() {

    }


    public int getTrainingId() {
        return trainingId;
    }

    public List<TrainingSkillEntry> getSkills() {
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

    /**
     * Used when loading from DB.
     */
    public void setSkills(List<TrainingSkillEntry> entries) {
        skills.clear();
        if (entries != null) {
            skills.addAll(entries);
        }
    }
}
