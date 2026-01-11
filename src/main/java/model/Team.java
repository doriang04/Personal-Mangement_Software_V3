package model;

import core.ServiceLocator;

public class Team {

    private int id;
    private String name;
    private int departmentId;
    // TODO: Teams können Teams haben (Hierarchie)
    // TODO: note for above: leck ei
    
    public Team(String name, int departmentId) {
        this.id = ServiceLocator.getTeamContainer().getNextFreeId();
        this.name = name;
        this.departmentId = departmentId;
    }

    public Team() {
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
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
}

