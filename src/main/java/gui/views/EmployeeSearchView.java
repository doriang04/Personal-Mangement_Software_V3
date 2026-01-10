package gui.views;

import core.SessionManager;
import gui.UIController;
import model.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class EmployeeSearchView extends JPanel implements View {

    // UI Komponenten (Namen wie in Anforderung)
    private JTextField txtSearchEmployee;
    private JComboBox<DepartmentItem> comboFilterDepartment;
    private JButton btnSearch;
    private JTable employeeResultTable;
    private DefaultTableModel tableModel;

    // Daten & State
    private final boolean isPrivileged; // True für HR oder Teamleiter

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

        // 1. Suche Name
        filterPanel.add(new JLabel("Name:"));
        txtSearchEmployee = new JTextField(15);

        // LIVE-UPDATE LOGIK:
        // Wir hören auf Änderungen im "Dokument" des Textfeldes (Einfügen, Löschen, Ändern)
        txtSearchEmployee.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchEmployees();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchEmployees();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchEmployees();
            }
        });

        filterPanel.add(txtSearchEmployee);

        // 2. Filter Abteilung
        filterPanel.add(new JLabel("Abteilung:"));
        comboFilterDepartment = new JComboBox<>();
        initDepartmentCombo();

        // LIVE-UPDATE AUCH BEI COMBOBOX:
        // Sobald eine andere Abteilung gewählt wird -> Suchen
        comboFilterDepartment.addActionListener(e -> searchEmployees());

        filterPanel.add(comboFilterDepartment);

        // 3. Button Suchen (Optional, da jetzt alles live ist, aber gut zur Bestätigung)
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
        // Spalten definieren (Rollenabhängig)
        // ID ist immer dabei (versteckt oder Index 0), um den User zu identifizieren
        ArrayList<String> columns = new ArrayList<>();
        columns.add("ID"); // Technisch notwendig
        columns.add("Nachname");
        columns.add("Vorname");
        columns.add("Abteilung");
        columns.add("E-Mail (Arbeit)");

        // Zusatzspalten für HR/Teamleiter
        if (isPrivileged) {
            columns.add("Rolle");
            columns.add("Telefon (Arbeit)");
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

        // Doppelklick-Logik
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
            // 1. Filter: Name (Vorname oder Nachname)
            boolean nameMatch = searchText.isEmpty() ||
                    emp.getLastName().toLowerCase().contains(searchText) ||
                    emp.getFirstName().toLowerCase().contains(searchText);

            // 2. Filter: Abteilung
            boolean deptMatch = true;
            String deptName = "Keine Abteilung"; // Default

            // Abteilungs-Namen herausfinden (Employee -> Team -> Dept)
            Team team = getTeamById(emp.getTeamId());
            if (team != null) {
                Department dept = getDepartmentById(team.getDepartmentId());
                if (dept != null) {
                    deptName = dept.getName();
                    if (filterDeptId != null && dept.getId() != filterDeptId) {
                        deptMatch = false;
                    }
                } else if (filterDeptId != null) {
                    deptMatch = false; // Hat Team aber keine Dept -> rausfiltern wenn Filter aktiv
                }
            } else if (filterDeptId != null) {
                deptMatch = false; // Hat kein Team -> rausfiltern wenn Filter aktiv
            }

            // Wenn alle Filter passen -> zur Tabelle hinzufügen
            if (nameMatch && deptMatch) {
                ArrayList<Object> rowData = new ArrayList<>();
                rowData.add(emp.getId());
                rowData.add(emp.getLastName());
                rowData.add(emp.getFirstName());
                rowData.add(deptName);
                rowData.add(emp.getEMail());

                if (isPrivileged) {
                    // Rolle ermitteln
                    String roleName = "-";
                    try {
                        if (emp.getRoleManager().getActiveRole() != null) {
                            roleName = emp.getRoleManager().getActiveRole().getName();
                        }
                    } catch (Exception ex) { /* Ignorieren */ }

                    rowData.add(roleName);
                    rowData.add(emp.getPhoneNumber());
                }

                tableModel.addRow(rowData.toArray());
            }
        }
    }

    private void openSelectedProfile() {
        int row = employeeResultTable.getSelectedRow();
        int empId = (int) tableModel.getValueAt(row, 0); // ID ist Spalte 0

        // Wir rufen den Controller auf, um den Tab zu öffnen
        // Da wir direkt keine Instanz haben, nutzen wir Singleton
        UIController.getInstance().openEmployeeDetailTab(empId);
    }

    // --- Helper für Data Retrieval ---
    private Team getTeamById(int id) {
        for (Team t : ServiceLocator.getTeamContainer().getTeams()) {
            if (t.getId() == id) return t;
        }
        return null;
    }

    private Department getDepartmentById(int id) {
        for (Department d : ServiceLocator.getDepartmentContainer().getDepartments()) {
            if (d.getId() == id) return d;
        }
        return null;
    }

    // --- Helper Klasse für ComboBox ---
    private static class DepartmentItem {
        Department dept;
        public DepartmentItem(Department d) { this.dept = d; }
        @Override public String toString() { return (dept == null) ? "Alle Abteilungen" : dept.getName(); }
    }

    // --- View Interface ---
    @Override public String getViewId() { return "employee-search-view"; }
    @Override public String getViewTabTitle() { return "Mitarbeitersuche"; }
    @Override public JPanel getContent() { return this; }
    @Override public boolean equals(View view) { return view != null && view.getViewId().equals(getViewId()); }
}