package model;

import java.util.ArrayList;

public class SkillContainer {

    private static SkillContainer instance;
    private ArrayList<Skill> skills = new ArrayList<>();

    private SkillContainer() {}

    public static synchronized SkillContainer getInstance() {
        if (instance == null) instance = new SkillContainer();
        return instance;
    }

    public void addSkill(Skill skill) { skills.add(skill); }

    public void removeSkill(Skill skill) throws Exception {
        if (skill.hasReferences()) throw new Exception("Not allowed to delete skill, as it is referenced.");
        skills.remove(skill);
    }

    public ArrayList<Skill> getSkills() {
        return skills;
    }

    public Skill getSkillById(int Skillid) {
        for (Skill skill: skills) {
            if (skill.getId() == Skillid) {
                return skill;
            }
        }
        return null;
    }

    public int getNextFreeId() {
        int i = 0;
        while (true) {
            if (getSkillById(i) == null) return i;
            i++;
        }
    }
}
