package model;

import java.util.ArrayList;
import java.util.Date;

public class TrainingManager {

    public static class TrainingEntry {
        private String trainingId;
        private Date date;

        public TrainingEntry(String trainingId, Date date) {
            this.trainingId = trainingId;
            this.date = date;
        }

        public String getTrainingId() {
            return trainingId;
        }

        public Date getDate() {
            return date;
        }
    }

    private ArrayList<TrainingEntry> openTrainings;   // Tupel[String trainingId, Date assigningDate]
    private ArrayList<TrainingEntry> doneTrainings;   // Tupel[String trainingId, Date completionDate]

    public ArrayList<TrainingEntry> getOpenTrainings() {
        return openTrainings;
    }

    public void setOpenTrainings(ArrayList<TrainingEntry> openTrainings) {
        this.openTrainings = openTrainings;
    }

    public ArrayList<TrainingEntry> getDoneTrainings() {
        return doneTrainings;
    }

    public void setDoneTrainings(ArrayList<TrainingEntry> doneTrainings) {
        this.doneTrainings = doneTrainings;
    }
}
