package model; // TODO move this to core package?

import core.SessionManager;

public class ServiceLocator {

    public static RoleManagerContainer getRoleManagerContainer() {
        return RoleManagerContainer.getInstance();
    }

    public static EmployeeManagerContainer getEmployeeManagerContainer() {
        return EmployeeManagerContainer.getInstance();
    }

    public static TeamManagerContainer getTeamManagerContainer() {
        return TeamManagerContainer.getInstance();
    }

    public static CompanyContainer getCompanyContainer() {
        return CompanyContainer.getInstance();
    }

    public static SkillManagerContainer getSkillManagerContainer() {
        return SkillManagerContainer.getInstance();
    }

    public static TrainingManagerContainer getTrainingManagerContainer() {
        return TrainingManagerContainer.getInstance();
    }

    public static SessionManager getSessionManager() {
        return SessionManager.getInstance();
    }
}