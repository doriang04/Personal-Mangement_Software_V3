package model;

import java.util.ArrayList;
import java.util.Date;

public class RoleManager {

    // Tupel[roleId, startDate, endDate] → hier als einfache Hilfsklasse
    public static class RoleHistoryEntry {
        private int roleId;
        private Date startDate;
        private Date endDate;

        public RoleHistoryEntry(int roleId, Date startDate, Date endDate) {
            this.roleId = roleId;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public int getRoleId() {
            return roleId;
        }

        public Date getStartDate() {
            return startDate;
        }

        public Date getEndDate() {
            return endDate;
        }
    }

    private ArrayList<RoleHistoryEntry> roleHistory;
    private Role activeRole;

    public ArrayList<RoleHistoryEntry> getRoleHistory() {
        return roleHistory;
    }

    public void setRoleHistory(ArrayList<RoleHistoryEntry> roleHistory) {
        this.roleHistory = roleHistory;
    }

    public Role getActiveRole() {
        return activeRole;
    }

    public void setActiveRole(Role activeRole) {
        this.activeRole = activeRole;
    }

    public void addRole(Role role, Date startDate, Date endDate) {
        if (roleHistory == null) {
            roleHistory = new ArrayList<>();
        }
        roleHistory.add(new RoleHistoryEntry(role.getId(), startDate, endDate));
    }
}
