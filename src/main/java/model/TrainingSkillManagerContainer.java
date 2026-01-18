package model;

import java.util.ArrayList;

public class TrainingSkillManagerContainer {

    private static TrainingSkillManagerContainer instance;

    private ArrayList<TrainingSkillManager> trainingSkillManagers = new ArrayList<>();

    private TrainingSkillManagerContainer() {}

    public static synchronized TrainingSkillManagerContainer getInstance() {
        if (instance == null) instance = new TrainingSkillManagerContainer();
        return instance;
    }

    public void addTrainingSkillManager(TrainingSkillManager trainingSkillManager) {
        trainingSkillManagers.add(trainingSkillManager);
    }

    public void removeTrainingSkillManager(TrainingSkillManager trainingSkillManager) throws Exception {
        if (trainingSkillManager.hasReferences()) throw new Exception("trainingSkillManager darf nicht gelöscht werden, da darauf verwiesen wird.");
        trainingSkillManagers.remove(trainingSkillManager);
    }

    public ArrayList<TrainingSkillManager> getTrainingSkillManagers() {
        return trainingSkillManagers;
    }

    public TrainingSkillManager getTrainingSkillManagerByTrainingId(int trainingId) {
        for (TrainingSkillManager manager : trainingSkillManagers) {
            if (manager.getTrainingId() == trainingId) {
                return manager;
            }
        }
        return null;
    }
}