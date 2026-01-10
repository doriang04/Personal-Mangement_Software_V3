package model;

import java.util.ArrayList;

public class TrainingSkillManagerContainer {

    private static TrainingSkillManagerContainer instance;

    // KORREKTUR: Die Liste muss 'TrainingSkillManager' speichern, nicht den Container selbst.
    private ArrayList<TrainingSkillManager> trainingSkillManagers = new ArrayList<>();

    private TrainingSkillManagerContainer() {}

    public static synchronized TrainingSkillManagerContainer getInstance() {
        if (instance == null) instance = new TrainingSkillManagerContainer();
        return instance;
    }

    public void addTrainingSkillManager(TrainingSkillManager trainingSkillManager) {
        trainingSkillManagers.add(trainingSkillManager);
    }

    public void removeTrainingSkillManager(TrainingSkillManager trainingSkillManager) {
        trainingSkillManagers.remove(trainingSkillManager);
    }

    public ArrayList<TrainingSkillManager> getTrainingSkillManagers() {
        return trainingSkillManagers;
    }

    /**
     * Sucht den TrainingSkillManager passend zu einer Training-ID.
     */
    public TrainingSkillManager getTrainingSkillManagerByTrainingId(int trainingId) {
        for (TrainingSkillManager manager : trainingSkillManagers) {
            // Wir vergleichen die ID des Trainings, für das dieser Manager zuständig ist
            if (manager.getTrainingId() == trainingId) {
                return manager;
            }
        }
        return null; // Nichts gefunden
    }
}