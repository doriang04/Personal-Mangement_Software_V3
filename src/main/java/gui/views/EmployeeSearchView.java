package gui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import core.ServiceLocator;
import gui.UIController;
import static gui.UITheme.COLOR_ACCENT;
import static gui.UITheme.COLOR_BG_CONTENT;
import static gui.UITheme.COLOR_BORDER;
import static gui.UITheme.COLOR_HEADER_BG;
import static gui.UITheme.COLOR_HOVER;
import static gui.UITheme.COLOR_TEXT_HEADER;
import model.Department;
import model.Employee;
import model.Team;

public class EmployeeSearchView extends JPanel implements View {

    private JTextField txtSearchEmployee;
    private JComboBox<DepartmentItem> comboFilterDepartment;
    private JTable employeeResultTable;
    private DefaultTableModel tableModel;
    private int hoveredRow = -1;

    private final boolean isPrivileged;

    public EmployeeSearchView() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG_CONTENT);

        String role = ServiceLocator.getSessionManager().getUserPermission();
        if (role == null) role = "GUEST";
        this.isPrivileged = role.toUpperCase().matches(".*(HR|ADMIN|LEAD).*");

        initUI();
        searchEmployees();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(true);
        header.setBackground(COLOR_HEADER_BG);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("Mitarbeitersuche");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_TEXT_HEADER);
        header.add(titleLabel, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel contentWrapper = new JPanel(new BorderLayout(0, 20));
        contentWrapper.setOpaque(false);
        contentWrapper.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(new LineBorder(COLOR_BORDER, 1, true));

        txtSearchEmployee = new JTextField(20);
        txtSearchEmployee.putClientProperty("JTextField.placeholderText", "Name suchen...");
        txtSearchEmployee.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { searchEmployees(); }
            public void removeUpdate(DocumentEvent e) { searchEmployees(); }
            public void changedUpdate(DocumentEvent e) { searchEmployees(); }
        });

        comboFilterDepartment = new JComboBox<>();
        refreshDepartmentCombo();
        comboFilterDepartment.addActionListener(e -> searchEmployees());

        filterCard.add(new JLabel("Name:"));
        filterCard.add(txtSearchEmployee);
        filterCard.add(new JLabel("Abteilung:"));
        filterCard.add(comboFilterDepartment);

        contentWrapper.add(filterCard, BorderLayout.NORTH);

        initTable();
        JScrollPane scrollPane = new JScrollPane(employeeResultTable);
        scrollPane.setBorder(new LineBorder(COLOR_BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);
        contentWrapper.add(scrollPane, BorderLayout.CENTER);
        add(contentWrapper, BorderLayout.CENTER);
    }

    private void initTable() {
        ArrayList<String> columns = new ArrayList<>();
        columns.add("ID"); columns.add("Nachname"); columns.add("Vorname");
        columns.add("Abteilung"); columns.add("E-Mail (Arbeit)");
        if (isPrivileged) { columns.add("Rolle"); columns.add("Telefon"); }

        tableModel = new DefaultTableModel(columns.toArray(), 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        employeeResultTable = new JTable(tableModel);
        employeeResultTable.setRowHeight(40);
        employeeResultTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        employeeResultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeResultTable.setSelectionBackground(Color.WHITE);
        employeeResultTable.setSelectionForeground(Color.BLACK);
        employeeResultTable.setGridColor(COLOR_BORDER);
        employeeResultTable.setShowVerticalLines(false);
        employeeResultTable.setIntercellSpacing(new Dimension(0, 1));

        employeeResultTable.removeColumn(employeeResultTable.getColumnModel().getColumn(0));

        employeeResultTable.setDefaultRenderer(Object.class, new SelectionIndicatorRenderer());

        JTableHeader header = employeeResultTable.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        employeeResultTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && employeeResultTable.getSelectedRow() != -1) openSelectedProfile();
            }
            @Override
            public void mouseExited(MouseEvent e) { hoveredRow = -1; employeeResultTable.repaint(); }
        });

        employeeResultTable.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = employeeResultTable.rowAtPoint(e.getPoint());
                if (row != hoveredRow) { hoveredRow = row; employeeResultTable.repaint(); }
            }
        });
    }

    private class SelectionIndicatorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            boolean isHovered = (row == hoveredRow);
            if (isHovered) {
                c.setBackground(COLOR_HOVER);
                if (column == 0) c.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 5, 0, 0, COLOR_ACCENT),
                        new EmptyBorder(0, 10, 0, 5)
                ));
                else c.setBorder(new EmptyBorder(0, 15, 0, 5));
            } else {
                c.setBackground(Color.WHITE);
                c.setBorder(new EmptyBorder(0, 15, 0, 5));
            }
            return c;
        }
    }

    private void searchEmployees() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        String searchText = txtSearchEmployee.getText().toLowerCase().trim();
        DepartmentItem selectedDeptItem = (DepartmentItem) comboFilterDepartment.getSelectedItem();
        Integer filterDeptId = (selectedDeptItem != null && selectedDeptItem.dept != null) ? selectedDeptItem.dept.getId() : null;

        for (Employee emp : ServiceLocator.getEmployeeContainer().getEmployees()) {
            if (emp.getId() == ServiceLocator.getSessionManager().getCurrentUser().getId()) continue;

            Team team = ServiceLocator.getTeamContainer().getTeamById(emp.getTeamId());
            Department dept = (team != null) ? ServiceLocator.getDepartmentContainer().getDepartmentById(team.getDepartmentId()) : null;
            String deptName = (dept != null) ? dept.getName() : "Keine Abteilung";
            if (filterDeptId != null && (dept == null || dept.getId() != filterDeptId)) continue;

            boolean nameMatch = searchText.isEmpty() ||
                    emp.getLastName().toLowerCase().contains(searchText) ||
                    emp.getFirstName().toLowerCase().contains(searchText);

            if (nameMatch) {
                ArrayList<Object> row = new ArrayList<>();
                row.add(emp.getId()); row.add(emp.getLastName()); row.add(emp.getFirstName());
                row.add(deptName); row.add(emp.getEMail());
                if (isPrivileged) {
                    String rName = "-";
                    try { if(emp.getRoleManager().getActiveRole() != null) rName = emp.getRoleManager().getActiveRole().getName(); } catch(Exception ignored){}
                    row.add(rName); row.add(emp.getPhoneNumber());
                }
                tableModel.addRow(row.toArray());
            }
        }
    }

    private void openSelectedProfile() {
        int viewRow = employeeResultTable.getSelectedRow();
        if (viewRow == -1) return;
        int modelRow = employeeResultTable.convertRowIndexToModel(viewRow);
        int empId = (int) tableModel.getValueAt(modelRow, 0);
        UIController.getInstance().openTabOrFocus(new gui.views.EmployeeDetailView(empId), true);
    }

    private void refreshDepartmentCombo() {
        DepartmentItem selected = (DepartmentItem) comboFilterDepartment.getSelectedItem();
        comboFilterDepartment.removeAllItems();
        comboFilterDepartment.addItem(new DepartmentItem(null));
        for (Department d : ServiceLocator.getDepartmentContainer().getDepartments()) comboFilterDepartment.addItem(new DepartmentItem(d));
        if (selected != null) comboFilterDepartment.setSelectedItem(selected);
    }

    @Override public String getViewId() { return "employee-search-view"; }
    @Override public String getViewTabTitle() { return "Mitarbeitersuche"; }
    @Override public JPanel getContent() { return this; }
    @Override public boolean equals(View v) { return v != null && v.getViewId().equals(getViewId()); }
    @Override public void updateSelf() { refreshDepartmentCombo(); searchEmployees(); }

    private static class DepartmentItem {
        Department dept; public DepartmentItem(Department d) { this.dept = d; }
        @Override public String toString() { return (dept == null) ? "Alle Abteilungen" : dept.getName(); }
    }
}