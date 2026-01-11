package gui.views;

import core.ServiceLocator;
import core.SessionManager;
import model.*;
import model.TrainingManager.TrainingHistoryEntry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TrainingManagementView extends JPanel implements View {

    private final SessionManager sessionManager;
    private final String currentUserRole;
    private final Employee currentUser;

    private JTabbedPane innerTabbedPane;
    private JTable teamProgressTable;
    private DefaultTableModel teamProgressModel;

    // Katalog Komponenten
    private JTable trainingCatalogTable;
    private DefaultTableModel trainingCatalogModel;

    public TrainingManagementView() {
        this.sessionManager = ServiceLocator.getSessionManager();

        // Rolle sicher abrufen
        String role = sessionManager.getUserPermission();
        this.currentUserRole = (role != null) ? role : "GUEST";

        // User Hack (wie besprochen)
        this.currentUser = findCurrentUserHack();

        setLayout(new BorderLayout());
        initUI();
    }

    private Employee findCurrentUserHack() {
        String fullName = sessionManager.getUserFirstNameAndLastName();
        if (fullName == null) return null;
        // Einfache Suche (Vorsicht bei gleichen Namen)
        for (Employee e : ServiceLocator.getEmployeeContainer().getEmployees()) {
            if ((e.getFirstName() + e.getLastName()).equalsIgnoreCase(fullName.replace(" ", ""))) {
                return e;
            }
        }
        return null; // Fallback
    }

    private void initUI() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.add(new JLabel("Schulungsverwaltung - Eingeloggt als: " + currentUserRole));
        add(header, BorderLayout.NORTH);

        innerTabbedPane = new JTabbedPane();

        // 1. Tab: Schulungskatalog (Immer sichtbar)
        innerTabbedPane.addTab("Schulungskatalog", createCatalogPanel());

        // 2. Tab: Team-Fortschritt
        // Logik: Jeder mit Personalverantwortung darf das sehen
        boolean isManager = isPrivilegedUser();

        if (isManager) {
            innerTabbedPane.addTab("Team-Fortschritt", createTeamProgressPanel());
        }

        add(innerTabbedPane, BorderLayout.CENTER);
    }

    private boolean isPrivilegedUser() {
        if (currentUserRole == null) return false;
        String r = currentUserRole.toUpperCase();
        return r.contains("ADMIN") || r.contains("HR") || r.contains("CEO") || r.contains("LEAD") || r.contains("MANAGER");
    }

    // --- PANELS ---

    private JPanel createCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columns = {"ID", "Titel", "Beschreibung", "Dauer (h)"};
        trainingCatalogModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        trainingCatalogTable = new JTable(trainingCatalogModel);
        panel.add(new JScrollPane(trainingCatalogTable), BorderLayout.CENTER);

        loadCatalogData();
        return panel;
    }

    private JPanel createTeamProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Tabelle
        String[] columns = {"Mitarbeiter", "Schulung", "Status", "Datum / Info"};
        teamProgressModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        teamProgressTable = new JTable(teamProgressModel);
        panel.add(new JScrollPane(teamProgressTable), BorderLayout.CENTER);

        // Button Leiste
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAssign = new JButton("Schulung zuweisen");

        btnAssign.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            // Öffnet den Dialog und lädt danach die Tabelle neu
            new AssignTrainingDialog(parentWindow, this::loadTeamProgressData).setVisible(true);
        });

        buttonPanel.add(btnAssign);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        loadTeamProgressData();
        return panel;
    }

    // --- DATEN LADEN ---

    private void loadCatalogData() {
        trainingCatalogModel.setRowCount(0);
        for (Training t : ServiceLocator.getTrainingContainer().getTrainings()) {
            trainingCatalogModel.addRow(new Object[]{t.getId(), t.getTitle(), t.getDescription(), t.getLength()});
        }
    }

    public void loadTeamProgressData() {
        teamProgressModel.setRowCount(0);
        List<Employee> allEmployees = ServiceLocator.getEmployeeContainer().getEmployees();

        String myRole = currentUserRole.toUpperCase();
        boolean seeAll = myRole.contains("HR") || myRole.contains("ADMIN") || myRole.contains("CEO");

        for (Employee emp : allEmployees) {
            boolean show = false;

            if (seeAll) {
                show = true;
            } else if (currentUser != null && emp.getTeamId() == currentUser.getTeamId()) {
                show = true; // Teamleiter sieht sein Team
            }

            if (show) {
                // Wir holen die Historie
                TrainingManager tm = emp.getTrainingManager();
                List<TrainingHistoryEntry> history = (tm != null) ? tm.getTrainingHistory() : null;

                // FALL 1: Mitarbeiter hat KEINE Schulungen
                if (history == null || history.isEmpty()) {
                    teamProgressModel.addRow(new Object[]{
                            emp.getFirstName() + " " + emp.getLastName(),
                            "-",
                            "Keine Zuweisung",
                            "-"
                    });
                }
                // FALL 2: Mitarbeiter HAT Schulungen
                else {
                    for (TrainingHistoryEntry entry : history) {
                        String title = "ID: " + entry.getTrainingId();
                        // Titel auflösen
                        for(Training t : ServiceLocator.getTrainingContainer().getTrainings()) {
                            if(t.getId() == entry.getTrainingId()) {
                                title = t.getTitle(); break;
                            }
                        }

                        // Status sicher abrufen (Falls Enum)
                        String status = (entry.getStatus() != null) ? entry.getStatus().toString() : "OPEN";

                        teamProgressModel.addRow(new Object[]{
                                emp.getFirstName() + " " + emp.getLastName(),
                                title,
                                status,
                                entry.getAssignedAt()
                        });
                    }
                }
            }
        }
    }

    // View Interface
    @Override public String getViewId() { return "training-management-view"; }
    @Override public String getViewTabTitle() { return "Schulungsverwaltung"; }
    @Override public JPanel getContent() { return this; }
    @Override public boolean equals(View view) { return view != null && view.getViewId().equals(this.getViewId()); }
}