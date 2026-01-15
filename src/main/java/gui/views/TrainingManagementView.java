package gui.views;

import core.ServiceLocator;
import core.SessionManager;
import gui.UIController; // Import the UIController
import gui.components.AssignTrainingDialog;
import gui.components.CreateTrainingDialog;
import model.*;
import model.TrainingManager.TrainingHistoryEntry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class TrainingManagementView extends JPanel implements View {

    private final SessionManager sessionManager;
    private final String currentUserRole;
    private final Employee currentUser;

    private JTabbedPane innerTabbedPane;
    private JTable teamProgressTable;
    private DefaultTableModel teamProgressModel;

    private JTable trainingCatalogTable;
    private DefaultTableModel trainingCatalogModel;

    public TrainingManagementView() {
        this.sessionManager = ServiceLocator.getSessionManager();

        // Safely get the role
        String role = sessionManager.getUserPermission();
        this.currentUserRole = (role != null) ? role.toUpperCase() : "GUEST";

        // Get the current user
        this.currentUser = sessionManager.getCurrentUser();

        setLayout(new BorderLayout());
        initUI();
    }

    private boolean isPrivilegedAdminOrHR() {
        return currentUserRole.contains("ADMIN") || currentUserRole.contains("HR");
    }

    private void initUI() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.add(new JLabel("Schulungsverwaltung - Eingeloggt als: " + currentUserRole));
        add(header, BorderLayout.NORTH);

        innerTabbedPane = new JTabbedPane();

        // Tab 1: Training Catalog (always visible)
        innerTabbedPane.addTab("Schulungskatalog", createCatalogPanel());

        // Tab 2: Team Progress (visible to managers)
        if (isPrivilegedManager()) {
            innerTabbedPane.addTab("Team-Fortschritt", createTeamProgressPanel());
        }

        add(innerTabbedPane, BorderLayout.CENTER);
    }

    private boolean isPrivilegedManager() {
        return currentUserRole.contains("ADMIN") || currentUserRole.contains("HR") || currentUserRole.contains("CEO") || currentUserRole.contains("LEAD") || currentUserRole.contains("MANAGER");
    }

    private void openCreateTrainingDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        // The dialog will modify the training data. The callback should trigger a global
        // UI refresh to ensure all views are updated, not just this one.
        CreateTrainingDialog dialog = new CreateTrainingDialog(parentWindow, () -> UIController.getInstance().updateMainWindow());
        dialog.setVisible(true);
    }

    private JPanel createCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String[] columns = {"ID", "Titel", "Beschreibung", "Dauer (h)", "Skills"};
        trainingCatalogModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        trainingCatalogTable = new JTable(trainingCatalogModel);
        panel.add(new JScrollPane(trainingCatalogTable), BorderLayout.CENTER);

        // Button panel only for HR/Admin
        if (isPrivilegedAdminOrHR()) {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnCreate = new JButton("Neues Training erstellen");
            btnCreate.addActionListener(_ -> openCreateTrainingDialog());
            buttonPanel.add(btnCreate);
            panel.add(buttonPanel, BorderLayout.SOUTH);
        }

        loadCatalogData();
        return panel;
    }

    private JPanel createTeamProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"Mitarbeiter", "Schulung", "Status", "Datum / Info"};
        teamProgressModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        teamProgressTable = new JTable(teamProgressModel);
        panel.add(new JScrollPane(teamProgressTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAssign = new JButton("Schulung zuweisen");
        btnAssign.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            // Assigning a training modifies employee data. Trigger a global UI refresh
            // on success to update all views that might depend on this data.
            new AssignTrainingDialog(parentWindow, () -> UIController.getInstance().updateMainWindow()).setVisible(true);
        });

        buttonPanel.add(btnAssign);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        loadTeamProgressData();
        return panel;
    }

    private void loadCatalogData() {
        if (trainingCatalogModel == null) return;
        trainingCatalogModel.setRowCount(0);

        for (Training t : ServiceLocator.getTrainingContainer().getTrainings()) {
            String skillsText = t.getSkillManager().getSkills().stream()
                    .map(entry -> ServiceLocator.getSkillContainer().getSkillById(entry.getSkillId()))
                    .filter(skill -> skill != null)
                    .map(Skill::getName)
                    .collect(Collectors.joining(", "));
            if (skillsText.isEmpty()) {
                skillsText = "-";
            }

            trainingCatalogModel.addRow(new Object[]{
                    t.getId(),
                    t.getTitle(),
                    t.getDescription(),
                    t.getLength(),
                    skillsText
            });
        }
    }

    public void loadTeamProgressData() {
        if (teamProgressModel == null) return;
        teamProgressModel.setRowCount(0);

        List<Employee> allEmployees = ServiceLocator.getEmployeeContainer().getEmployees();
        boolean seeAll = currentUserRole.contains("HR") || currentUserRole.contains("ADMIN") || currentUserRole.contains("CEO");

        for (Employee emp : allEmployees) {
            boolean show = false;
            if (seeAll) {
                show = true;
            } else if (currentUser != null && emp.getTeamId() == currentUser.getTeamId()) {
                show = true; // Team lead sees their team
            }

            if (show) {
                TrainingManager tm = emp.getTrainingManager();
                List<TrainingHistoryEntry> history = (tm != null) ? tm.getTrainingHistory() : null;

                if (history == null || history.isEmpty()) {
                    teamProgressModel.addRow(new Object[]{(emp.getFirstName() + " " + emp.getLastName()), "-", "Keine Zuweisung", "-"});
                } else {
                    for (TrainingHistoryEntry entry : history) {
                        Training training = ServiceLocator.getTrainingContainer().getTrainingById(entry.getTrainingId());
                        String title = (training != null) ? training.getTitle() : "Unbekannt (ID: " + entry.getTrainingId() + ")";
                        String status = (entry.getStatus() != null) ? entry.getStatus().toString() : "OPEN";

                        teamProgressModel.addRow(new Object[]{(emp.getFirstName() + " " + emp.getLastName()), title, status, entry.getAssignedAt()});
                    }
                }
            }
        }
    }

    @Override public String getViewId() { return "training-management-view"; }
    @Override public String getViewTabTitle() { return "Schulungsverwaltung"; }
    @Override public JPanel getContent() { return this; }
    @Override public boolean equals(View view) { return view != null && view.getViewId().equals(this.getViewId()); }

    /**
     * Refreshes all data displayed in this view.
     * This reloads the training catalog and, if visible, the team progress table.
     */
    @Override
    public void updateSelf() {
        // 1. Refresh the training catalog, which is always visible.
        loadCatalogData();

        // 2. Refresh the team progress table, but only if the user has
        //    permissions to see it (and thus the model is not null).
        if (teamProgressModel != null) {
            loadTeamProgressData();
        }
    }
}