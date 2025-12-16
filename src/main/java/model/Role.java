package model;

public class Role {

    private int id;
    private String name;
    private String description;
    private String permission;

    public Role(int id, String name, String description, String permission) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.permission = permission;
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

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
