package gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import core.ServiceLocator;
import core.SessionManager;
import gui.views.ConfigurationView;
import gui.views.DashboardView;
import gui.views.EmployeeManagementView;
import gui.views.EmployeeSearchView;
import gui.views.LoginView;
import gui.views.MyProfileView;
import gui.views.MyTrainingsView;
import gui.views.TrainingManagementView;
import gui.views.View;


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

        initShutdownListener();
    }

    private void initShutdownListener() {
        mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdownApplication();
            }
        });

    }

    // NEU: Zentrale Methode zum Beenden
    public void shutdownApplication() {
        System.out.println("🛑 Programm wird beendet...");

        database.DatabaseManager.getInstance().saveAllDataOnce();
        ServiceLocator.getSessionManager().logout();

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

        mainWindow.setupMainLayout(name, role, _ -> handleLogout());
        buildNavigation(role);
        openDashboard();
    }

    private void handleLogout() {
    // Sicherheitsabfrage (Optional)
    int confirm = JOptionPane.showConfirmDialog(mainWindow, 
        "Möchten Sie sich wirklich ausloggen?", "Logout", JOptionPane.YES_NO_OPTION);
    
    if (confirm == JOptionPane.YES_OPTION) {
        database.DatabaseManager.getInstance().saveAllData();
        sessionManager.logout();
        showLoginScreen(); 
    }
}
    
private void handleLogout(java.awt.event.ActionEvent e) 
{
    handleLogout();
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
        }

        if ("TEAM_LEAD".equals(role)) {
            mainWindow.addNavigationSection("Teamleitung");
            // Beispiel:
            // mainWindow.addNavigationEntry("Mein Team",
            //      () -> openTabOrFocus(TeamOverviewView.class, TeamOverviewView::new, true));
        }

        if ("ADMIN".equals(role)) {
            mainWindow.addNavigationSection("Administration");
            mainWindow.addNavigationEntry("Daten Konfiguration",
                    () -> openTabOrFocus(new ConfigurationView(), true));
            mainWindow.addNavigationEntry("Systemsteuerung ⚙️",
                    () -> openTabOrFocus(new gui.views.AdminControlPanelView(), true));
            // mainWindow.addNavigationEntry("Einstellungen", ...);
        }

        if ("HR".equals(role) || "TEAM_LEAD".equals(role) || "ADMIN".equals(role)) {
            mainWindow.addNavigationEntry("Schulungsverwaltung",
                    () -> openTabOrFocus(new TrainingManagementView(), true));
        }

        if ("ADMIN".equals(role) || "HR".equals(role)) {
            mainWindow.addNavigationEntry("Personal verwalten (+/-)",
                    () -> openTabOrFocus(new EmployeeManagementView(), true));
        }

        mainWindow.addGlueToNav(this::handleLogout);
    }

    private void openDashboard() {
        openTabOrFocus(new DashboardView(), false);
    }

    public void openTabOrFocus(View view, boolean closable) {
        if (!mainWindow.selectTabIfExists(view)) mainWindow.openTab(view, closable);
    }

    /**
     * Öffnet das Profil eines Mitarbeiters in einem neuen Tab.
     * Wird von EmployeeSearchView per Doppelklick aufgerufen.
     */
    public void openEmployeeDetailTab(int employeeId) { // TODO usage ändern, sodass es via openTabOrFocus läuft
        // 1. View erstellen (ID übergeben)
        gui.views.EmployeeDetailView view = new gui.views.EmployeeDetailView(employeeId);

        // 2. WICHTIG: Tab im Hauptfenster öffnen! (DIESE ZEILE FEHLTE)
        mainWindow.openTab(view, true);
    }

    public void updateMainWindow() {
        mainWindow.updateSelf();
    }
}