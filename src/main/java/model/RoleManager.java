package model;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoleManager {

    private List<RoleHistoryEntry> roleHistory = new ArrayList<>();
    private Role activeRole;
    private int id;

    public List<RoleHistoryEntry> getRoleHistory() {
        return new ArrayList<>(roleHistory);
    }

    public void setRoleHistory(List<RoleHistoryEntry> roleHistory) {
        this.roleHistory = new ArrayList<>(roleHistory);
    }

    public Role getActiveRole() {
        return activeRole;
    }

    public int getRolemanagerid() {
        return id;
    }

    public void setRolemanagerId(int id) {
        this.id = id;
    }

    public void setActiveRole(Role activeRole) {
        this.activeRole = activeRole;
    }

    // neue Rolle ab Datum setzen (alte ggf. beenden)
    public void addRole(Role role, LocalDate date) {
        // alte aktive Rolle schließen
        if (activeRole != null) {
            for (RoleHistoryEntry entry : roleHistory) {
                if (entry.getRoleId() == (activeRole).getId() && entry.getEndDate() == null) {
                    entry.setEndDate(date.minusDays(1));
                    break;
                }
            }
        }
        // neue aktive Rolle setzen
        this.activeRole = role;
        // neuen Verlaufseintrag hinzufügen
        roleHistory.add(new RoleHistoryEntry(role.getId(), date, null));
    }
}
