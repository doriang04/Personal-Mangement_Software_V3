package model;

import core.ServiceLocator;

import java.time.LocalDate;
import java.util.*;
import java.util.ArrayList;

public class TrainingManager {

    private int employeeId; // was final
    private final ArrayList<TrainingHistoryEntry> trainingHistory = new ArrayList<>();

    public int getId() {
        return employeeId;
    }

    public enum Status {
        OPEN,
        DONE
    }
    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public static class TrainingHistoryEntry {
        private int historyId;          // training_history.id
        private int trainingId;         // training_history.training_id
        private Status status;          // OPEN / DONE
        private LocalDate assignedAt;   // training_history.assigned_at
        private LocalDate completedAt;  // training_history.completed_at (nullable)

        public TrainingHistoryEntry(
                int historyId,
                int trainingId,
                Status status,
                LocalDate assignedAt,
                LocalDate completedAt
        ) {
            this.historyId = historyId;
            this.trainingId = trainingId;
            this.status = status;
            this.assignedAt = assignedAt;
            this.completedAt = completedAt;
        }

        public TrainingHistoryEntry(int trainingId, LocalDate assignedAt) {
            this(0, trainingId, Status.OPEN, assignedAt, null);
        }

        public TrainingHistoryEntry() {

        }

        public int getHistoryId() {
            return historyId;
        }

        public void setHistoryId(int historyId) {
            this.historyId = historyId;
        }

        public int getTrainingId() {
            return trainingId;
        }

        public Status getStatus() {
            return status;
        }

        public LocalDate getAssignedAt() {
            return assignedAt;
        }

        public LocalDate getCompletedAt() {
            return completedAt;
        }

        public boolean isOpen() {
            return status == Status.OPEN;
        }

        public boolean isDone() {
            return status == Status.DONE;
        }

        private void markDone(LocalDate completionDate) {
            this.status = Status.DONE;
            this.completedAt = completionDate;
        }
    }

    public TrainingManager(Employee employee) {
        this.employeeId = employee.getId();
    }

    public TrainingManager() {

    }

    public int getEmployeeId() {
        return employeeId;
    }

    public ArrayList<TrainingHistoryEntry> getTrainingHistory() {
        return trainingHistory;
    }

    /**
     * Assigns a training to the employee.
     */
    public TrainingHistoryEntry assignTraining(int trainingId, LocalDate assignedAt) {
        TrainingHistoryEntry entry = new TrainingHistoryEntry(trainingId, assignedAt);
        trainingHistory.add(entry);
        return entry;
    }

    /**
     * Completes an open training.
     */
    public void completeTraining(int trainingId, LocalDate completionDate) {
        Optional<TrainingHistoryEntry> entryOpt = trainingHistory.stream()
                .filter(e -> e.getTrainingId() == trainingId && e.isOpen())
                .findFirst();

        TrainingHistoryEntry entry = entryOpt.orElseThrow(
                () -> new IllegalStateException("No open training found for id " + trainingId)
        );

        entry.markDone(completionDate);
        Employee employee = ServiceLocator.getEmployeeContainer().getEmployeeById(employeeId);
        employee.getSkillManager().addSkillsFromTraining(
                ServiceLocator.getTrainingContainer().getTrainingById(trainingId),
                completionDate);
    }

    /**
     * Used when loading from DB.
     */
    public void setTrainingHistory(ArrayList<TrainingHistoryEntry> entries) {
        trainingHistory.clear();
        if (entries != null) {
            trainingHistory.addAll(entries);
        }
    }

    public boolean hasReferences() {
        // TODO write out this method
        return false;
    }
}
