package model;

import java.util.ArrayList;
import java.util.Date;

public class TrainingManager {
    private Employee employee;
    private int id;
    private ArrayList<TrainingEntry> openTrainings;   // Tupel[String trainingId, Date assigningDate]
    private ArrayList<TrainingEntry> doneTrainings;   // Tupel[String trainingId, Date completionDate]

    public TrainingManager(Employee emp) {
        this.openTrainings = new ArrayList<>();
        this.doneTrainings = new ArrayList<>();
        this.employee = emp;
        this.id = emp.getId();
    }
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
   
    public ArrayList<TrainingEntry> getOpenTrainings(Employee emp){
        return openTrainings;
    }

    public void assinTraining(Employee emp, TrainingEntry trainingEntry){
        openTrainings.add(trainingEntry);
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

    public void addTraining(String trainingId, Date date) {
        openTrainings.add(new TrainingEntry(trainingId, date));
    }

    public void completeTraining(String trainingId, Date completionDate) {
        TrainingEntry entry = openTrainings.stream()
                .filter(t -> t.getTrainingId().equals(trainingId))
                .findFirst()
                .orElseThrow();

        openTrainings.remove(entry);
        doneTrainings.add(new TrainingEntry(trainingId, completionDate));
    }

}
