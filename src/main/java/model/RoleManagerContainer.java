package model;

import java.util.ArrayList;
import java.util.List;

public class RoleManagerContainer {
    private static RoleManagerContainer instance;
    private List<RoleManager> roleManagers = new ArrayList<>();

    private RoleManagerContainer() {}

    public static synchronized RoleManagerContainer getInstance() {
        if (instance == null) instance = new RoleManagerContainer();
        return instance;
    }

    public void addRoleManager(RoleManager rm) {
        this.roleManagers.add(rm);
    }

    public List<RoleManager> getRoleManagers() {
        return roleManagers;
    }
}
