package gui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import core.ServiceLocator;
import gui.UIController;
import static gui.UITheme.COLOR_ACCENT;
import static gui.UITheme.COLOR_BG_CONTENT;
import static gui.UITheme.COLOR_BORDER;
import static gui.UITheme.COLOR_HEADER_BG;
import static gui.UITheme.COLOR_HOVER;
import static gui.UITheme.COLOR_TEXT_HEADER;
import static gui.UITheme.createModernCard;
import static gui.UITheme.createStyledButton;
import model.Employee;
import model.Role;
import model.Team;

public class EmployeeManagementView extends JPanel implements View {

    private JList<Employee> employeeList;
    private DefaultListModel<Employee> listModel;
    private int hoveredIndex = -1;

    private JTextField txtFirstName, txtLastName, txtUsername, txtEmail, txtPhone, txtAddress;
    private JPasswordField txtPassword;
    private JComboBox<String> cbGender;
    private JComboBox<TeamItem> cbTeam;
    private JComboBox<RoleItem> cbRole;
    private JTextField txtDateOfBirth, txtHireDate;

    public EmployeeManagementView() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG_CONTENT);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(true);
        header.setBackground(COLOR_HEADER_BG);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));
        JLabel titleLabel = new JLabel("Personalverwaltung");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_TEXT_HEADER);
        header.add(titleLabel, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel leftPanel = createLeftPanel();
        JPanel rightPanel = createRightPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(400);
        splitPane.setDividerSize(1);
        splitPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        splitPane.setOpaque(false);

        add(splitPane, BorderLayout.CENTER);
        refreshList();
    }

    private JPanel createLeftPanel() {
        JPanel container = new JPanel(new BorderLayout(0, 15));
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(0, 0, 0, 10));

        JPanel card = createModernCard("Mitarbeiter-Verzeichnis");
        listModel = new DefaultListModel<>();
        employeeList = new JList<>(listModel);
        employeeList.setCellRenderer(new EmployeeListRenderer());
        employeeList.setFixedCellHeight(50);
        employeeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeList.setSelectionBackground(Color.WHITE);
        employeeList.setSelectionForeground(Color.BLACK);

        employeeList.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int index = employeeList.locationToIndex(e.getPoint());
                if (index != hoveredIndex) { hoveredIndex = index; employeeList.repaint(); }
            }
        });
        employeeList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) { hoveredIndex = -1; employeeList.repaint(); }
        });

        JScrollPane scrollPane = new JScrollPane(employeeList);
        scrollPane.setBorder(new LineBorder(COLOR_BORDER));
        card.add(scrollPane, BorderLayout.CENTER);

        JButton btnDelete = createStyledButton("Mitarbeiter löschen", false);
        btnDelete.addActionListener(_ -> {
            try { deleteSelectedEmployee(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Fehler: " + ex.getMessage()); }
        });
        card.add(btnDelete, BorderLayout.SOUTH);

        container.add(card, BorderLayout.CENTER);
        return container;
    }

    private JPanel createRightPanel() {
        JPanel card = createModernCard("Neuen Mitarbeiter erfassen");
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 2, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtFirstName = new JTextField();
        txtLastName = new JTextField();
        txtUsername = new JTextField();
        txtPassword = new JPasswordField();
        txtEmail = new JTextField();
        txtPhone = new JTextField();
        txtAddress = new JTextField();
        txtDateOfBirth = new JTextField("YYYY-MM-DD");
        txtHireDate = new JTextField(LocalDate.now().toString());
        cbGender = new JComboBox<>(new String[]{"Männlich", "Weiblich", "Divers"});
        cbTeam = new JComboBox<>();
        cbRole = new JComboBox<>();
        loadComboBoxData();

        int row = 0;
        addModernFormRow(formPanel, gbc, row++, "Vorname*", txtFirstName);
        addModernFormRow(formPanel, gbc, row++, "Nachname*", txtLastName);
        addModernFormRow(formPanel, gbc, row++, "Benutzername*", txtUsername);
        addModernFormRow(formPanel, gbc, row++, "Passwort*", txtPassword);
        addModernFormRow(formPanel, gbc, row++, "Team*", cbTeam);
        addModernFormRow(formPanel, gbc, row++, "Rolle*", cbRole);

        gbc.gridy = row++; gbc.insets = new Insets(15, 0, 15, 0);
        formPanel.add(new JSeparator(), gbc);
        gbc.insets = new Insets(8, 0, 2, 0);

        addModernFormRow(formPanel, gbc, row++, "E-Mail", txtEmail);
        addModernFormRow(formPanel, gbc, row++, "Telefon", txtPhone);
        addModernFormRow(formPanel, gbc, row++, "Adresse", txtAddress);
        addModernFormRow(formPanel, gbc, row++, "Einstellungsdatum", txtHireDate);
        addModernFormRow(formPanel, gbc, row++, "Geburtsdatum", txtDateOfBirth);
        addModernFormRow(formPanel, gbc, row++, "Geschlecht", cbGender);

        JButton btnAdd = createStyledButton("Mitarbeiter hinzufügen", true);
        btnAdd.addActionListener(e -> createEmployee());

        card.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        card.add(btnAdd, BorderLayout.SOUTH);

        return card;
    }

    private void addModernFormRow(JPanel p, GridBagConstraints gbc, int row, String labelText, JComponent comp) {
        gbc.gridy = row;
        JPanel rowPanel = new JPanel(new BorderLayout(0, 5));
        rowPanel.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(COLOR_TEXT_HEADER);
        rowPanel.add(lbl, BorderLayout.NORTH);
        rowPanel.add(comp, BorderLayout.CENTER);
        p.add(rowPanel, gbc);
    }

    private class EmployeeListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Employee emp = (Employee) value;
            JPanel panel = new JPanel(new BorderLayout(10, 0));
            panel.setPreferredSize(new Dimension(0, 50));
            boolean isHovered = (index == hoveredIndex);
            if (isHovered) {
                panel.setBackground(COLOR_HOVER);
                panel.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, COLOR_ACCENT));
            } else {
                panel.setBackground(Color.WHITE);
                panel.setBorder(new EmptyBorder(0, 15, 0, 5));
            }
            JLabel nameLabel = new JLabel("  " + emp.getFirstName() + " " + emp.getLastName());
            nameLabel.setFont(new Font("SansSerif", isSelected ? Font.BOLD : Font.PLAIN, 13));
            JLabel idLabel = new JLabel("ID: " + emp.getId() + "  ");
            idLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
            idLabel.setForeground(Color.GRAY);
            panel.add(nameLabel, BorderLayout.CENTER);
            panel.add(idLabel, BorderLayout.EAST);
            return panel;
        }
    }

    private void createEmployee() {
        if (txtFirstName.getText().trim().isEmpty() || txtLastName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pflichtfelder ausfüllen!", "Warnung", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int newId = ServiceLocator.getEmployeeContainer().getEmployees().stream()
                    .mapToInt(Employee::getId).max().orElse(0) + 1;
            Employee newEmp = new Employee(
                    newId, ((TeamItem) Objects.requireNonNull(cbTeam.getSelectedItem())).team.getId(),
                    txtUsername.getText().trim(), new String(txtPassword.getPassword()),
                    txtFirstName.getText().trim(), txtLastName.getText().trim(),
                    txtEmail.getText().trim(), null, txtAddress.getText().trim(),
                    ((String) Objects.requireNonNull(cbGender.getSelectedItem())).charAt(0),
                    java.sql.Date.valueOf(LocalDate.parse(txtHireDate.getText())),
                    0, true, txtPhone.getText().trim()
            );
            Role selRole = ((RoleItem) Objects.requireNonNull(cbRole.getSelectedItem())).role;
            if(selRole != null) newEmp.getRoleManager().assignRole(selRole.getId(), LocalDate.now());
            ServiceLocator.getEmployeeContainer().addEmployee(newEmp);
            UIController.getInstance().updateMainWindow();
            clearForm();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler: " + ex.getMessage());
        }
    }

    private void deleteSelectedEmployee() throws Exception {
        Employee selected = employeeList.getSelectedValue();
        if (selected == null) return;
        if (JOptionPane.showConfirmDialog(this, "Löschen?") == JOptionPane.YES_OPTION) {
            ServiceLocator.getEmployeeContainer().removeEmployee(selected);
            UIController.getInstance().updateMainWindow();
        }
    }

    private void refreshList() {
        listModel.clear();
        ServiceLocator.getEmployeeContainer().getEmployees().forEach(listModel::addElement);
    }

    private void loadComboBoxData() {
        cbTeam.removeAllItems(); cbRole.removeAllItems();
        cbTeam.addItem(new TeamItem(null));
        ServiceLocator.getTeamContainer().getTeams().forEach(t -> cbTeam.addItem(new TeamItem(t)));
        cbRole.addItem(new RoleItem(null));
        ServiceLocator.getRoleContainer().getRoles().forEach(r -> cbRole.addItem(new RoleItem(r)));
    }

    private void clearForm() {
        txtFirstName.setText(""); txtLastName.setText(""); txtUsername.setText("");
        txtPassword.setText(""); txtEmail.setText("");
    }

    static class TeamItem { Team team; public TeamItem(Team t) { this.team = t; } @Override public String toString() { return (team == null) ? "- Kein Team -" : team.getName(); } }
    static class RoleItem { Role role; public RoleItem(Role r) { this.role = r; } @Override public String toString() { return (role == null) ? "- Keine Rolle -" : role.getName(); } }

    @Override public String getViewId() { return "admin-employee-management"; }
    @Override public String getViewTabTitle() { return "Personalverwaltung"; }
    @Override public JPanel getContent() { return this; }
    @Override public boolean equals(View view) { return view != null && view.getViewId().equals(getViewId()); }
    @Override public void updateSelf() { refreshList(); loadComboBoxData(); }
}
