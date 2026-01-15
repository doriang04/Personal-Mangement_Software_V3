package gui.views;

import core.ServiceLocator;
import gui.UIController; // Import the UIController
import model.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeManagementView extends JPanel implements View {

    private JList<String> employeeList;
    private DefaultListModel<String> listModel;
    private ArrayList<Employee> currentListCache;

    // --- INPUT FIELDS ---
    private JTextField txtFirstName, txtLastName, txtUsername, txtEmail, txtPhone, txtAddress;
    private JPasswordField txtPassword;
    private JComboBox<String> cbGender;
    private JComboBox<TeamItem> cbTeam;
    private JComboBox<RoleItem> cbRole;
    private JTextField txtDateOfBirth, txtHireDate;

    public EmployeeManagementView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- LEFT PANEL: List & Delete ---
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(new TitledBorder("Mitarbeiter verwalten"));
        listModel = new DefaultListModel<>();
        employeeList = new JList<>(listModel);
        leftPanel.add(new JScrollPane(employeeList), BorderLayout.CENTER);

        JButton btnDelete = new JButton("Ausgewählten Mitarbeiter löschen");
        btnDelete.setBackground(new Color(255, 100, 100));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(_ -> deleteSelectedEmployee());
        leftPanel.add(btnDelete, BorderLayout.SOUTH);

        // --- RIGHT PANEL: Add ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(new TitledBorder("Neuen Mitarbeiter anlegen"));
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Initialize fields
        txtFirstName = new JTextField(20);
        txtLastName = new JTextField(20);
        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);
        txtEmail = new JTextField(20);
        txtPhone = new JTextField(20);
        txtAddress = new JTextField(20);
        txtDateOfBirth = new JTextField("YYYY-MM-DD");
        txtHireDate = new JTextField(LocalDate.now().toString());
        cbGender = new JComboBox<>(new String[]{"Männlich", "Weiblich", "Divers"});
        cbTeam = new JComboBox<>();
        cbRole = new JComboBox<>();
        loadComboBoxData();

        // Add fields to layout
        int row = 0;
        addFormRow(formPanel, gbc, row++, "Vorname*:", txtFirstName);
        addFormRow(formPanel, gbc, row++, "Nachname*:", txtLastName);
        addFormRow(formPanel, gbc, row++, "Benutzername*:", txtUsername);
        addFormRow(formPanel, gbc, row++, "Passwort*:", txtPassword);
        addFormRow(formPanel, gbc, row++, "Team*:", cbTeam);
        addFormRow(formPanel, gbc, row++, "Rolle*:", cbRole);
        gbc.gridx=0; gbc.gridy=row++; gbc.gridwidth=2; formPanel.add(new JSeparator(), gbc); gbc.gridwidth=1;
        addFormRow(formPanel, gbc, row++, "E-Mail:", txtEmail);
        addFormRow(formPanel, gbc, row++, "Telefon:", txtPhone);
        addFormRow(formPanel, gbc, row++, "Adresse:", txtAddress);
        addFormRow(formPanel, gbc, row++, "Geburtsdatum:", txtDateOfBirth);
        addFormRow(formPanel, gbc, row++, "Einstellungsdatum:", txtHireDate);
        addFormRow(formPanel, gbc, row++, "Geschlecht:", cbGender);

        JButton btnAdd = new JButton("Mitarbeiter hinzufügen");
        btnAdd.addActionListener(e -> createEmployee());
        rightPanel.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        rightPanel.add(btnAdd, BorderLayout.SOUTH);

        // Split Pane for layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(350);
        add(splitPane, BorderLayout.CENTER);

        refreshList();
    }

    private void createEmployee() {
        // --- 1. Validation ---
        List<String> missingFields = new ArrayList<>();
        if (txtFirstName.getText().trim().isEmpty()) missingFields.add("Vorname");
        if (txtLastName.getText().trim().isEmpty()) missingFields.add("Nachname");
        if (txtUsername.getText().trim().isEmpty()) missingFields.add("Benutzername");
        if (new String(txtPassword.getPassword()).trim().isEmpty()) missingFields.add("Passwort");
        if (cbTeam.getSelectedItem() == null || ((TeamItem) cbTeam.getSelectedItem()).team == null) missingFields.add("Team");
        if (cbRole.getSelectedItem() == null || ((RoleItem) cbRole.getSelectedItem()).role == null) missingFields.add("Rolle");

        if (!missingFields.isEmpty()) {
            String message = "Folgende Pflichtfelder müssen ausgefüllt werden:\n" + String.join(", ", missingFields);
            JOptionPane.showMessageDialog(this, message, "Fehlende Eingaben", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // --- 2. Data collection ---
            String firstName = txtFirstName.getText().trim();
            String lastName = txtLastName.getText().trim();
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());
            int teamId = ((TeamItem) cbTeam.getSelectedItem()).team.getId();
            Role selectedRole = ((RoleItem) cbRole.getSelectedItem()).role;
            String email = txtEmail.getText().trim().isEmpty() ? "n/a" : txtEmail.getText().trim();
            String phone = txtPhone.getText().trim().isEmpty() ? "n/a" : txtPhone.getText().trim();
            String address = txtAddress.getText().trim().isEmpty() ? "n/a" : txtAddress.getText().trim();
            char gender = ((String)cbGender.getSelectedItem()).charAt(0);
            LocalDate hireDate = LocalDate.parse(txtHireDate.getText());
            java.util.Date hireDateAsDate = java.sql.Date.valueOf(hireDate);

            // --- 3. Create new Employee object ---
            int newId = ServiceLocator.getEmployeeContainer().getEmployees().stream()
                    .mapToInt(Employee::getId).max().orElse(0) + 1;

            Employee newEmp = new Employee(
                    newId, teamId, username, password, firstName, lastName, email,
                    null, address, gender, hireDateAsDate, 0, true, phone
            );

            if(newEmp.getRoleManager() != null) {
                newEmp.getRoleManager().assignRole(selectedRole.getId(), hireDate);
            }

            // --- 4. Save and update UI ---
            ServiceLocator.getEmployeeContainer().addEmployee(newEmp);

            // Since we modified core data, trigger a global UI refresh.
            // This will update all views, including this one's list.
            UIController.getInstance().updateMainWindow();

            clearForm(); // Clear the form fields locally.

            JOptionPane.showMessageDialog(this, "Mitarbeiter '" + firstName + " " + lastName + "' (ID: " + newId + ") erfolgreich angelegt.", "Erfolg", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ein unerwarteter Fehler ist aufgetreten:\n" + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.1;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 0.9;
        p.add(comp, gbc);
    }

    private void loadComboBoxData() {
        cbTeam.removeAllItems();
        cbRole.removeAllItems();

        cbTeam.addItem(new TeamItem(null));
        ServiceLocator.getTeamContainer().getTeams().forEach(t -> cbTeam.addItem(new TeamItem(t)));

        cbRole.addItem(new RoleItem(null));
        ServiceLocator.getRoleContainer().getRoles().forEach(r -> cbRole.addItem(new RoleItem(r)));
    }

    private void clearForm() {
        txtFirstName.setText("");
        txtLastName.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        txtDateOfBirth.setText("YYYY-MM-DD");
        txtHireDate.setText(LocalDate.now().toString());
        cbTeam.setSelectedIndex(0);
        cbRole.setSelectedIndex(0);
        cbGender.setSelectedIndex(0);
    }

    private void refreshList() {
        listModel.clear();
        currentListCache = new ArrayList<>(ServiceLocator.getEmployeeContainer().getEmployees());
        for (Employee e : currentListCache) {
            listModel.addElement(e.getId() + " | " + e.getFirstName() + " " + e.getLastName() + " (" + e.getUsername() + ")");
        }
    }

    private void deleteSelectedEmployee() {
        int index = employeeList.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Bitte wählen Sie einen Mitarbeiter aus.");
            return;
        }

        Employee toDelete = currentListCache.get(index);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Sollen " + toDelete.getFirstName() + " " + toDelete.getLastName() + " wirklich gelöscht werden?",
                "Löschen bestätigen", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                ServiceLocator.getEmployeeContainer().removeEmployee(toDelete);

                // Since we modified core data, trigger a global UI refresh.
                UIController.getInstance().updateMainWindow();

                JOptionPane.showMessageDialog(this, "Mitarbeiter gelöscht!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Fehler beim Löschen: " + ex.getMessage());
            }
        }
    }

    // Helper inner classes
    static class TeamItem {
        Team team;
        public TeamItem(Team t) { this.team = t; }
        @Override public String toString() { return (team == null) ? "- Kein Team -" : team.getName(); }
    }
    static class RoleItem {
        Role role;
        public RoleItem(Role r) { this.role = r; }
        @Override public String toString() { return (role == null) ? "- Keine Rolle -" : role.getName(); }
    }

    // View interface methods
    @Override public String getViewId() { return "admin-employee-management"; }
    @Override public String getViewTabTitle() { return "Personalverwaltung"; }
    @Override public JPanel getContent() { return this; }
    @Override public boolean equals(View view) { return view != null && view.getViewId().equals(getViewId()); }

    @Override
    public void updateSelf() {
        refreshList();
        loadComboBoxData();
    }
}