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
        // NEU: Tab für die Skill-Übersicht hinzufügen
        tabbedPane.addTab("Meine Skills", createMySkillsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createMySkillsPanel() {
        // Die SkillHistoryPanel ist eine eigenständige Komponente.
        // Für die Ansicht des eigenen Profils ist sie immer schreibgeschützt.
        SkillHistoryPanel panel = new SkillHistoryPanel(this.currentUser, false);
        return panel;
    }

    private JPanel createOpenTrainingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"Schulung", "Beschreibung", "Zugewiesen am"};
        openModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        openTable = new JTable(openModel);
        openTable.setRowHeight(25);
        openTable.getTableHeader().setReorderingAllowed(false);

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

        for (TrainingHistoryEntry entry : history) {
            String title = "Unbekannt";
            String description = "-";

            Training t = ServiceLocator.getTrainingContainer().getTrainingById(entry.getTrainingId());
            if (t != null) title = t.getTitle();
            if (t != null) description = t.getDescription();

            if (entry.getStatus() == null || entry.getStatus() == Status.OPEN) {
                openModel.addRow(new Object[]{
                        entry.getTrainingId(),
                        title,
                        description,
                        entry.getAssignedAt()
                });
            } else if (entry.getStatus() == Status.DONE) {
                String completedStr = (entry.getCompletedAt() != null)
                        ? entry.getCompletedAt().format(DateTimeFormatter.ISO_DATE)
                        : "-";

                historyModel.addRow(new Object[]{
                        title,
                        completedStr,
                        "Anzeigen" // Platzhalter Button TODO später noch austauschen gegen was wirklich gebrauchtes (maybe)
                });
            }
        }
    }

    private void completeSelectedTraining() {
        int selectedRow = openTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Bitte eine Schulung auswählen.");
            return;
        }

        int trainingId = (int) openModel.getValueAt(selectedRow, 0);
        String trainingTitle = (String) openModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Haben Sie '" + trainingTitle + "' wirklich abgeschlossen?",
                "Bestätigung", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                TrainingManager tm = currentUser.getTrainingManager();
                tm.completeTraining(trainingId, LocalDate.now());

                JOptionPane.showMessageDialog(this, "Erledigt! In Historie verschoben.");
                loadData();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Fehler: " + ex.getMessage());
            }
        }
    }

    @Override public String getViewId() { return "my-trainings-view"; }
    @Override public String getViewTabTitle() { return "Meine Schulungen"; }
    @Override public JPanel getContent() { return this; }
    @Override public boolean equals(View view) { return view != null && view.getViewId().equals(getViewId()); }
}