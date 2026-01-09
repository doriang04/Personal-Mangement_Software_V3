package model;

import java.util.ArrayList;
import java.util.Date;

public interface ITrainingManager {

    int getId();

    void loadTrainingsForEmployee(java.sql.Connection connection) throws java.sql.SQLException;

    ArrayList<TrainingManager.TrainingEntry> getOpenTrainings(Employee emp);

    void setOpenTrainings(ArrayList<TrainingManager.TrainingEntry> openTrainings);

    ArrayList<TrainingManager.TrainingEntry> getDoneTrainings();

    void setDoneTrainings(ArrayList<TrainingManager.TrainingEntry> doneTrainings);

    void addTraining(String trainingId, Date date);

    void completeTraining(String trainingId, Date completionDate);
}

