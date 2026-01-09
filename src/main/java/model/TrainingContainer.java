package model;

import java.util.ArrayList;

public class TrainingContainer {

    private static TrainingContainer instance;
    private ArrayList<Training> trainings = new ArrayList<>();

    private TrainingContainer() {}

    public static synchronized TrainingContainer getInstance() {
        if (instance == null) instance = new TrainingContainer();
        return instance;
    }

    public void addTraining(Training training) {
        trainings.add(training);
    }

    public void removeTraining(Training training) {
        trainings.remove(training);
    }

    public ArrayList<Training> getTrainings() {
        return trainings;
    }

}
