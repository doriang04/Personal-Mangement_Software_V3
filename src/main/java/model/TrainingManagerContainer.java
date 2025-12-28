package model;

import java.util.ArrayList;
import java.util.List;

// Training Manager Container
public class TrainingManagerContainer {
    private static TrainingManagerContainer instance;
    private List<TrainingManager> trainingManagers = new ArrayList<>();

    private TrainingManagerContainer() { }

    public static synchronized TrainingManagerContainer getInstance() {
        if (instance == null) instance = new TrainingManagerContainer();
        return instance;
    }

    public void addTrainingManager(TrainingManager tm) { trainingManagers.add(tm); }
    public List<TrainingManager> getTrainingManagers() { return new ArrayList<>(trainingManagers); }
}
