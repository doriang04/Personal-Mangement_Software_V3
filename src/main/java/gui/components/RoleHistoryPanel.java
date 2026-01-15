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

    private Employee employee; // Changed to non-final to allow updates
    private final boolean isEditable;
    private final Runnable onDataChangedCallback; // Callback for global refresh

    private JTable historyTable;
    private RoleHistoryTableModel tableModel;

    /**
     * Updated constructor to accept a callback.
     * @param employee The employee whose role history is displayed.
     * @param isEditable If true, editing controls are visible.
     * @param onDataChangedCallback A callback to run after data is successfully modified. Can be null.
     */
    public RoleHistoryPanel(Employee employee, boolean isEditable, Runnable onDataChangedCallback) {
        this.employee = employee;
        this.isEditable = isEditable;
        this.onDataChangedCallback = onDataChangedCallback;
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
        tableModel = new RoleHistoryTableModel(new ArrayList<>());
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

    /**
     * Reloads and displays the role history data for the current employee.
     */
    public void loadData() {
        if (employee == null || employee.getRoleManager() == null) {
            tableModel.setHistory(new ArrayList<>());
            return;
        }
        ArrayList<RoleHistoryEntry> history = employee.getRoleManager().getRoleHistory();
        history.sort(Comparator.comparing(RoleHistoryEntry::getAcquireDate).reversed());
        tableModel.setHistory(history);
    }

    /**
     * Updates the employee reference for this panel. This is useful when the parent view
     * reloads its data and gets a new employee object instance.
     * @param employee The new, fresh Employee object.
     */
    public void updateEmployee(Employee employee) {
        this.employee = employee;
    }

    private void addEntry() {
        Role selectedRole = selectRoleDialog("Neue Rolle auswählen");
        if (selectedRole == null) return;

        String acquireDateStr = JOptionPane.showInputDialog(this, "Startdatum (YYYY-MM-DD):", LocalDate.now().toString());
        if (acquireDateStr == null || acquireDateStr.trim().isEmpty()) return;

        String endDateStr = JOptionPane.showInputDialog(this, "Enddatum (YYYY-MM-DD, leer lassen für aktive Rolle):");

        try {
            LocalDate acquireDate = LocalDate.parse(acquireDateStr);
            LocalDate endDate = (endDateStr == null || endDateStr.trim().isEmpty()) ? null : LocalDate.parse(endDateStr);

            if (endDate == null) {
                employee.getRoleManager().assignRole(selectedRole.getId(), acquireDate);
            } else {
                RoleHistoryEntry newEntry = new RoleHistoryEntry(selectedRole.getId(), acquireDate, endDate);
                employee.getRoleManager().addRoleHistoryEntry(newEntry);
            }

            JOptionPane.showMessageDialog(this, "Eintrag hinzugefügt.");

            // Trigger global refresh via the provided callback.
            if (onDataChangedCallback != null) {
                onDataChangedCallback.run();
            } else {
                loadData(); // Fallback to local refresh if no callback is given.
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

        RoleHistoryEntry entryToEdit = tableModel.getEntryAt(selectedRow);
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

            // Trigger global refresh via the provided callback.
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

        RoleHistoryEntry entryToDelete = tableModel.getEntryAt(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this, "Soll dieser Eintrag wirklich gelöscht werden?", "Bestätigung", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            employee.getRoleManager().removeRoleHistoryEntry(entryToDelete);
            JOptionPane.showMessageDialog(this, "Eintrag gelöscht.");

            // Trigger global refresh via the provided callback.
            if (onDataChangedCallback != null) {
                onDataChangedCallback.run();
            } else {
                loadData();
            }
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
            return columnNames.length > column ? columnNames[column] : "";
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