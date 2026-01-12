package gui.components;

import core.ServiceLocator;
import model.Employee;
import model.Role;
import model.RoleManager.RoleHistoryEntry;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RoleHistoryPanel extends JPanel {

    private final Employee employee;
    private final boolean isEditable;
    private JTable historyTable;
    private RoleHistoryTableModel tableModel;

    public RoleHistoryPanel(Employee employee, boolean isEditable) {
        this.employee = employee;
        this.isEditable = isEditable;
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Titel
        String titleText = "Rollenhistorie für: " + employee.getFirstName() + " " + employee.getLastName();
        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        // Tabelle
        tableModel = new RoleHistoryTableModel(employee.getRoleManager().getRoleHistory());
        historyTable = new JTable(tableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setFillsViewportHeight(true);

        add(new JScrollPane(historyTable), BorderLayout.CENTER);

        // Buttons (nur wenn bearbeitbar)
        if (isEditable) {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnAdd = new JButton("Hinzufügen");
            JButton btnEdit = new JButton("Bearbeiten");
            JButton btnDelete = new JButton("Löschen");

            btnAdd.addActionListener(e -> addEntry());
            btnEdit.addActionListener(e -> editEntry());
            btnDelete.addActionListener(e -> deleteEntry());

            buttonPanel.add(btnAdd);
            buttonPanel.add(btnEdit);
            buttonPanel.add(btnDelete);
            add(buttonPanel, BorderLayout.SOUTH);
        }
    }

    public void loadData() {
        ArrayList<RoleHistoryEntry> history = employee.getRoleManager().getRoleHistory();
        // Sortieren nach Startdatum (neueste zuerst) für bessere Übersicht
        history.sort(Comparator.comparing(RoleHistoryEntry::getAcquireDate).reversed());
        tableModel.setHistory(history);
    }

    private void addEntry() {
        // Für einen besseren UX wäre hier ein eigener JDialog ideal.
        // Zur Vereinfachung nutzen wir Input-Dialoge.
        Role selectedRole = selectRoleDialog("Neue Rolle auswählen");
        if (selectedRole == null) return;

        String acquireDateStr = JOptionPane.showInputDialog(this, "Startdatum (YYYY-MM-DD):", LocalDate.now().toString());
        if (acquireDateStr == null || acquireDateStr.trim().isEmpty()) return;

        String endDateStr = JOptionPane.showInputDialog(this, "Enddatum (YYYY-MM-DD, leer lassen für aktive Rolle):");

        try {
            LocalDate acquireDate = LocalDate.parse(acquireDateStr);
            LocalDate endDate = (endDateStr == null || endDateStr.trim().isEmpty()) ? null : LocalDate.parse(endDateStr);

            // Logik zum Beenden der alten aktiven Rolle, falls nötig
            if (endDate == null) {
                employee.getRoleManager().assignRole(selectedRole.getId(), acquireDate);
            } else {
                RoleHistoryEntry newEntry = new RoleHistoryEntry(selectedRole.getId(), acquireDate, endDate);
                employee.getRoleManager().addRoleHistoryEntry(newEntry);
            }

            // WICHTIG: Die Änderungen müssen persistiert werden (z.B. in einer DB)
            // ServiceLocator.getDatabaseManager().saveEmployeeRoleHistory(employee);

            JOptionPane.showMessageDialog(this, "Eintrag hinzugefügt.");
            loadData(); // UI aktualisieren
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

        RoleHistoryEntry entryToEdit = tableModel.getEntryAt(selectedRow);

        // Auch hier wäre ein JDialog besser.
        Role selectedRole = selectRoleDialog("Rolle ändern", entryToEdit.getRoleId());
        if (selectedRole == null) return;

        String acquireDateStr = JOptionPane.showInputDialog(this, "Startdatum (YYYY-MM-DD):", entryToEdit.getAcquireDate().toString());
        if (acquireDateStr == null || acquireDateStr.trim().isEmpty()) return;

        String endDateStr = JOptionPane.showInputDialog(this, "Enddatum (YYYY-MM-DD, leer lassen für aktive Rolle):",
                entryToEdit.getEndDate() != null ? entryToEdit.getEndDate().toString() : "");

        try {
            LocalDate acquireDate = LocalDate.parse(acquireDateStr);
            LocalDate endDate = (endDateStr == null || endDateStr.trim().isEmpty()) ? null : LocalDate.parse(endDateStr);

            entryToEdit.setRoleId(selectedRole.getId());
            entryToEdit.setAcquireDate(acquireDate);
            entryToEdit.setEndDate(endDate);

            employee.getRoleManager().updateRoleHistoryEntry(entryToEdit);

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

        RoleHistoryEntry entryToDelete = tableModel.getEntryAt(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this, "Soll dieser Eintrag wirklich gelöscht werden?", "Bestätigung", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            employee.getRoleManager().removeRoleHistoryEntry(entryToDelete);

            // WICHTIG: Die Änderungen müssen persistiert werden
            // ServiceLocator.getDatabaseManager().saveEmployeeRoleHistory(employee);

            JOptionPane.showMessageDialog(this, "Eintrag gelöscht.");
            loadData();
        }
    }

    private Role selectRoleDialog(String title, int preselectedRoleId) {
        List<Role> roles = ServiceLocator.getRoleContainer().getRoles();
        Role preselectedRole = null;
        for(Role r : roles) {
            if(r.getId() == preselectedRoleId) {
                preselectedRole = r;
                break;
            }
        }

        Role selected = (Role) JOptionPane.showInputDialog(
                this,
                "Bitte eine Rolle auswählen:",
                title,
                JOptionPane.QUESTION_MESSAGE,
                null,
                roles.toArray(),
                preselectedRole
        );
        return selected;
    }

    private Role selectRoleDialog(String title) {
        return selectRoleDialog(title, -1);
    }

    // --- Inner class for the Table Model ---
    private static class RoleHistoryTableModel extends AbstractTableModel {
        private final String[] columnNames = {"Rolle", "Startdatum", "Enddatum", "Status"};
        private List<RoleHistoryEntry> history;
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        public RoleHistoryTableModel(List<RoleHistoryEntry> history) {
            this.history = history;
        }

        public void setHistory(List<RoleHistoryEntry> history) {
            this.history = history;
            fireTableDataChanged();
        }

        public RoleHistoryEntry getEntryAt(int rowIndex) {
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
            RoleHistoryEntry entry = history.get(rowIndex);

            switch (columnIndex) {
                case 0:
                    Role role = ServiceLocator.getRoleContainer().getRoleById(entry.getRoleId());
                    return (role != null) ? role.getName() : "Unbekannte Rolle (ID: " + entry.getRoleId() + ")";
                case 1:
                    return entry.getAcquireDate().format(formatter);
                case 2:
                    return (entry.getEndDate() != null) ? entry.getEndDate().format(formatter) : "-";
                case 3:
                    return entry.isActive() ? "Aktiv" : "Abgelaufen";
                default:
                    return null;
            }
        }
    }
}