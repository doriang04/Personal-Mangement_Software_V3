package gui.components;

import core.ServiceLocator;
import model.Employee;
import model.Skill;
import model.SkillManager.SkillHistoryEntry;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SkillHistoryPanel extends JPanel {

    private final Employee employee;
    private final boolean isEditable;
    private JTable historyTable;
    private SkillHistoryTableModel tableModel;

    public SkillHistoryPanel(Employee employee, boolean isEditable) {
        this.employee = employee;
        this.isEditable = isEditable;
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        String titleText = "Skill-Historie für: " + employee.getFirstName() + " " + employee.getLastName();
        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        // Table
        tableModel = new SkillHistoryTableModel(new ArrayList<>());
        historyTable = new JTable(tableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setFillsViewportHeight(true);

        // Adjust column widths if needed
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Skill
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Beschreibung
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Erworben am
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Status

        add(new JScrollPane(historyTable), BorderLayout.CENTER);

        // Buttons (only if editable)
        if (isEditable) {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnAdd = new JButton("Hinzufügen");
            JButton btnEdit = new JButton("Bearbeiten");
            JButton btnDelete = new JButton("Löschen");

            btnAdd.addActionListener(e -> addEntry()); // TODO should this be editable?? (or is this admin stuff)
            btnEdit.addActionListener(e -> editEntry()); // TODO should this be editable?? (or is this admin stuff)
            btnDelete.addActionListener(e -> deleteEntry()); // TODO should this be editable?? (or is this admin stuff)

            buttonPanel.add(btnAdd);
            buttonPanel.add(btnEdit);
            buttonPanel.add(btnDelete);
            add(buttonPanel, BorderLayout.SOUTH);
        }
    }

    public void loadData() {
        ArrayList<SkillHistoryEntry> history = employee.getSkillManager().getSkillHistory();
        // Sort by acquisition date (newest first) for better overview
        history.sort(Comparator.comparing(SkillHistoryEntry::getAcquireDate).reversed());
        tableModel.setHistory(history);
    }

    private void addEntry() {
        // A custom JDialog would be ideal for better UX.
        // For simplicity, we use input dialogs.
        Skill selectedSkill = selectSkillDialog("Neuen Skill auswählen");
        if (selectedSkill == null) return;

        String acquireDateStr = JOptionPane.showInputDialog(this, "Erwerbsdatum (YYYY-MM-DD):", LocalDate.now().toString());
        if (acquireDateStr == null || acquireDateStr.trim().isEmpty()) return;

        try {
            LocalDate acquireDate = LocalDate.parse(acquireDateStr);

            // Add the new skill to the employee's skill manager
            employee.getSkillManager().addSkill(selectedSkill, acquireDate);

            // IMPORTANT: The changes must be persisted (e.g., in a DB)
            // ServiceLocator.getDatabaseManager().saveEmployeeSkillHistory(employee);

            JOptionPane.showMessageDialog(this, "Eintrag hinzugefügt.");
            loadData(); // Refresh UI
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editEntry() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Bitte einen Eintrag zum Bearbeiten auswählen.");
            return;
        }

        SkillHistoryEntry entryToEdit = tableModel.getEntryAt(selectedRow);

        // A JDialog would be better here as well.
        String acquireDateStr = JOptionPane.showInputDialog(this, "Erwerbsdatum (YYYY-MM-DD):", entryToEdit.getAcquireDate().toString());
        if (acquireDateStr == null || acquireDateStr.trim().isEmpty()) return;

        try {
            LocalDate acquireDate = LocalDate.parse(acquireDateStr);

            // Update the existing entry object
            entryToEdit.setAcquireDate(acquireDate);
            // NOTE: You could also allow changing the skill itself, but that's less common.
            // For this example, we only edit the acquisition date.

            // IMPORTANT: Persist changes
            // ServiceLocator.getDatabaseManager().updateSkillHistoryEntry(entryToEdit);

            JOptionPane.showMessageDialog(this, "Eintrag aktualisiert.");
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteEntry() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Bitte einen Eintrag zum Löschen auswählen.");
            return;
        }

        SkillHistoryEntry entryToDelete = tableModel.getEntryAt(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this, "Soll dieser Eintrag wirklich gelöscht werden?", "Bestätigung", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // This requires a new method in SkillManager, see step 2
            employee.getSkillManager().removeSkillEntry(entryToDelete);

            // IMPORTANT: Persist changes
            // ServiceLocator.getDatabaseManager().deleteSkillHistoryEntry(entryToDelete);

            JOptionPane.showMessageDialog(this, "Eintrag gelöscht.");
            loadData();
        }
    }

    private Skill selectSkillDialog(String title) {
        List<Skill> skills = ServiceLocator.getSkillContainer().getSkills();

        // Note: JOptionPane uses the .toString() method of the objects.
        // Your current Skill.toString() is "id_(name)". For a better display,
        // you might want to change it to just return getName().
        Skill selected = (Skill) JOptionPane.showInputDialog(
                this,
                "Bitte einen Skill auswählen:",
                title,
                JOptionPane.QUESTION_MESSAGE,
                null,
                skills.toArray(),
                null
        );
        return selected;
    }

    // --- Inner class for the Table Model ---
    private static class SkillHistoryTableModel extends AbstractTableModel {
        private final String[] columnNames = {"Skill", "Beschreibung", "Erworben am", "Status"};
        private List<SkillHistoryEntry> history;
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        public SkillHistoryTableModel(List<SkillHistoryEntry> history) {
            this.history = history;
        }

        public void setHistory(List<SkillHistoryEntry> history) {
            this.history = history;
            fireTableDataChanged();
        }

        public SkillHistoryEntry getEntryAt(int rowIndex) {
            return history.get(rowIndex);
        }

        @Override
        public int getRowCount() {
            return history.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            SkillHistoryEntry entry = history.get(rowIndex);
            Skill skill = ServiceLocator.getSkillContainer().getSkillById(entry.getSkillId());

            switch (columnIndex) {
                case 0: // Skill Name
                    return (skill != null) ? skill.getName() : "Unbekannter Skill (ID: " + entry.getSkillId() + ")";
                case 1: // Skill Description
                    return (skill != null) ? skill.getDescription() : "-";
                case 2: // Acquired Date
                    return entry.getAcquireDate().format(formatter);
                case 3: // Status
                    return entry.isExpired() ? "Abgelaufen" : "Aktiv";
                default:
                    return null;
            }
        }
    }
}