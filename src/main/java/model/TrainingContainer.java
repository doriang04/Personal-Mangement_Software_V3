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

    public void removeTraining(Training training) throws Exception {
        if (training.hasReferences()) throw new Exception("Not allowed to delete training, as it is referenced.");
        trainings.remove(training);
    }

    public ArrayList<Training> getTrainings() {
        return trainings;
    }

    public Training getTrainingById(int Trainingid) {
        for (Training Training: trainings) {
            if (Training.getId() == Trainingid) {
                return Training;
            }
        }
        return null;
    }

    public int getNextFreeId() {
        int i = 0;
        while (true) {
            if (getTrainingById(i) == null) return i;
            i++;
        }
    }
}
