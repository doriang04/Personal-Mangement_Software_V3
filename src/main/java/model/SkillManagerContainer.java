package model;

import java.util.ArrayList;
import java.util.List;

public class SkillManagerContainer {
    private static SkillManagerContainer instance;
    private ArrayList<SkillManager> skillManagers = new ArrayList<>();

    private SkillManagerContainer() {}

    public static synchronized SkillManagerContainer getInstance() {
        if (instance == null) instance = new SkillManagerContainer();
        return instance;
    }

    public void addSkillManager(SkillManager sm) { skillManagers.add(sm); }

    public void removeSkillManager(SkillManager sm) { skillManagers.remove(sm); }

    public ArrayList<SkillManager> getSkillManagers() {
        return skillManagers;
    }

    public SkillManager getSkillManagerById(int SkillManagerid) {
        for (SkillManager SkillManager: skillManagers) {
            if (SkillManager.getId() == SkillManagerid) {
                return SkillManager;
            }
        }
        return null;
    }

}


