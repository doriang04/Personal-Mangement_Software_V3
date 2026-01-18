package model;

import java.util.ArrayList;
import java.util.List;

public class TeamContainer {
    private static TeamContainer instance;
    private ArrayList<Team> teams = new ArrayList<>();

    private TeamContainer() {}

    public static synchronized TeamContainer getInstance() {
        if (instance == null) instance = new TeamContainer();
        return instance;
    }

    public void addTeam(Team team) { teams.add(team); }

    public void removeTeam(Team team) throws Exception {
        if (team.hasReferences()) throw new Exception("Das Unternehmen darf nicht gelöscht werden, da es referenziert wird.");
        teams.remove(team);
    }

    public ArrayList<Team> getTeams() {
        return teams;
    }

    public Team getTeamById(int id) {
        for (Team t : teams) {
            if (t.getId() == id) return t;
        }
        return null;
    }

    public int getNextFreeId() {
        int i = 0;
        while (true) {
            if (getTeamById(i) == null) return i;
            i++;
        }
    }
}
