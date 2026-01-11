package gui;

import model.ServiceLocator;
import core.SessionManager;
import gui.views.*;

// NEUE IMPORTS
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

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

        // NEU: Window-Listener registrieren, um das Schließen abzufangen
        initShutdownListener();
    }

    // NEU: Diese Methode kümmert sich um das sichere Beenden
    private void initShutdownListener() {
        // Sicherstellen, dass das Fenster nicht einfach zugeht (Redundant falls schon in MainWindow gesetzt, aber sicher ist sicher)
        this.mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        this.mainWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdownApplication();
            }
        });
    }

    // NEU: Zentrale Methode zum Beenden
    public void shutdownApplication() {
        System.out.println("🛑 Programm wird beendet... Speichere Daten.");

        // 1. Daten speichern
        database.DatabaseManager.getInstance().saveAllData();

        // 2. JVM beenden
        System.exit(0);
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
        // Du kannst hier auch direkt saveAllData aufrufen oder dich auf den Shutdown verlassen,
        // aber explizit ist besser:
        database.DatabaseManager.getInstance().saveAllData();
        sessionManager.logout();
        showLoginScreen();
    }

    private void buildNavigation(String role) { // TODO finish this method to include all needed navigation
        // --- Standard für alle ---
        mainWindow.addNavigationEntry("Dashboard", this::openDashboard);
        mainWindow.addNavigationEntry("Mitarbeiter suchen",
                () -> openTabOrFocus(new EmployeeSearchView(), true));
        mainWindow.addNavigationEntry("Mein Profil",
                () -> openTabOrFocus(new MyProfileView(), true));
        mainWindow.addNavigationEntry("Meine Schulungen",
                () -> openTabOrFocus(new MyTrainingsView(), true));

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

        if ("HR".equals(role) || "TEAM_LEAD".equals(role) || "ADMIN".equals(role)) {
            mainWindow.addNavigationEntry("Schulungsverwaltung",
                    () -> openTabOrFocus(new TrainingManagementView(), true));
        }

        if ("ADMIN".equals(role) || "HR".equals(role)) {
            mainWindow.addNavigationEntry("Personal verwalten (+/-)", () -> {
                openTabOrFocus(new gui.views.EmployeeManagementView(), true);
            });
        }

        mainWindow.addGlueToNav();
    }

    private void openDashboard() {
        openTabOrFocus(new DashboardView(), false);
    }

    private void openTabOrFocus(View view, boolean closable) {
        if (!mainWindow.selectTabIfExists(view)) mainWindow.openTab(view, closable);
    }


    /**
     * Öffnet das Profil eines Mitarbeiters in einem neuen Tab.
     * Wird von EmployeeSearchView per Doppelklick aufgerufen.
     */
    public void openEmployeeDetailTab(int employeeId) {
        // 1. View erstellen (ID übergeben)
        gui.views.EmployeeDetailView view = new gui.views.EmployeeDetailView(employeeId);

        // 2. WICHTIG: Tab im Hauptfenster öffnen! (DIESE ZEILE FEHLTE)
        mainWindow.openTab(view, true);
    }

    // Falls du komplexere Anforderungen hast (wie im originalen requestTabCreation): TODO is this needed?
    public void requestSpecificView(String viewIdentifier, Object payload) {
        // Hier könnte Logik stehen, die z.B. Daten lädt, bevor der Tab geöffnet wird
        // ...
    }
}