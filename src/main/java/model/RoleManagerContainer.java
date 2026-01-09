package model;

import java.util.ArrayList;
import java.util.List;

public class RoleManagerContainer {
    private static RoleManagerContainer instance;
    private ArrayList<RoleManager> roleManagers = new ArrayList<>();

    private RoleManagerContainer() {}

    public static synchronized RoleManagerContainer getInstance() {
        if (instance == null) instance = new RoleManagerContainer();
        return instance;
    }

    public void addRoleManager(RoleManager rm) {
        this.roleManagers.add(rm);
    }

    public void removeRoleManager(RoleManager rm) {
        this.roleManagers.remove(rm);
    }

    public ArrayList<RoleManager> getRoleManagers() {
        return roleManagers;
    }
}
