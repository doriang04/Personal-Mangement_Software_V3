package model;

import java.util.ArrayList;

public class TrainingSkillManagerContainer {

    private static TrainingSkillManagerContainer instance;
    private ArrayList<TrainingSkillManagerContainer> trainingSkillManagers = new ArrayList<>();

    private TrainingSkillManagerContainer() {}

    public static synchronized TrainingSkillManagerContainer getInstance() {
        if (instance == null) instance = new TrainingSkillManagerContainer();
        return instance;
    }

    public void addTrainingSkillManagerContainer(TrainingSkillManagerContainer trainingSkillManager) {
        trainingSkillManagers.add(trainingSkillManager);
    }

    public void removeTrainingSkillManagerContainer(TrainingSkillManagerContainer trainingSkillManager) {
        trainingSkillManagers.remove(trainingSkillManager);
    }

    public ArrayList<TrainingSkillManagerContainer> getTrainingSkillManagerContainers() {
        return trainingSkillManagers;
    }
    
}
