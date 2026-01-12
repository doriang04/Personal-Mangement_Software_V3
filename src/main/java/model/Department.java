package model;

import core.ServiceLocator;

public class Department {

    private int id;
    private String name;
    private int companyId;

    public Department(String name, int companyId) {
        this.id = ServiceLocator.getDepartmentContainer().getNextFreeId();
        this.name = name;
        this.companyId = companyId;
    }

    public Department() {

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

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String toString() {
        return getId() + "_(" + getName() + ", compId:" + getCompanyId() + ")";
    }

    public boolean hasReferences() {
        for (Team team: ServiceLocator.getTeamContainer().getTeams()) {
            if (team.getDepartmentId() == getId()) return true;
        }
        return false;
    }
}
