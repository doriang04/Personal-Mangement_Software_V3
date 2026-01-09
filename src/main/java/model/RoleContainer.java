package model;

import java.util.ArrayList;

public class RoleContainer {

    private static RoleContainer instance;
    private ArrayList<Role> roles = new ArrayList<>();

    private RoleContainer() {}

    public static synchronized RoleContainer getInstance() {
        if (instance == null) instance = new RoleContainer();
        return instance;
    }

    public void addRole(Role role) { roles.add(role); }

    public void removeRole(Role role) {
        roles.remove(role);
    }

    public ArrayList<Role> getRoles() {
        return roles;
    }

}
