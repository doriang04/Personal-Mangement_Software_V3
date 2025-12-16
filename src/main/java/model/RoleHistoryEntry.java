package model;

import java.time.LocalDate;

public class RoleHistoryEntry {

    private int roleId;
    private LocalDate startDate;
    private LocalDate endDate;

    public RoleHistoryEntry(int roleId, LocalDate startDate, LocalDate endDate) {
        this.roleId = roleId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getRoleId() {
        return roleId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
