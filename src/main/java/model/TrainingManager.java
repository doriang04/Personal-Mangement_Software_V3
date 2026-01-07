package model;

import java.util.ArrayList;
import java.util.Date;
import java.sql.*;


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

    public void loadTrainingsForEmployee(Connection connection) throws SQLException {
        openTrainings.clear();
        doneTrainings.clear();

        String sqlOpen = "SELECT training_id, assigning_date FROM open_trainings WHERE employee_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sqlOpen)) {
            ps.setInt(1, this.id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String trainingId = rs.getString("training_id");
                    Date assigningDate = new Date(rs.getTimestamp("assigning_date").getTime());
                    openTrainings.add(new TrainingEntry(trainingId, assigningDate));
                }
            }
        }

        String sqlDone = "SELECT training_id, completion_date FROM done_trainings WHERE employee_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sqlDone)) {
            ps.setInt(1, this.id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String trainingId = rs.getString("training_id");
                    Date completionDate = new Date(rs.getTimestamp("completion_date").getTime());
                    doneTrainings.add(new TrainingEntry(trainingId, completionDate));
                }
            }
        }
    }

}
