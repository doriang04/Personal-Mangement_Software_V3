package model;

import java.time.LocalDate;
import java.util.List;

public interface IRoleManager {

    int getId();

    void setId(int id);

    List<RoleManager.RoleHistoryEntry> getRoleHistory();

    void setRoleHistory(List<RoleManager.RoleHistoryEntry> roleHistory);

    RoleManager.RoleHistoryEntry getActiveRole();

    void setActiveRole(RoleManager.RoleHistoryEntry activeRole);

    void addRole(RoleManager.RoleHistoryEntry role, LocalDate date);
}

