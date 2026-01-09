package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TrainingManager {

    private final int employeeId;
    private final List<TrainingHistoryEntry> trainingHistory = new ArrayList<>();

    public enum Status {
        OPEN,
        DONE
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

    public int getEmployeeId() {
        return employeeId;
    }

    public List<TrainingHistoryEntry> getTrainingHistory() {
        return Collections.unmodifiableList(new ArrayList<>(trainingHistory));
    }

    public List<TrainingHistoryEntry> getOpenTrainings() {
        List<TrainingHistoryEntry> open = new ArrayList<>();
        for (TrainingHistoryEntry e : trainingHistory) {
            if (e.isOpen()) {
                open.add(e);
            }
        }
        return open;
    }

    public List<TrainingHistoryEntry> getDoneTrainings() {
        List<TrainingHistoryEntry> done = new ArrayList<>();
        for (TrainingHistoryEntry e : trainingHistory) {
            if (e.isDone()) {
                done.add(e);
            }
        }
        return done;
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
    }

    /**
     * Used when loading from DB.
     */
    public void setTrainingHistory(List<TrainingHistoryEntry> entries) {
        trainingHistory.clear();
        if (entries != null) {
            trainingHistory.addAll(entries);
        }
    }
}
