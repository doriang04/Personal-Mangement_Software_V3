package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// TODO test this goofy ahh gpt code (was zu faul lowkey)

public class RoleManager  {

    private final int employeeId;               // corresponds to role_history.employee_id / employees.id
    private final List<RoleHistoryEntry> roleHistory = new ArrayList<>();
    private RoleHistoryEntry activeRole;        // latest entry where endDate == null

    /**
     * Represents one entry from role_history table.
     * historyId is nullable (0 if unknown / not persisted yet).
     */
    public static class RoleHistoryEntry {
        private int historyId;      // corresponds to role_history.id (0 if not set)
        private int roleId;         // corresponds to role_history.role_id
        private LocalDate assignedAt; // corresponds to role_history.assigned_at
        private LocalDate endedAt;    // corresponds to role_history.ended_at (nullable)

        public RoleHistoryEntry(int historyId, int roleId, LocalDate assignedAt, LocalDate endedAt) {
            this.historyId = historyId;
            this.roleId = roleId;
            this.assignedAt = assignedAt;
            this.endedAt = endedAt;
        }

        public RoleHistoryEntry(int roleId, LocalDate assignedAt, LocalDate endedAt) {
            this(0, roleId, assignedAt, endedAt);
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

        public LocalDate getAssignedAt() {
            return assignedAt;
        }

        public void setAssignedAt(LocalDate assignedAt) {
            this.assignedAt = assignedAt;
        }

        public LocalDate getEndedAt() {
            return endedAt;
        }

        public void setEndedAt(LocalDate endedAt) {
            this.endedAt = endedAt;
        }

        public boolean isActive() {
            return this.endedAt == null;
        }

        @Override
        public String toString() {
            return "RoleHistoryEntry{" +
                    "historyId=" + historyId +
                    ", roleId=" + roleId +
                    ", assignedAt=" + assignedAt +
                    ", endedAt=" + endedAt +
                    '}';
        }
    }

    public RoleManager(Employee employee) {
        this.employeeId = employee.getId();
        // roleHistory list starts empty; if you load persisted entries afterwards, call setRoleHistory(...)
        this.activeRole = null;
    }

    /**
     * If you already have persisted role history for the employee, load it here.
     * The list should be ordered by assignedAt ascending (oldest -> newest).
     */
    public void setRoleHistory(ArrayList<RoleHistoryEntry> entries) {
        roleHistory.clear();
        if (entries != null) {
            roleHistory.addAll(entries);
        }
        // determine active role (last entry with endedAt == null)
        this.activeRole = null;
        for (int i = roleHistory.size() - 1; i >= 0; i--) {
            RoleHistoryEntry e = roleHistory.get(i);
            if (e.getEndedAt() == null) {
                this.activeRole = e;
                break;
            }
        }
    }

    public ArrayList<RoleHistoryEntry> getRoleHistory() {
        return new ArrayList<>(roleHistory);
    }

    public Optional<RoleHistoryEntry> getActiveRole() {
        return Optional.ofNullable(activeRole);
    }

    public int getEmployeeId() {
        return employeeId;
    }

    /**
     * Assigns a new role starting on assignedAt.
     * Closes the previous active role (if any) by setting its endedAt to assignedAt.minusDays(1).
     * Then creates and activates a new RoleHistoryEntry with endedAt == null.
     */
    public RoleHistoryEntry assignRole(int roleId, LocalDate assignedAt) {
        if (assignedAt == null) {
            throw new IllegalArgumentException("assignedAt must not be null");
        }

        // close currently active role if exists
        if (activeRole != null) {
            // set end date to day before new assignment
            LocalDate endDate = assignedAt.minusDays(1);
            activeRole.setEndedAt(endDate);
        }

        RoleHistoryEntry newEntry = new RoleHistoryEntry(roleId, assignedAt, null);
        roleHistory.add(newEntry);
        activeRole = newEntry;
        return newEntry;
    }

    /**
     * Ends the active role on the given date. If there is no active role, does nothing.
     */
    public void endActiveRole(LocalDate endedAt) {
        if (activeRole == null) return;
        if (endedAt == null) throw new IllegalArgumentException("endedAt must not be null");
        // ensure endedAt is >= assignedAt; if not, adjust or throw
        if (endedAt.isBefore(activeRole.getAssignedAt())) {
            throw new IllegalArgumentException("endedAt cannot be before assignedAt of the active role");
        }
        activeRole.setEndedAt(endedAt);
        activeRole = null;
    }

    /**
     * Convenience: returns the currently active role id or -1 if none.
     */
    public int getCurrentRoleIdOrMinusOne() {
        return (activeRole != null) ? activeRole.getRoleId() : -1;
    }
}
