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

    public void removeSkill(Skill skill) {
        skills.remove(skill);
    }

    public ArrayList<Skill> getSkills() {
        return skills;
    }
    
}
