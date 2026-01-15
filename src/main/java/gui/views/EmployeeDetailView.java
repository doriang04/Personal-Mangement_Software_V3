package gui.views;

import core.ServiceLocator;
import gui.components.RoleHistoryPanel;
import gui.components.SkillHistoryPanel;
import model.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.time.LocalDate;

public class EmployeeDetailView extends JPanel implements View {

    private final String viewId;
    private String tabTitle;
    private final Employee employee; // Nun final, da nur im Konstruktor gesetzt
    private final boolean canEdit; // Finale Berechtigung, ändert sich nicht

    // --- ZUSTANDS-VARIABLEN ---
    private boolean isInEditMode = false;
    private boolean hasUnsavedChanges = false;

    // UI Komponenten
    private JTextField txtFirstName, txtLastName, txtEmail, txtPhone, txtAddress, txtUsername;
    private JComboBox<TeamItem> cbTeam;
    private JComboBox<RoleItem> cbRole;
    private JButton btnShowRoleHistory;
    private JButton btnShowSkillHistory; // NEU: Button für Skill-Historie

    // Footer Komponenten
    private JButton btnPrimaryAction; // Wird zu "Bearbeiten", "Speichern", "Verlassen"
    private JButton btnDiscard;       // "Änderungen verwerfen"
    private JLabel lblUnsavedChanges; // "* Ungespeicherte Änderungen"

    /**
     * Konstruktor zur Anzeige eines bestehenden Mitarbeiters.
     * Dies ist nun der einzige Konstruktor.
     * @param employeeId Die ID des anzuzeigenden Mitarbeiters.
     */
    public EmployeeDetailView(int employeeId) {
        this.employee = findEmployeeById(employeeId);
        this.canEdit = checkPermissions();

        if (this.employee != null) {
            this.tabTitle = "Profil: " + employee.getFirstName();
            this.viewId = "employee-detail-" + employee.getId();
        } else {
            this.tabTitle = "Mitarbeiter nicht gefunden";
            this.viewId = "employee-detail-error-" + employeeId;
        }
        initUI();
    }

    // --- DER KONSTRUKTOR ZUM ERSTELLEN NEUER MITARBEITER WURDE ENTFERNT ---

    private boolean checkPermissions() {
        String role = ServiceLocator.getSessionManager().getUserPermission();
        if (role == null) return false;
        role = role.toUpperCase();
        return role.contains("HR") || role.contains("ADMIN");
    }

    private Employee findEmployeeById(int id) {
        // Besser: ServiceLocator.getEmployeeContainer().getEmployeeById(id);
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
        // Der Titel hängt davon ab, ob der Mitarbeiter gefunden wurde.
        String headerText = (employee != null)
                ? "Profil von: " + employee.getFirstName() + " " + employee.getLastName()
                : "Mitarbeiter nicht gefunden";

        JLabel titleLabel = new JLabel(headerText);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        header.add(titleLabel);
        add(header, BorderLayout.NORTH);

        // Wenn kein Mitarbeiter gefunden wurde, eine Fehlermeldung anzeigen und den UI-Aufbau abbrechen.
        if (employee == null) {
            JPanel errorPanel = new JPanel(new GridBagLayout());
            errorPanel.add(new JLabel("Der angeforderte Mitarbeiter konnte nicht gefunden werden."));
            add(errorPanel, BorderLayout.CENTER);
            return; // Wichtig: Bricht den weiteren Aufbau ab.
        }

        // --- Formular (wird nur erstellt, wenn Mitarbeiter existiert) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Felder initialisieren
        txtUsername = new JTextField(20);
        txtFirstName = new JTextField(20);
        txtLastName = new JTextField(20);
        txtEmail = new JTextField(20);
        txtPhone = new JTextField(20);
        txtAddress = new JTextField(20);
        cbTeam = new JComboBox<>();
        cbRole = new JComboBox<>();

        // Felder befüllen und Listener hinzufügen
        loadComboBoxData();
        fillFields();
        addChangeListeners();

        // Layout
        addFormRow(formPanel, gbc, row++, "Benutzername:", txtUsername);
        addFormRow(formPanel, gbc, row++, "Vorname:", txtFirstName);
        addFormRow(formPanel, gbc, row++, "Nachname:", txtLastName);
        addFormRow(formPanel, gbc, row++, "E-Mail:", txtEmail);
        addFormRow(formPanel, gbc, row++, "Telefon:", txtPhone);
        addFormRow(formPanel, gbc, row++, "Adresse:", txtAddress);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        addFormRow(formPanel, gbc, row++, "Abteilung / Team:", cbTeam);
        addFormRow(formPanel, gbc, row++, "Aktuelle Rolle:", cbRole);

        JPanel historyButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        historyButtonPanel.setOpaque(false); // Transparenter Hintergrund

        btnShowRoleHistory = new JButton("Rollenhistorie...");
        btnShowRoleHistory.addActionListener(_ -> showRoleHistoryDialog());
        historyButtonPanel.add(btnShowRoleHistory);

        // NEU: Button für Skill-Historie hinzufügen
        btnShowSkillHistory = new JButton("Skill-Historie...");
        btnShowSkillHistory.addActionListener(_ -> showSkillHistoryDialog());
        historyButtonPanel.add(btnShowSkillHistory);

        gbc.gridx = 1;
        gbc.gridy = row++;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(historyButtonPanel, gbc);
        // -- ENDE DES GEÄNDERTEN BLOCKS --

        add(new JScrollPane(formPanel), BorderLayout.CENTER);

        // --- Footer mit dynamischen Buttons (nur für berechtigte Benutzer) ---
        if (canEdit) {
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));

            lblUnsavedChanges = new JLabel("* Ungespeicherte Änderungen");
            lblUnsavedChanges.setForeground(Color.BLUE.darker());

            btnDiscard = new JButton("Änderungen verwerfen");
            btnDiscard.addActionListener(_ -> discardChanges());

            btnPrimaryAction = new JButton(); // Text wird dynamisch gesetzt
            btnPrimaryAction.setBackground(new Color(100, 200, 100));
            btnPrimaryAction.addActionListener(_ -> handlePrimaryAction());

            footer.add(lblUnsavedChanges);
            footer.add(btnDiscard);
            footer.add(btnPrimaryAction);
            add(footer, BorderLayout.SOUTH);
        }

        // Initialen UI-Zustand setzen
        updateUiForCurrentState();
    }

    private void updateUiForCurrentState() {
        // Felder (de)aktivieren
        enableFields(isInEditMode);
        txtUsername.setEditable(false); // Username ist immer schreibgeschützt

        if (btnPrimaryAction == null) return; // Footer existiert nicht (keine Rechte)

        if (isInEditMode) {
            lblUnsavedChanges.setVisible(hasUnsavedChanges);
            btnDiscard.setVisible(hasUnsavedChanges);

            if (hasUnsavedChanges) {
                btnPrimaryAction.setText("Änderungen speichern");
            } else {
                // Der Fall "Neuer Mitarbeiter" existiert nicht mehr.
                btnPrimaryAction.setText("Bearbeitungsmodus verlassen");
            }
        } else {
            // Ansichtsmodus
            btnPrimaryAction.setText("Profil bearbeiten");
            btnDiscard.setVisible(false);
            lblUnsavedChanges.setVisible(false);
        }
    }

    private void handlePrimaryAction() {
        if (isInEditMode) {
            // Die Bedingung für das Erstellen eines neuen Mitarbeiters wurde entfernt.
            if (hasUnsavedChanges) {
                saveChanges();
            } else {
                // Bearbeitungsmodus ohne Änderungen verlassen
                isInEditMode = false;
                updateUiForCurrentState();
            }
        } else {
            // Bearbeitungsmodus starten
            isInEditMode = true;
            hasUnsavedChanges = false; // Zurücksetzen für die neue Bearbeitungssession
            updateUiForCurrentState();
        }
    }

    private void discardChanges() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Möchten Sie wirklich alle ungespeicherten Änderungen verwerfen?",
                "Änderungen verwerfen", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            isInEditMode = false;
            hasUnsavedChanges = false;
            fillFields(); // Felder mit den ursprünglichen Daten neu befüllen
            updateUiForCurrentState();
        }
    }

    private void addChangeListeners() {
        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { markAsChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { markAsChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { markAsChanged(); }
        };

        txtFirstName.getDocument().addDocumentListener(dl);
        txtLastName.getDocument().addDocumentListener(dl);
        txtEmail.getDocument().addDocumentListener(dl);
        txtPhone.getDocument().addDocumentListener(dl);
        txtAddress.getDocument().addDocumentListener(dl);

        cbTeam.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) markAsChanged();
        });
        cbRole.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) markAsChanged();
        });
    }

    private void markAsChanged() {
        if (isInEditMode && !hasUnsavedChanges) {
            hasUnsavedChanges = true;
            updateUiForCurrentState();
        }
    }

    private void saveChanges() {
        // Der Block zum Erstellen eines neuen Mitarbeiters wurde entfernt.
        // Die Methode geht davon aus, dass 'employee' immer existiert.
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

            // --- WICHTIG: Hier käme der Datenbank-Speicheraufruf ---
            // ServiceLocator.getDatabaseManager().saveEmployee(employee);

            JOptionPane.showMessageDialog(this, "Erfolgreich gespeichert!");

            // Bearbeitungsmodus verlassen
            isInEditMode = false;
            hasUnsavedChanges = false;
            tabTitle = "Profil: " + employee.getFirstName(); // Tab-Titel aktualisieren
            updateUiForCurrentState();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler beim Speichern: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRoleHistoryDialog() {
        JDialog historyDialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), "Rollenhistorie", Dialog.ModalityType.APPLICATION_MODAL);

        RoleHistoryPanel panel = new RoleHistoryPanel(this.employee, this.canEdit && this.isInEditMode);

        historyDialog.setContentPane(panel);
        historyDialog.setSize(600, 400);
        historyDialog.setLocationRelativeTo(this);
        historyDialog.setVisible(true);

        fillFields();
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

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        p.add(comp, gbc);
    }

    private void loadComboBoxData() {
        cbTeam.removeAllItems();
        cbRole.removeAllItems();
        cbTeam.addItem(new TeamItem(null));
        for (Team t : ServiceLocator.getTeamContainer().getTeams()) {
            cbTeam.addItem(new TeamItem(t));
        }
        for (Role r : ServiceLocator.getRoleContainer().getRoles()) {
            cbRole.addItem(new RoleItem(r));
        }
    }

    private void fillFields() {
        // Die Methode wird dank der Logik in initUI nur aufgerufen, wenn employee existiert.
        txtUsername.setText(employee.getUsername());
        txtFirstName.setText(employee.getFirstName());
        txtLastName.setText(employee.getLastName());
        txtEmail.setText(employee.getEMail());
        txtPhone.setText(employee.getPhoneNumber());
        txtAddress.setText(employee.getAddress());
        int currentTeamId = employee.getTeamId();
        for (int i = 0; i < cbTeam.getItemCount(); i++) {
            TeamItem item = cbTeam.getItemAt(i);
            if (item.team != null && item.team.getId() == currentTeamId) {
                cbTeam.setSelectedIndex(i);
                break;
            }
        }
        Role currentRole = employee.getRoleManager() != null ? employee.getRoleManager().getActiveRole() : null;
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

    private void showSkillHistoryDialog() {
        JDialog historyDialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), "Skill-Historie", Dialog.ModalityType.APPLICATION_MODAL);

        // Das Panel ist nur editierbar, wenn der Benutzer die Rechte hat UND der Bearbeitungsmodus aktiv ist.
        SkillHistoryPanel panel = new SkillHistoryPanel(this.employee, this.canEdit && this.isInEditMode);

        historyDialog.setContentPane(panel);
        historyDialog.setSize(700, 450);
        historyDialog.setLocationRelativeTo(this);
        historyDialog.setVisible(true);

        // Nach dem Schließen des Dialogs ist hier keine Aktualisierung der Hauptfelder
        // notwendig, im Gegensatz zur Rollenhistorie.
    }

    // Unveränderte Helper-Klassen und Interface-Methoden
    static class TeamItem { Team team; public TeamItem(Team t) { this.team = t; } @Override public String toString() { return (team == null) ? "- Kein Team -" : team.getName(); } }
    static class RoleItem { Role role; public RoleItem(Role r) { this.role = r; } @Override public String toString() { return role.getName(); } }
    @Override public String getViewId() { return viewId; }
    @Override public String getViewTabTitle() { return tabTitle; }
    @Override public JPanel getContent() { return this; }
    @Override public boolean equals(View view) { return view != null && view.getViewId().equals(this.getViewId()); }
}