package model;

import java.util.ArrayList;
import java.util.List;

// Team Manager Container
public class TeamManagerContainer {
    private static TeamManagerContainer instance;
    private List<Team> teams = new ArrayList<>();

    private TeamManagerContainer() {}

    public static synchronized TeamManagerContainer getInstance() {
        if (instance == null) instance = new TeamManagerContainer();
        return instance;
    }

    public void addTeam(Team team) { teams.add(team); }
}
