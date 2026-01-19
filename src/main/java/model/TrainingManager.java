package model;

import core.ServiceLocator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

public class TrainingManager {

    private int employeeId;
    private final ArrayList<TrainingHistoryEntry> trainingHistory = new ArrayList<>();

    public int getId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public ArrayList<TrainingHistoryEntry> getTrainingHistory() {
        return trainingHistory;
    }

    public enum Status {
        OPEN,
        DONE
    }

    public static class TrainingHistoryEntry {
        private int historyId;
        private int trainingId;
        private Status status;
        private LocalDate assignedAt;
        private LocalDate completedAt;

        public TrainingHistoryEntry(int historyId, int trainingId, Status status, LocalDate assignedAt, LocalDate completedAt) {
            this.historyId = historyId;
            this.trainingId = trainingId;
            this.status = status;
            this.assignedAt = assignedAt;
            this.completedAt = completedAt;
        }

        public TrainingHistoryEntry(int trainingId, LocalDate assignedAt) {
            this(0, trainingId, Status.OPEN, assignedAt, null);
        }

        public TrainingHistoryEntry() {}

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

    public TrainingManager() {}

    public TrainingHistoryEntry assignTraining(int trainingId, LocalDate assignedAt) {
        TrainingHistoryEntry entry = new TrainingHistoryEntry(trainingId, assignedAt);
        trainingHistory.add(entry);
        return entry;
    }

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
                completionDate
        );
    }

    public void setTrainingHistory(ArrayList<TrainingHistoryEntry> entries) {
        trainingHistory.clear();
        if (entries != null) {
            trainingHistory.addAll(entries);
        }
    }

    public boolean hasReferences() {
        for (Employee employee: ServiceLocator.getEmployeeContainer().getEmployees()) {
            if (employee.getId() == employeeId) return true;
        }
        return false;
    }
}
