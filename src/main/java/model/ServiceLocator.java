package model; // TODO move this to core package?

import core.SessionManager;

public class ServiceLocator {

    public static RoleManagerContainer getRoleManagerContainer() {
        return RoleManagerContainer.getInstance();
    }

    public static EmployeeContainer getEmployeeContainer() {
        return EmployeeContainer.getInstance();
    }

    public static TeamContainer getTeamManagerContainer() {
        return TeamContainer.getInstance();
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

    public static RoleContainer getRoleContainer() {
        return RoleContainer.getInstance();
    }

    public static DepartmentContainer getDepartmentContainer() {
        return DepartmentContainer.getInstance();
    }

    public static TeamContainer getTeamContainer() {
        return TeamContainer.getInstance();
    }

    public static SkillContainer getSkillContainer() {
        return SkillContainer.getInstance();
    }

    public static TrainingContainer getTrainingContainer() {
        return TrainingContainer.getInstance();
    }

}