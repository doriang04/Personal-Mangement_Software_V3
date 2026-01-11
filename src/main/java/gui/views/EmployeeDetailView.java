package gui.views;

import core.ServiceLocator;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class EmployeeDetailView extends JPanel implements View {

    private final String viewId;
    private String tabTitle; // Nicht mehr final, falls sich der Name ändert
    private Employee employee;
    private boolean isEditable;

    // UI Komponenten
    private JTextField txtFirstName, txtLastName, txtEmail, txtPhone, txtAddress, txtUsername;
    private JComboBox<TeamItem> cbTeam;
    private JComboBox<RoleItem> cbRole;
    private JButton btnSave;

    /**
     * ÄNDERUNG: Konstruktor nimmt jetzt die ID (int) statt String!
     */
    public EmployeeDetailView(int employeeId) {
        // 1. Mitarbeiter anhand ID finden (Stabil)
        this.employee = findEmployeeById(employeeId);

        this.isEditable = checkPermissions();

        if (this.employee != null) {
            this.tabTitle = "Profil: " + employee.getFirstName();
            this.viewId = "employee-detail-" + employee.getId();
        } else {
            this.tabTitle = "Mitarbeiter nicht gefunden";
            this.viewId = "employee-detail-error-" + employeeId;
        }

        initUI();
    }

    /**
     * Fallback für "Neuer Mitarbeiter"
     */
    public EmployeeDetailView() {
        this.tabTitle = "Neuer Mitarbeiter";
        this.viewId = "employee-detail-new";
        this.employee = null;
        this.isEditable = true;

        initUI();
    }

    private boolean checkPermissions() {
        String role = ServiceLocator.getSessionManager().getUserPermission();
        if (role == null) return false;
        role = role.toUpperCase();
        return role.contains("HR") || role.contains("ADMIN");
    }

    private Employee findEmployeeById(int id) {
        for (Employee e : ServiceLocator.getEmployeeContainer().getEmployees()) {
            if (e.getId() == id) {
                return e;
            }
        }
        return null;
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // --- Header ---
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String headerText = (employee != null)
                ? "Profil bearbeiten: " + employee.getFirstName() + " " + employee.getLastName()
                : "Neuer Mitarbeiter";

        JLabel titleLabel = new JLabel(headerText);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        header.add(titleLabel);

        if (isEditable && employee != null) {
            JLabel badge = new JLabel(" [ADMIN/HR MODUS] ");
            badge.setForeground(Color.RED);
            header.add(badge);
        }

        add(header, BorderLayout.NORTH);

        // --- Formular ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Felder
        txtUsername = new JTextField(20);
        txtFirstName = new JTextField(20);
        txtLastName = new JTextField(20);
        txtEmail = new JTextField(20);
        txtPhone = new JTextField(20);
        txtAddress = new JTextField(20);

        cbTeam = new JComboBox<>();
        cbRole = new JComboBox<>();

        loadComboBoxData();
        fillFields(); // Füllt die Felder basierend auf dem 'employee' Objekt

        enableFields(isEditable);
        txtUsername.setEditable(false); // Username bleibt fix

        // Layout
        addFormRow(formPanel, gbc, row++, "Benutzername:", txtUsername);
        addFormRow(formPanel, gbc, row++, "Vorname:", txtFirstName);
        addFormRow(formPanel, gbc, row++, "Nachname:", txtLastName);
        addFormRow(formPanel, gbc, row++, "E-Mail:", txtEmail);
        addFormRow(formPanel, gbc, row++, "Telefon:", txtPhone);
        addFormRow(formPanel, gbc, row++, "Adresse:", txtAddress);

        gbc.gridx=0; gbc.gridy=row++; gbc.gridwidth=2;
        formPanel.add(new JSeparator(), gbc); gbc.gridwidth=1;

        addFormRow(formPanel, gbc, row++, "Abteilung / Team:", cbTeam);
        addFormRow(formPanel, gbc, row++, "Aktuelle Rolle:", cbRole);

        add(new JScrollPane(formPanel), BorderLayout.CENTER);

        // Footer
        if (isEditable) {
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnSave = new JButton("Änderungen speichern");
            btnSave.setBackground(new Color(100, 200, 100));
            btnSave.addActionListener(e -> saveChanges());
            footer.add(btnSave);
            add(footer, BorderLayout.SOUTH);
        }
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        p.add(comp, gbc);
    }

    private void loadComboBoxData() {
        cbTeam.addItem(new TeamItem(null));
        for (Team t : ServiceLocator.getTeamContainer().getTeams()) {
            cbTeam.addItem(new TeamItem(t));
        }
        for (Role r : ServiceLocator.getRoleContainer().getRoles()) {
            cbRole.addItem(new RoleItem(r));
        }
    }

    private void fillFields() {
        if (employee == null) return;

        txtUsername.setText(employee.getUsername());
        txtFirstName.setText(employee.getFirstName());
        txtLastName.setText(employee.getLastName());
        txtEmail.setText(employee.getEMail());
        txtPhone.setText(employee.getPhoneNumber());
        txtAddress.setText(employee.getAddress());

        // Team selection
        int currentTeamId = employee.getTeamId();
        for (int i = 0; i < cbTeam.getItemCount(); i++) {
            TeamItem item = cbTeam.getItemAt(i);
            if (item.team != null && item.team.getId() == currentTeamId) {
                cbTeam.setSelectedIndex(i);
                break;
            }
        }

        // Role selection
        Role currentRole = null;
        try {
            if (employee.getRoleManager() != null) currentRole = employee.getRoleManager().getActiveRole();
        } catch (Exception e) { /* ignore */ }

        if (currentRole != null) {
            for (int i = 0; i < cbRole.getItemCount(); i++) {
                RoleItem item = cbRole.getItemAt(i);
                if (item.role.getId() == currentRole.getId()) {
                    cbRole.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void enableFields(boolean enable) {
        txtFirstName.setEditable(enable);
        txtLastName.setEditable(enable);
        txtEmail.setEditable(enable);
        txtPhone.setEditable(enable);
        txtAddress.setEditable(enable);
        cbTeam.setEnabled(enable);
        cbRole.setEnabled(enable);
    }

    private void saveChanges() {
        if (employee == null) return;

        int confirm = JOptionPane.showConfirmDialog(this, "Änderungen speichern?", "Bestätigung", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            // Daten im Objekt aktualisieren
            employee.setFirstName(txtFirstName.getText());
            employee.setLastName(txtLastName.getText());
            employee.setEMail(txtEmail.getText());
            employee.setPhoneNumber(txtPhone.getText());
            employee.setAddress(txtAddress.getText());

            // Team
            TeamItem selectedTeam = (TeamItem) cbTeam.getSelectedItem();
            employee.setTeamId((selectedTeam != null && selectedTeam.team != null) ? selectedTeam.team.getId() : 0);

            // Rolle
            RoleItem selectedRoleItem = (RoleItem) cbRole.getSelectedItem();
            if (selectedRoleItem != null) {
                Role newRole = selectedRoleItem.role;
                Role currentRole = employee.getRoleManager().getActiveRole();
                if (currentRole == null || currentRole.getId() != newRole.getId()) {
                    employee.getRoleManager().assignRole(newRole.getId(), LocalDate.now());
                }
            }

            JOptionPane.showMessageDialog(this, "Erfolgreich gespeichert!");

            // Tab Titel Update (optional, erfordert komplexeres Re-Rendering)
            this.tabTitle = "Profil: " + employee.getFirstName();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler: " + ex.getMessage());
        }
    }

    // Helper Klassen
    static class TeamItem {
        Team team;
        public TeamItem(Team t) { this.team = t; }
        @Override public String toString() { return (team == null) ? "- Kein Team -" : team.getName(); }
    }
    static class RoleItem {
        Role role;
        public RoleItem(Role r) { this.role = r; }
        @Override public String toString() { return role.getName(); }
    }

    @Override public String getViewId() { return viewId; }
    @Override public String getViewTabTitle() { return tabTitle; }
    @Override public JPanel getContent() { return this; }
    @Override public boolean equals(View view) { return view != null && view.getViewId().equals(this.getViewId()); }
}