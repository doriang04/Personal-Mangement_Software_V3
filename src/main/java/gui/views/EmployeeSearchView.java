package gui.views;

import core.ServiceLocator;
import gui.UIController;
import model.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EmployeeSearchView extends JPanel implements View {

    private JTextField txtSearchEmployee;
    private JComboBox<DepartmentItem> comboFilterDepartment;
    private JButton btnSearch;
    private JTable employeeResultTable;
    private DefaultTableModel tableModel;

    private final boolean isPrivileged;

    public EmployeeSearchView() {
        setLayout(new BorderLayout());

        // 1. Check permissions
        String role = ServiceLocator.getSessionManager().getUserPermission();
        if (role == null) role = "GUEST";
        role = role.toUpperCase();

        this.isPrivileged = role.contains("HR") || role.contains("ADMIN") || role.contains("LEAD");

        // 2. Build UI
        initUI();

        // 3. Load initial data
        searchEmployees();
    }

    private void initUI() {
        // --- HEADER: Filter Panel ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Name Search (Live Search)
        filterPanel.add(new JLabel("Name:"));
        txtSearchEmployee = new JTextField(15);
        txtSearchEmployee.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { searchEmployees(); }
            public void removeUpdate(DocumentEvent e) { searchEmployees(); }
            public void changedUpdate(DocumentEvent e) { searchEmployees(); }
        });
        filterPanel.add(txtSearchEmployee);

        // Department Filter
        filterPanel.add(new JLabel("Abteilung:"));
        comboFilterDepartment = new JComboBox<>();
        refreshDepartmentCombo(); // Use the new refresh method
        comboFilterDepartment.addActionListener(e -> searchEmployees());
        filterPanel.add(comboFilterDepartment);

        // Search Button (optional due to live search)
        btnSearch = new JButton("Suchen");
        btnSearch.addActionListener(e -> searchEmployees());
        filterPanel.add(btnSearch);

        add(filterPanel, BorderLayout.NORTH);

        // --- CENTER: Table ---
        initTable();
        JScrollPane scrollPane = new JScrollPane(employeeResultTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void refreshDepartmentCombo() {
        // --- Preserve current selection ---
        Object selectedItem = comboFilterDepartment.getSelectedItem();

        // --- Repopulate the combo box ---
        comboFilterDepartment.removeAllItems();
        comboFilterDepartment.addItem(new DepartmentItem(null)); // "Alle Abteilungen"
        for (Department dept : ServiceLocator.getDepartmentContainer().getDepartments()) {
            comboFilterDepartment.addItem(new DepartmentItem(dept));
        }

        // --- Restore selection if it still exists ---
        if (selectedItem != null) {
            comboFilterDepartment.setSelectedItem(selectedItem);
        }
    }

    private void initTable() {
        // Define columns
        ArrayList<String> columns = new ArrayList<>();
        columns.add("ID");
        columns.add("Nachname");
        columns.add("Vorname");
        columns.add("Abteilung");
        columns.add("E-Mail (Arbeit)");

        if (isPrivileged) {
            columns.add("Rolle");
            columns.add("Telefon");
        }

        tableModel = new DefaultTableModel(columns.toArray(), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table not editable
            }
        };

        employeeResultTable = new JTable(tableModel);
        employeeResultTable.setRowHeight(25);
        employeeResultTable.getTableHeader().setReorderingAllowed(false);

        // Hide the ID column from the user, but keep it in the model
        employeeResultTable.removeColumn(employeeResultTable.getColumnModel().getColumn(0));

        // Double-click logic
        employeeResultTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && employeeResultTable.getSelectedRow() != -1) {
                    openSelectedProfile();
                }
            }
        });
    }

    private void searchEmployees() {
        tableModel.setRowCount(0);

        String searchText = txtSearchEmployee.getText().toLowerCase().trim();
        DepartmentItem selectedDeptItem = (DepartmentItem) comboFilterDepartment.getSelectedItem();
        Integer filterDeptId = (selectedDeptItem != null && selectedDeptItem.dept != null)
                ? selectedDeptItem.dept.getId() : null;

        List<Employee> allEmployees = ServiceLocator.getEmployeeContainer().getEmployees();

        for (Employee emp : allEmployees) {
            // Filter: Name
            boolean nameMatch = searchText.isEmpty() ||
                    emp.getLastName().toLowerCase().contains(searchText) ||
                    emp.getFirstName().toLowerCase().contains(searchText) ||
                    emp.getUsername().toLowerCase().contains(searchText);

            // Filter: Department
            boolean deptMatch = true;
            String deptName = "Keine Abteilung";

            Team team = ServiceLocator.getTeamContainer().getTeamById(emp.getTeamId());
            if (team != null) {
                Department dept = ServiceLocator.getDepartmentContainer().getDepartmentById(team.getDepartmentId());
                if (dept != null) {
                    deptName = dept.getName();
                    if (filterDeptId != null && dept.getId() != filterDeptId) deptMatch = false;
                } else if (filterDeptId != null) deptMatch = false;
            } else if (filterDeptId != null) {
                deptMatch = false;
            }

            // Don't show the currently logged-in user in the search results
            if (nameMatch && deptMatch && emp.getId() != ServiceLocator.getSessionManager().getCurrentUser().getId()) {
                ArrayList<Object> rowData = new ArrayList<>();
                rowData.add(emp.getId());
                rowData.add(emp.getLastName());
                rowData.add(emp.getFirstName());
                rowData.add(deptName);
                rowData.add(emp.getEMail());

                if (isPrivileged) {
                    String roleName = "-";
                    try {
                        if (emp.getRoleManager().getActiveRole() != null) {
                            roleName = emp.getRoleManager().getActiveRole().getName();
                        }
                    } catch (Exception _) {}
                    rowData.add(roleName);
                    rowData.add(emp.getPhoneNumber());
                }

                tableModel.addRow(rowData.toArray());
            }
        }
    }

    private void openSelectedProfile() {
        int selectedViewRow = employeeResultTable.getSelectedRow();
        if (selectedViewRow == -1) return;

        // Convert view index to model index in case columns are sorted/moved
        int modelRow = employeeResultTable.convertRowIndexToModel(selectedViewRow);

        // Get the ID from column 0 of the model
        Object idObj = tableModel.getValueAt(modelRow, 0);
        int empId = Integer.parseInt(idObj.toString());

        UIController.getInstance().openEmployeeDetailTab(empId);
    }

    private static class DepartmentItem {
        Department dept;
        public DepartmentItem(Department d) { this.dept = d; }
        @Override public String toString() { return (dept == null) ? "Alle Abteilungen" : dept.getName(); }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DepartmentItem that = (DepartmentItem) o;
            if (dept == null) return that.dept == null;
            if (that.dept == null) return false;
            return dept.getId() == that.dept.getId();
        }

        @Override
        public int hashCode() {
            return Objects.hash(dept != null ? dept.getId() : -1);
        }
    }

    @Override public String getViewId() { return "employee-search-view"; }
    @Override public String getViewTabTitle() { return "Mitarbeitersuche"; }
    @Override public JPanel getContent() { return this; }

    // The equals method can be simplified as its complexity is not needed for simple view management.
    @Override public boolean equals(View view) {
        if (view == null) return false;
        if (!view.getViewId().equals(getViewId())) return false;
        if (!((EmployeeSearchView) view).txtSearchEmployee.getText().equals(this.txtSearchEmployee.getText())) return false;
        return Objects.equals(Objects.requireNonNull(((EmployeeSearchView) view).comboFilterDepartment.getSelectedItem()).toString(), Objects.requireNonNull(this.comboFilterDepartment.getSelectedItem()).toString());
    }

    /**
     * Refreshes the view by reloading data from core services.
     * This updates the department filter dropdown and re-runs the search
     * to update the results table, while preserving the user's filter criteria.
     */
    @Override
    public void updateSelf() {
        // 1. Refresh the department filter dropdown, preserving the current selection.
        refreshDepartmentCombo();

        // 2. Re-run the search. This will use the current search text and the
        //    (potentially newly selected or restored) department filter to refresh the table.
        searchEmployees();
    }
}