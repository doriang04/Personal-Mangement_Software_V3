package gui;

import model.ServiceLocator;
import core.SessionManager;
import gui.views.*;

public class UIController {

    private static UIController instance;
    private final MainWindow mainWindow;
    private final SessionManager sessionManager;

    public static synchronized UIController getInstance() {
        if (instance == null) instance = new UIController();
        return instance;
    }

    private UIController() {
        this.mainWindow = MainWindow.getInstance();
        this.sessionManager = ServiceLocator.getSessionManager();
    }

    public void startApplication() {
        showLoginScreen();
    }

    private void showLoginScreen() {
        LoginView loginView = new LoginView();
        mainWindow.showSingleView(loginView, loginView.getViewTabTitle(), 450, 550);
    }

    public void onLoginSuccess() {
        String name = sessionManager.getUserFirstNameAndLastName();
        String role = sessionManager.getUserPermission();

        mainWindow.setupMainLayout(name, role, _ -> logout());
        buildNavigation(role);
        openDashboard();
    }

    public void logout() {
        sessionManager.logout();
        showLoginScreen();
    }

    private void buildNavigation(String role) { // TODO finish this method to include all needed navigation
        // --- Standard für alle ---
        mainWindow.addNavigationEntry("Dashboard", this::openDashboard);
        mainWindow.addNavigationEntry("Mitarbeiter suchen",
                () -> openTabOrFocus(new EmployeeSearchView(), true));
        mainWindow.addNavigationEntry("Mein Profil",
                () -> openTabOrFocus(new EmployeeDetailView("Eigenes Profil"), true));

        mainWindow.addSpacerToNav();

        // --- Rollenspezifisch ---
        if ("HR".equals(role)) {
            mainWindow.addNavigationSection("HR Management");
            mainWindow.addNavigationEntry("Neuen Mitarbeiter anlegen",
                    () -> openTabOrFocus(new EmployeeDetailView(), true));
        }

        if ("TEAM_LEAD".equals(role)) {
            mainWindow.addNavigationSection("Teamleitung");
            // Beispiel:
            // mainWindow.addNavigationEntry("Mein Team", "icons/team.png",
            //      () -> openTabOrFocus(TeamOverviewView.class, TeamOverviewView::new, true));
        }

        if ("ADMIN".equals(role)) {
            mainWindow.addNavigationSection("Administration");
            // mainWindow.addNavigationEntry("Einstellungen", ...);
        }

        mainWindow.addGlueToNav();
    }

    private void openDashboard() {
        openTabOrFocus(new DashboardView(), false);
    }

    private void openTabOrFocus(View view, boolean closable) {
        if (!mainWindow.selectTabIfExists(view)) mainWindow.openTab(view, closable);
    }

    // Falls du komplexere Anforderungen hast (wie im originalen requestTabCreation): TODO is this needed?
    public void requestSpecificView(String viewIdentifier, Object payload) {
        // Hier könnte Logik stehen, die z.B. Daten lädt, bevor der Tab geöffnet wird
        // ...
    }
}