package model;

import java.time.LocalDate;
import java.util.ArrayList;

public class RoleManager {

    private int employeeId; // corresponds to role_history.employee_id / employees.id  // war mal final
    private final ArrayList<RoleHistoryEntry> roleHistory = new ArrayList<>();

    public  int getId() {
        return employeeId;
    }

    public void addRoleHistoryEntry(RoleHistoryEntry entry) {
        roleHistory.add(entry);
    }

    public static class RoleHistoryEntry {
        private int historyId;
        private int roleId;
        private LocalDate acquireDate;
        private LocalDate endDate;

        public RoleHistoryEntry(int historyId, int roleId, LocalDate acquireDate, LocalDate endDate) {
            this.historyId = historyId;
            this.roleId = roleId;
            this.acquireDate = acquireDate;
            this.endDate = endDate;
        }

        public RoleHistoryEntry(int roleId, LocalDate acquireDate, LocalDate endDate) {
            this(0, roleId, acquireDate, endDate);
        }

        public RoleHistoryEntry() {

        }

        public int getHistoryId() {
            return historyId;
        }

        public void setHistoryId(int historyId) {
            this.historyId = historyId;
        }

        public int getRoleId() {
            return roleId;
        }

        public void setRoleId(int roleId) {
            this.roleId = roleId;
        }

        public LocalDate getAcquireDate() {
            return acquireDate;
        }

        public void setAcquireDate(LocalDate acquireDate) {
            this.acquireDate = acquireDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public boolean isActive() {
            return endDate == null;
        }

        @Override
        public String toString() {
            return "RoleHistoryEntry{" +
                    "historyId=" + historyId +
                    ", roleId=" + roleId +
                    ", assignedAt=" + acquireDate +
                    ", endedAt=" + endDate +
                    '}';
        }
    }

    public RoleManager(Employee employee) {
        this.employeeId = employee.getId();
    }

    public RoleManager() {
    }

    public void setRoleHistory(ArrayList<RoleHistoryEntry> entries) {
        roleHistory.clear();
        if (entries != null) {
            roleHistory.addAll(entries);
        }
    }

    public ArrayList<RoleHistoryEntry> getRoleHistory() {
        return new ArrayList<>(roleHistory);
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public Role getActiveRole() {
        RoleHistoryEntry activeEntry = null;

        // newest entry wins → iterate backwards
        for (int i = roleHistory.size() - 1; i >= 0; i--) {
            RoleHistoryEntry entry = roleHistory.get(i);
            if (entry.isActive()) {
                activeEntry = entry;
                break;
            }
        }

        if (activeEntry == null) {
            return null;
        }

        return ServiceLocator
                .getRoleContainer()
                .getRoleById(activeEntry.getRoleId());
    }

    public RoleHistoryEntry assignRole(int roleId, LocalDate assignedAt) {
        if (assignedAt == null) {
            throw new IllegalArgumentException("assignedAt must not be null");
        }

        // close existing active role
        for (int i = roleHistory.size() - 1; i >= 0; i--) {
            RoleHistoryEntry entry = roleHistory.get(i);
            if (entry.isActive()) {
                entry.setEndDate(assignedAt.minusDays(1));
                break;
            }
        }

        RoleHistoryEntry newEntry = new RoleHistoryEntry(roleId, assignedAt, null);
        roleHistory.add(newEntry);
        return newEntry;
    }

    public void endActiveRole(LocalDate endedAt) {
        if (endedAt == null) {
            throw new IllegalArgumentException("endedAt must not be null");
        }

        for (int i = roleHistory.size() - 1; i >= 0; i--) {
            RoleHistoryEntry entry = roleHistory.get(i);
            if (entry.isActive()) {
                if (endedAt.isBefore(entry.getAcquireDate())) {
                    throw new IllegalArgumentException("endedAt cannot be before assignedAt");
                }
                entry.setEndDate(endedAt);
                return;
            }
        }
    }

}
