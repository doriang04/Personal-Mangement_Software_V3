package model;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoleManager {

    private List<RoleHistoryEntry> roleHistory;
    private RoleHistoryEntry activeRole;
    private int id;
    private Employee employee;

    public static class RoleHistoryEntry {
        private int roleId;
        private LocalDate startDate;
        private LocalDate endDate;
        
        public RoleHistoryEntry(int roleId, LocalDate startDate, LocalDate endDate){
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

    public RoleManager(Employee employee) {
        this.id = employee.getId();
        this.roleHistory = new ArrayList<>(roleHistory);
        if (this.roleHistory.isEmpty()) {
            this.activeRole = null; // oder Optional
        } else {
            this.activeRole = this.roleHistory.get(this.roleHistory.size() - 1);
        }
    }

    public List<RoleHistoryEntry> getRoleHistory() {
        return new ArrayList<>(roleHistory);
    }

    public void setRoleHistory(List<RoleHistoryEntry> roleHistory) {
        this.roleHistory = new ArrayList<>(roleHistory);
    }

    public RoleHistoryEntry getActiveRole() {
        return activeRole;
    }

    public int getRolemanagerid() {
        return id;
    }

    public void setRolemanagerId(int id) {
        this.id = id;
    }

    public void setActiveRole(RoleHistoryEntry activeRole) {
        this.activeRole = activeRole;
    }

    // neue Rolle ab Datum setzen (alte ggf. beenden)
    public void addRole(RoleHistoryEntry role, LocalDate date) {
        // alte aktive Rolle schließen
        if (activeRole != null) {
            for (RoleHistoryEntry entry : roleHistory) {
                if (entry.getRoleId() == (activeRole).getRoleId() && entry.getEndDate() == null) {
                    entry.setEndDate(date.minusDays(1));
                    break;
                }
            }
        }
        // neue aktive Rolle setzen
        this.activeRole = role;
        // neuen Verlaufseintrag hinzufügen
        roleHistory.add(new RoleHistoryEntry(role.getRoleId(), date, null));
    }
}
