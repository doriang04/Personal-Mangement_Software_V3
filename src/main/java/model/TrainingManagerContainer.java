package model;

import java.util.ArrayList;
import java.util.List;

public class TrainingManagerContainer {
    private static TrainingManagerContainer instance;
    private ArrayList<TrainingManager> trainingManagers = new ArrayList<>();

    private TrainingManagerContainer() { }

    public static synchronized TrainingManagerContainer getInstance() {
        if (instance == null) instance = new TrainingManagerContainer();
        return instance;
    }

    public void addTrainingManager(TrainingManager tm) { trainingManagers.add(tm); }
    public void removeTrainingManager(TrainingManager tm) { trainingManagers.remove(tm); }
    public ArrayList<TrainingManager> getTrainingManagers() { return new ArrayList<>(trainingManagers); }

    public TrainingManager getTrainingManagerById(int TrainingManagerid) {
        for (TrainingManager TrainingManager: trainingManagers) {
            if (TrainingManager.getId() == TrainingManagerid) {
                return TrainingManager;
            }
        }
        return null;
    }
}
