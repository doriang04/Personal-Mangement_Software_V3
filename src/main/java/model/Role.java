package model;

import core.ServiceLocator;

public class Role {

    private int id;
    private String name;
    private String description;
    private String systemPermission;

    public Role() {}

    public Role(String name, String description, String systemPermission) {
        this.id = ServiceLocator.getRoleContainer().getNextFreeId();
        this.name = name;
        this.description = description;
        this.systemPermission = systemPermission;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSystemPermission() {
        return systemPermission;
    }

    public void setSystemPermission(String systemPermission) {
        this.systemPermission = systemPermission;
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", systemPermission='" + systemPermission + '\'' +
                '}';
    }
}
