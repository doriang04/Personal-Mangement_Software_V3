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

        // 1. Berechtigung prüfen
        String role = ServiceLocator.getSessionManager().getUserPermission();
        if (role == null) role = "GUEST";
        role = role.toUpperCase();

        this.isPrivileged = role.contains("HR") || role.contains("ADMIN") || role.contains("LEAD");

        // 2. UI aufbauen
        initUI();

        // 3. Daten laden (Initial alle anzeigen)
        searchEmployees();
    }

    private void initUI() {
        // --- HEADER: Filterleiste ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Suche Name (Live-Suche)
        filterPanel.add(new JLabel("Name:"));
        txtSearchEmployee = new JTextField(15);
        txtSearchEmployee.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { searchEmployees(); }
            public void removeUpdate(DocumentEvent e) { searchEmployees(); }
            public void changedUpdate(DocumentEvent e) { searchEmployees(); }
        });
        filterPanel.add(txtSearchEmployee);

        // Filter Abteilung
        filterPanel.add(new JLabel("Abteilung:"));
        comboFilterDepartment = new JComboBox<>();
        initDepartmentCombo();
        comboFilterDepartment.addActionListener(e -> searchEmployees());
        filterPanel.add(comboFilterDepartment);

        // Button (Optional durch Live-Suche)
        btnSearch = new JButton("Suchen");
        btnSearch.addActionListener(e -> searchEmployees());
        filterPanel.add(btnSearch);

        add(filterPanel, BorderLayout.NORTH);

        // --- CENTER: Tabelle ---
        initTable();
        JScrollPane scrollPane = new JScrollPane(employeeResultTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void initDepartmentCombo() {
        comboFilterDepartment.addItem(new DepartmentItem(null)); // "Alle Abteilungen"
        for (Department dept : ServiceLocator.getDepartmentContainer().getDepartments()) {
            comboFilterDepartment.addItem(new DepartmentItem(dept));
        }
    }

    private void initTable() {
        // Spalten definieren
        // WICHTIG: "ID" muss dabei sein, damit wir wissen, wen wir öffnen sollen.
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
                return false; // Tabelle nicht editierbar
            }
        };

        employeeResultTable = new JTable(tableModel);
        employeeResultTable.setRowHeight(25);
        employeeResultTable.getTableHeader().setReorderingAllowed(false);

        // --- DOPPELKLICK LOGIK (Hier lag wahrscheinlich der Fehler) ---
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

            // Filter: Abteilung
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
        int row = employeeResultTable.getSelectedRow();
        if (row == -1) return;

        // WICHTIG: Wir holen die ID aus Spalte 0
        Object idObj = tableModel.getValueAt(row, 0);
        int empId = Integer.parseInt(idObj.toString());

        // Aufruf des Controllers mit ID (int)
        UIController.getInstance().openEmployeeDetailTab(empId);
    }

    private static class DepartmentItem {
        Department dept;
        public DepartmentItem(Department d) { this.dept = d; }
        @Override public String toString() { return (dept == null) ? "Alle Abteilungen" : dept.getName(); }
    }

    @Override public String getViewId() { return "employee-search-view"; }
    @Override public String getViewTabTitle() { return "Mitarbeitersuche"; }
    @Override public JPanel getContent() { return this; }
    @Override public boolean equals(View view) {
        if (view == null) return false;
        if (!view.getViewId().equals(getViewId())) return false;
        if (!((EmployeeSearchView) view).txtSearchEmployee.getText().equals(this.txtSearchEmployee.getText())) return false;
        return Objects.equals(Objects.requireNonNull(((EmployeeSearchView) view).comboFilterDepartment.getSelectedItem()).toString(), Objects.requireNonNull(this.comboFilterDepartment.getSelectedItem()).toString());
    }
}