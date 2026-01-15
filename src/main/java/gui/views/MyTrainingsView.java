package gui.views;

import core.ServiceLocator;
import gui.components.SkillHistoryPanel;
import model.*;
import model.TrainingManager.TrainingHistoryEntry;
import model.TrainingManager.Status;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MyTrainingsView extends JPanel implements View {

    private Employee currentUser;

    private JTabbedPane tabbedPane;
    private JTable openTable;
    private DefaultTableModel openModel;
    private JTable historyTable;
    private DefaultTableModel historyModel;
    private SkillHistoryPanel mySkillsPanel; // Reference to the skills panel

    public MyTrainingsView() {
        setLayout(new BorderLayout());
        this.currentUser = ServiceLocator.getSessionManager().getCurrentUser();
        initUI();
        loadData();
    }

    private void initUI() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel title = new JLabel("Meine Schulungsübersicht");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        header.add(title);
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(header, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Offene Schulungen", createOpenTrainingsPanel());
        tabbedPane.addTab("Historie (Erledigt)", createHistoryPanel());
        tabbedPane.addTab("Meine Skills", createMySkillsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createMySkillsPanel() {
        // The SkillHistoryPanel is a self-contained component.
        // For viewing one's own profile, it's always read-only.
        // We store the instance to be able to refresh it later.
        this.mySkillsPanel = new SkillHistoryPanel(this.currentUser, false);
        return this.mySkillsPanel;
    }

    private JPanel createOpenTrainingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // FIX: Add ID as the first column to the model
        String[] columns = {"ID", "Schulung", "Beschreibung", "Zugewiesen am"};
        openModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        openTable = new JTable(openModel);
        openTable.setRowHeight(25);
        openTable.getTableHeader().setReorderingAllowed(false);
        // FIX: Hide the ID column from the user
        openTable.removeColumn(openTable.getColumnModel().getColumn(0));

        panel.add(new JScrollPane(openTable), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnComplete = new JButton("Als erledigt markieren");
        btnComplete.addActionListener(_ -> completeSelectedTraining());

        footer.add(btnComplete);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"Schulung", "Abschlussdatum", "Zertifikat"};
        historyModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        historyTable = new JTable(historyModel);
        historyTable.setRowHeight(25);
        historyTable.getTableHeader().setReorderingAllowed(false);

        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        return panel;
    }

    private void loadData() {
        openModel.setRowCount(0);
        historyModel.setRowCount(0);

        if (currentUser == null) return;

        TrainingManager tm = currentUser.getTrainingManager();
        if (tm == null) return;

        ArrayList<TrainingHistoryEntry> history = tm.getTrainingHistory();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (TrainingHistoryEntry entry : history) {
            String title = "Unbekannt (ID: " + entry.getTrainingId() + ")";
            String description = "-";

            Training t = ServiceLocator.getTrainingContainer().getTrainingById(entry.getTrainingId());
            if (t != null) {
                title = t.getTitle();
                description = t.getDescription();
            }

            if (entry.getStatus() == null || entry.getStatus() == Status.OPEN) {
                // Now correctly adds 4 items to the 4-column model
                openModel.addRow(new Object[]{
                        entry.getTrainingId(),
                        title,
                        description,
                        entry.getAssignedAt().format(formatter)
                });
            } else if (entry.getStatus() == Status.DONE) {
                String completedStr = (entry.getCompletedAt() != null)
                        ? entry.getCompletedAt().format(formatter)
                        : "-";

                historyModel.addRow(new Object[]{
                        title,
                        completedStr,
                        "Anzeigen" // Placeholder
                });
            }
        }
    }

    private void completeSelectedTraining() {
        int selectedViewRow = openTable.getSelectedRow();
        if (selectedViewRow == -1) {
            JOptionPane.showMessageDialog(this, "Bitte eine Schulung auswählen.");
            return;
        }

        // Convert view index to model index to be safe
        int modelRow = openTable.convertRowIndexToModel(selectedViewRow);

        // FIX: Get ID from column 0, Title from column 1
        int trainingId = (int) openModel.getValueAt(modelRow, 0);
        String trainingTitle = (String) openModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Haben Sie '" + trainingTitle + "' wirklich abgeschlossen?",
                "Bestätigung", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                TrainingManager tm = currentUser.getTrainingManager();
                tm.completeTraining(trainingId, LocalDate.now());

                JOptionPane.showMessageDialog(this, "Erledigt! In Historie verschoben.");
                loadData(); // Refresh the tables

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Fehler: " + ex.getMessage());
            }
        }
    }

    @Override public String getViewId() { return "my-trainings-view"; }
    @Override public String getViewTabTitle() { return "Meine Schulungen"; }
    @Override public JPanel getContent() { return this; }
    @Override public boolean equals(View view) { return view != null && view.getViewId().equals(getViewId()); }

    /**
     * Refreshes all data displayed in this view.
     * This reloads the list of open and completed trainings, and also
     * triggers an update of the "My Skills" panel.
     */
    @Override
    public void updateSelf() {
        // 1. Re-fetch the current user from the session to ensure data is fresh.
        this.currentUser = ServiceLocator.getSessionManager().getCurrentUser();

        // 2. Reload data for the training tables.
        loadData();

        // 3. Trigger a refresh of the contained skills panel.
        //    (This assumes SkillHistoryPanel has a public loadData() method).
        if (this.mySkillsPanel != null) {
            // The panel might need the latest user object reference if it has changed.
            // A more robust implementation of SkillHistoryPanel might have a `setUser(e)` method.
            // For now, we assume calling its own loadData is sufficient.
            this.mySkillsPanel.loadData();
        }
    }
}