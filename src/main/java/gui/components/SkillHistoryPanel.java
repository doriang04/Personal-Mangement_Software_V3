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

    private Employee employee;
    private final boolean isEditable;
    private final Runnable onDataChangedCallback;

    private JTable historyTable;
    private SkillHistoryTableModel tableModel;

    public SkillHistoryPanel(Employee employee, boolean isEditable, Runnable onDataChangedCallback) {
        this.employee = employee;
        this.isEditable = isEditable;
        this.onDataChangedCallback = onDataChangedCallback;
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Skill-Historie für: " + employee.getFirstName() + " " + employee.getLastName());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        tableModel = new SkillHistoryTableModel(new ArrayList<>());
        historyTable = new JTable(tableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setFillsViewportHeight(true);
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(80);

        add(new JScrollPane(historyTable), BorderLayout.CENTER);

        if (isEditable) {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnAdd = new JButton("Hinzufügen");
            JButton btnEdit = new JButton("Bearbeiten");
            JButton btnDelete = new JButton("Löschen");

            btnAdd.addActionListener(_ -> addEntry());
            btnEdit.addActionListener(_ -> editEntry());
            btnDelete.addActionListener(_ -> deleteEntry());

            buttonPanel.add(btnAdd);
            buttonPanel.add(btnEdit);
            buttonPanel.add(btnDelete);
            add(buttonPanel, BorderLayout.SOUTH);
        }
    }

    public void loadData() {
        if (employee == null || employee.getSkillManager() == null) {
            tableModel.setHistory(new ArrayList<>());
            return;
        }
        ArrayList<SkillHistoryEntry> history = employee.getSkillManager().getSkillHistory();
        history.sort(Comparator.comparing(SkillHistoryEntry::getAcquireDate).reversed());
        tableModel.setHistory(history);
    }

    public void updateEmployee(Employee employee) {
        this.employee = employee;
    }

    private void addEntry() {
        Skill selectedSkill = selectSkillDialog("Neuen Skill auswählen");
        if (selectedSkill == null) return;

        String acquireDateStr = JOptionPane.showInputDialog(this, "Erwerbsdatum (YYYY-MM-DD):", LocalDate.now().toString());
        if (acquireDateStr == null || acquireDateStr.trim().isEmpty()) return;

        try {
            LocalDate acquireDate = LocalDate.parse(acquireDateStr);
            employee.getSkillManager().addSkill(selectedSkill, acquireDate);

            JOptionPane.showMessageDialog(this, "Eintrag hinzugefügt.");

            if (onDataChangedCallback != null) {
                onDataChangedCallback.run();
            } else {
                loadData();
            }
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
        String acquireDateStr = JOptionPane.showInputDialog(this, "Erwerbsdatum (YYYY-MM-DD):", entryToEdit.getAcquireDate().toString());
        if (acquireDateStr == null || acquireDateStr.trim().isEmpty()) return;

        try {
            LocalDate acquireDate = LocalDate.parse(acquireDateStr);
            entryToEdit.setAcquireDate(acquireDate);
            JOptionPane.showMessageDialog(this, "Eintrag aktualisiert.");

            if (onDataChangedCallback != null) {
                onDataChangedCallback.run();
            } else {
                loadData();
            }
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
            employee.getSkillManager().removeSkillEntry(entryToDelete);
            JOptionPane.showMessageDialog(this, "Eintrag gelöscht.");

            if (onDataChangedCallback != null) {
                onDataChangedCallback.run();
            } else {
                loadData();
            }
        }
    }

    private Skill selectSkillDialog(String title) {
        List<Skill> skills = ServiceLocator.getSkillContainer().getSkills();
        return (Skill) JOptionPane.showInputDialog(
                this,
                "Bitte einen Skill auswählen:",
                title,
                JOptionPane.QUESTION_MESSAGE,
                null,
                skills.toArray(),
                null
        );
    }

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
                case 0:
                    return (skill != null) ? skill.getName() : "Unbekannter Skill (ID: " + entry.getSkillId() + ")";
                case 1:
                    return (skill != null) ? skill.getDescription() : "-";
                case 2:
                    return entry.getAcquireDate().format(formatter);
                case 3:
                    return entry.isExpired() ? "Abgelaufen" : "Aktiv";
                default:
                    return null;
            }
        }
    }
}