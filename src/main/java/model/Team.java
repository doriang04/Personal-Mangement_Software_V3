package model;

public class Team {

    private int departmentId;
    private int teamId;
    private String teamName;
    // TODO: Teams können Teams haben (Hierarchie)
    
    public Team(int departmentId, int teamId, String teamName) {
        this.departmentId = departmentId;
        this.teamId = teamId;
        this.teamName = teamName;
    }
    public Team() {
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
}

