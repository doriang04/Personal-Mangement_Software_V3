package gui.views;

import core.SessionManager;
import model.*;
import model.TrainingManager.TrainingHistoryEntry;
import model.TrainingManager.Status;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MyTrainingsView extends JPanel implements View {

    private Employee currentUser;

    // UI Komponenten
    private JTabbedPane tabbedPane;
    private JTable openTable;
    private DefaultTableModel openModel;
    private JTable historyTable;
    private DefaultTableModel historyModel;

    public MyTrainingsView() {
        setLayout(new BorderLayout());

        // 1. Eingeloggten User finden
        this.currentUser = findCurrentUser();

        // 2. UI aufbauen
        initUI();

        // 3. Daten laden
        loadData();
    }

    private Employee findCurrentUser() {
        SessionManager session = ServiceLocator.getSessionManager();
        String fullName = session.getUserFirstNameAndLastName();

        if (fullName == null) return null;

        for (Employee e : ServiceLocator.getEmployeeContainer().getEmployees()) {
            if ((e.getFirstName() + e.getLastName()).equalsIgnoreCase(fullName.replace(" ", ""))) {
                return e;
            }
        }
        return null;
    }

    private void initUI() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel title = new JLabel("Meine Schulungsübersicht");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        header.add(title);
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(header, BorderLayout.NORTH);

        // Tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Offene Schulungen", createOpenTrainingsPanel());
        tabbedPane.addTab("Historie (Erledigt)", createHistoryPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // --- TAB 1: OFFENE SCHULUNGEN ---
    private JPanel createOpenTrainingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Spalten: ID, Titel, Beschreibung, Zuweisungsdatum
        String[] columns = {"ID", "Schulung", "Beschreibung", "Zugewiesen am"};
        openModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        openTable = new JTable(openModel);
        openTable.setRowHeight(25);
        openTable.getTableHeader().setReorderingAllowed(false);

        panel.add(new JScrollPane(openTable), BorderLayout.CENTER);

        // Button unten
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnComplete = new JButton("Als erledigt markieren");
        btnComplete.addActionListener(e -> completeSelectedTraining());

        footer.add(btnComplete);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    // --- TAB 2: HISTORIE (Ohne Ablaufdatum) ---
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Spalten: Titel, Abschlussdatum, Zertifikat
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

        TrainingManager tm = currentUser.getOpenTrainingManager();
        if (tm == null) return;

        List<TrainingHistoryEntry> history = tm.getTrainingHistory();

        for (TrainingHistoryEntry entry : history) {
            // Namen auflösen
            String title = "Unbekannt";
            String description = "-";

            for(Training t : ServiceLocator.getTrainingContainer().getTrainings()) {
                if (t.getId() == entry.getTrainingId()) {
                    title = t.getTitle();
                    description = t.getDescription();
                    break;
                }
            }

            // --- Status prüfen ---
            if (entry.getStatus() == null || entry.getStatus() == Status.OPEN) {
                // OFFEN
                openModel.addRow(new Object[]{
                        entry.getTrainingId(),
                        title,
                        description,
                        entry.getAssignedAt()
                });
            } else if (entry.getStatus() == Status.DONE) {
                // ERLEDIGT
                String completedStr = (entry.getCompletedAt() != null)
                        ? entry.getCompletedAt().format(DateTimeFormatter.ISO_DATE)
                        : "-";

                historyModel.addRow(new Object[]{
                        title,
                        completedStr,
                        "Anzeigen" // Platzhalter Button
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
                TrainingManager tm = currentUser.getOpenTrainingManager();
                // Nur im RAM speichern (wie gewünscht)
                tm.completeTraining(trainingId, LocalDate.now());

                JOptionPane.showMessageDialog(this, "Erledigt! In Historie verschoben.");
                loadData(); // Ansicht aktualisieren

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