package gui.views;

import core.ServiceLocator;
import gui.UIController; // Import the UIController
import gui.components.RoleHistoryPanel;
import gui.components.SkillHistoryPanel;
import model.Employee;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class MyProfileView extends JPanel implements View {

    private Employee currentUser;

    // --- STATE VARIABLES ---
    private boolean isInEditMode = false;
    private boolean hasUnsavedChanges = false;

    // Editable fields
    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtAddress;
    private JPasswordField txtPassword;

    // Permanently read-only fields
    private JTextField txtId;
    private JTextField txtUsername;
    private JTextField txtRole;
    private JTextField txtTeam;
    private JTextField txtSkills;

    // Footer components
    private JButton btnPrimaryAction;
    private JButton btnDiscard;
    private JLabel lblUnsavedChanges;

    public MyProfileView() {
        setLayout(new BorderLayout());
        this.currentUser = ServiceLocator.getSessionManager().getCurrentUser();
        initUI();
    }

    private void initUI() {
        // --- Header ---
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel title = new JLabel("Mein Profil");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        header.add(title);
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(header, BorderLayout.NORTH);

        // --- Form Panel ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // -- Initialize fields --
        txtId = createReadOnlyField();
        txtUsername = createReadOnlyField();
        txtRole = createReadOnlyField();
        txtTeam = createReadOnlyField();
        txtSkills = createReadOnlyField();

        txtFirstName = new JTextField(20);
        txtLastName = new JTextField(20);
        txtEmail = new JTextField(20);
        txtPhone = new JTextField(20);
        txtAddress = new JTextField(20);
        txtPassword = new JPasswordField(20);

        // -- Build layout --
        int row = 0;
        addFormRow(formPanel, gbc, row++, "Mitarbeiter-ID:", txtId);
        addFormRow(formPanel, gbc, row++, "Benutzername:", txtUsername);

        JPanel rolePanel = new JPanel(new BorderLayout(5, 0));
        rolePanel.add(txtRole, BorderLayout.CENTER);
        JButton btnHistory = new JButton("Historie");
        btnHistory.setMargin(new Insets(2, 5, 2, 5));
        btnHistory.addActionListener(_ -> showRoleHistoryDialog());
        rolePanel.add(btnHistory, BorderLayout.EAST);
        addFormRow(formPanel, gbc, row++, "Rolle:", rolePanel);

        JPanel skillPanel = new JPanel(new BorderLayout(5, 0));
        skillPanel.add(txtSkills, BorderLayout.CENTER);
        JButton btnSkillHistory = new JButton("Historie");
        btnSkillHistory.setMargin(new Insets(2, 5, 2, 5));
        btnSkillHistory.addActionListener(_ -> showSkillHistoryDialog(false));
        skillPanel.add(btnSkillHistory, BorderLayout.EAST);
        addFormRow(formPanel, gbc, row++, "Skills:", skillPanel);

        addFormRow(formPanel, gbc, row++, "Team / Abteilung:", txtTeam);

        JSeparator sep = new JSeparator();
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formPanel.add(sep, gbc);
        gbc.gridwidth = 1;

        addFormRow(formPanel, gbc, row++, "Vorname:", txtFirstName);
        addFormRow(formPanel, gbc, row++, "Nachname:", txtLastName);
        addFormRow(formPanel, gbc, row++, "E-Mail:", txtEmail);
        addFormRow(formPanel, gbc, row++, "Telefon:", txtPhone);
        addFormRow(formPanel, gbc, row++, "Adresse:", txtAddress);
        addFormRow(formPanel, gbc, row++, "Passwort:", txtPassword);

        add(new JScrollPane(formPanel), BorderLayout.CENTER);

        // --- Footer ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblUnsavedChanges = new JLabel("* Ungespeicherte Änderungen");
        lblUnsavedChanges.setForeground(Color.BLUE.darker());

        btnDiscard = new JButton("Änderungen verwerfen");
        btnDiscard.addActionListener(_ -> discardChanges());

        btnPrimaryAction = new JButton();
        btnPrimaryAction.setBackground(new Color(100, 200, 100));
        btnPrimaryAction.addActionListener(_ -> handlePrimaryAction());

        footer.add(lblUnsavedChanges);
        footer.add(btnDiscard);
        footer.add(btnPrimaryAction);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 20));
        add(footer, BorderLayout.SOUTH);

        // Load data, add listeners, and set initial UI state
        loadData();
        addChangeListeners();
        updateUiForCurrentState();
    }

    private void updateUiForCurrentState() {
        enableEditableFields(isInEditMode);

        if (isInEditMode) {
            lblUnsavedChanges.setVisible(hasUnsavedChanges);
            btnDiscard.setVisible(hasUnsavedChanges);
            btnPrimaryAction.setText(hasUnsavedChanges ? "Änderungen speichern" : "Bearbeitungsmodus verlassen");
        } else {
            btnPrimaryAction.setText("Profil bearbeiten");
            btnDiscard.setVisible(false);
            lblUnsavedChanges.setVisible(false);
        }
    }

    private void enableEditableFields(boolean enable) {
        txtFirstName.setEditable(enable);
        txtLastName.setEditable(enable);
        txtEmail.setEditable(enable);
        txtPhone.setEditable(enable);
        txtAddress.setEditable(enable);
        txtPassword.setEditable(enable);

        Color bgColor = enable ? Color.WHITE : new Color(240, 240, 240);
        txtFirstName.setBackground(bgColor);
        txtLastName.setBackground(bgColor);
        txtEmail.setBackground(bgColor);
        txtPhone.setBackground(bgColor);
        txtAddress.setBackground(bgColor);
        txtPassword.setBackground(bgColor);
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
        txtPassword.getDocument().addDocumentListener(dl);
    }

    private void markAsChanged() {
        if (isInEditMode && !hasUnsavedChanges) {
            hasUnsavedChanges = true;
            updateUiForCurrentState();
        }
    }

    private void handlePrimaryAction() {
        if (isInEditMode) {
            if (hasUnsavedChanges) {
                saveChanges();
            } else {
                isInEditMode = false;
                updateUiForCurrentState();
            }
        } else {
            isInEditMode = true;
            hasUnsavedChanges = false;
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
            loadData();
            updateUiForCurrentState();
        }
    }

    private void saveChanges() {
        if (currentUser == null) return;

        if (txtFirstName.getText().trim().isEmpty() || txtLastName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vor- und Nachname dürfen nicht leer sein.", "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentUser.setFirstName(txtFirstName.getText().trim());
        currentUser.setLastName(txtLastName.getText().trim());
        currentUser.setEMail(txtEmail.getText().trim());
        currentUser.setPhoneNumber(txtPhone.getText().trim());
        currentUser.setAddress(txtAddress.getText().trim());
        // Only update password if the field is not empty
        if (new String(txtPassword.getPassword()).trim().length() > 0) {
            currentUser.setPassword(new String(txtPassword.getPassword()));
        }

        // Here you would call the database persistence layer
        // ServiceLocator.getDatabaseManager().saveEmployee(currentUser);
        JOptionPane.showMessageDialog(this, "Profil erfolgreich aktualisiert!");

        isInEditMode = false;
        hasUnsavedChanges = false;

        // The underlying employee data has changed. Trigger a global UI refresh
        // to ensure all views (including this one) are updated consistently.
        UIController.getInstance().updateMainWindow();

        // The global refresh will call updateSelf() which handles reloading data.
        // We still call this to immediately update the button state.
        updateUiForCurrentState();
    }

    private void loadData() {
        if (currentUser == null) return;
        txtId.setText(String.valueOf(currentUser.getId()));
        txtUsername.setText(currentUser.getUsername());
        String roleName = "-";
        if (currentUser.getRoleManager() != null && currentUser.getRoleManager().getActiveRole() != null) {
            roleName = currentUser.getRoleManager().getActiveRole().getName();
        }
        txtRole.setText(roleName);

        String skillsInfo = "-";
        if (currentUser.getSkillManager() != null) {
            int activeSkillsCount = currentUser.getSkillManager().getActiveSkills().size();
            skillsInfo = activeSkillsCount + (activeSkillsCount == 1 ? " aktiver Skill" : " aktive Skills");
        }
        txtSkills.setText(skillsInfo);

        txtTeam.setText("Team-ID: " + currentUser.getTeamId());
        txtFirstName.setText(currentUser.getFirstName());
        txtLastName.setText(currentUser.getLastName());
        txtEmail.setText(currentUser.getEMail());
        txtPhone.setText(currentUser.getPhoneNumber());
        txtAddress.setText(currentUser.getAddress());

        // For security, don't display the actual password.
        txtPassword.setText("");
    }

    private void showRoleHistoryDialog() {
        JDialog historyDialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), "Meine Rollenhistorie", Dialog.ModalityType.APPLICATION_MODAL);
        // This view is read-only, so no data-changed callback is needed.
        RoleHistoryPanel panel = new RoleHistoryPanel(this.currentUser, false, null);
        historyDialog.setContentPane(panel);
        historyDialog.setSize(600, 400);
        historyDialog.setLocationRelativeTo(this);
        historyDialog.setVisible(true);
    }

    private void showSkillHistoryDialog(boolean isEditable) {
        if (this.currentUser == null) {
            JOptionPane.showMessageDialog(this, "Kein Benutzer ausgewählt.", "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JDialog historyDialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), "Meine Skill-Historie", Dialog.ModalityType.APPLICATION_MODAL);

        // Pass a callback to the panel. If it modifies data (adds/removes a skill),
        // it will execute this callback to trigger a global UI refresh.
        Runnable onDataChangedCallback = () -> UIController.getInstance().updateMainWindow();
        SkillHistoryPanel panel = new SkillHistoryPanel(this.currentUser, isEditable, onDataChangedCallback);

        historyDialog.setContentPane(panel);
        historyDialog.setSize(700, 450);
        historyDialog.setLocationRelativeTo(this);
        historyDialog.setVisible(true);
        // The previous local `loadData()` call is no longer needed, as the global refresh handles it.
    }

    private JTextField createReadOnlyField() {
        JTextField tf = new JTextField(20);
        tf.setEditable(false);
        tf.setBackground(new Color(240, 240, 240));
        return tf;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    @Override public String getViewId() { return "my-profile-view"; }
    @Override public String getViewTabTitle() { return "Mein Profil"; }
    @Override public JPanel getContent() { return this; }
    @Override public boolean equals(View view) { return view != null && view.getViewId().equals(getViewId()); }

    /**
     * Refreshes the view's data from the core services.
     * This will only update the UI if the user is not in edit mode,
     * to prevent overwriting any unsaved changes.
     */
    @Override
    public void updateSelf() {
        this.currentUser = ServiceLocator.getSessionManager().getCurrentUser();
        if (this.currentUser == null) {
            return;
        }

        // CRITICAL: Only refresh if not in edit mode to prevent overwriting user input.
        if (!isInEditMode) {
            loadData();
        }
    }
}