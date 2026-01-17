package gui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import core.ServiceLocator;
import gui.UIController;
import static gui.UITheme.COLOR_ACCENT;
import static gui.UITheme.COLOR_BG_CONTENT;
import static gui.UITheme.COLOR_BORDER;
import static gui.UITheme.COLOR_HEADER_BG;
import static gui.UITheme.COLOR_TEXT_HEADER;
import static gui.UITheme.createModernTextField;
import static gui.UITheme.createStyledButton;
import gui.components.RoleHistoryPanel;
import gui.components.SkillHistoryPanel;
import model.Employee;
import model.Role;
import model.Team;

public class EmployeeDetailView extends JPanel implements View {

    private final String viewId;
    private String tabTitle;
    private final Employee employee;
    private final boolean canEdit;

    private boolean isInEditMode = false;
    private boolean hasUnsavedChanges = false;

    private JTextField txtFirstName, txtLastName, txtEmail, txtPhone, txtAddress, txtUsername;
    private JComboBox<TeamItem> cbTeam;
    private JComboBox<RoleItem> cbRole;
    private JButton btnShowRoleHistory, btnShowSkillHistory;
    private JButton btnPrimaryAction, btnDiscard;
    private JLabel lblUnsavedChanges;

    public EmployeeDetailView(int employeeId) {
        // Daten laden vom ServiceLocator
        this.employee = ServiceLocator.getEmployeeContainer().getEmployeeById(employeeId);
        this.canEdit = checkPermissions();

        if (this.employee != null) {
            this.tabTitle = "Profil: " + employee.getFirstName();
            this.viewId = "employee-detail-" + employee.getId();
        } else {
            this.tabTitle = "Fehler";
            this.viewId = "employee-detail-error-" + employeeId;
        }
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG_CONTENT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(true);
        header.setBorder(new EmptyBorder(20, 30, 15, 30));
        header.setBackground(COLOR_HEADER_BG);

        String headerText = (employee != null)
                ? "Profil von: " + employee.getFirstName() + " " + employee.getLastName()
                : "Mitarbeiter nicht gefunden";

        JLabel titleLabel = new JLabel(headerText);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_TEXT_HEADER);
        header.add(titleLabel, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        if (employee == null) {
            add(new JLabel("Mitarbeiter konnte nicht geladen werden.", SwingConstants.CENTER), BorderLayout.CENTER);
            return;
        }

        // Mittelteil
        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        initFormFields();
        loadComboBoxData();
        fillFields();
        addChangeListeners();

        int row = 0;
        addModernRow(cardPanel, gbc, row++, "Benutzername:", txtUsername);
        addModernRow(cardPanel, gbc, row++, "Vorname:", txtFirstName);
        addModernRow(cardPanel, gbc, row++, "Nachname:", txtLastName);
        addModernRow(cardPanel, gbc, row++, "E-Mail:", txtEmail);
        addModernRow(cardPanel, gbc, row++, "Telefon:", txtPhone);
        addModernRow(cardPanel, gbc, row++, "Adresse:", txtAddress);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 20, 10);
        cardPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(8, 10, 8, 10);

        addModernRow(cardPanel, gbc, row++, "Abteilung / Team:", cbTeam);
        addModernRow(cardPanel, gbc, row++, "Aktuelle Rolle:", cbRole);

        // History Button Panel
        JPanel historyButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        historyButtonPanel.setOpaque(false);

        btnShowRoleHistory = createStyledButton("Rollenhistorie...", false);
        btnShowRoleHistory.addActionListener(_ -> showRoleHistoryDialog());
        
        btnShowSkillHistory = createStyledButton("Skill-Historie...", false);
        btnShowSkillHistory.addActionListener(_ -> showSkillHistoryDialog());

        historyButtonPanel.add(btnShowRoleHistory);
        historyButtonPanel.add(btnShowSkillHistory);

        gbc.gridx = 1; gbc.gridy = row++;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        cardPanel.add(historyButtonPanel, gbc);

        // Zentrieren von Mittelteil mit Scrollbar
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(cardPanel, new GridBagConstraints());

        JScrollPane scrollPane = new JScrollPane(centerWrapper);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        // Fußzeile mit Zeugs
        if (canEdit) {
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
            footer.setOpaque(false);
            footer.setBorder(new EmptyBorder(0, 0, 10, 30));

            lblUnsavedChanges = new JLabel("* Ungespeicherte Änderungen");
            lblUnsavedChanges.setFont(new Font("SansSerif", Font.ITALIC, 12));
            lblUnsavedChanges.setForeground(COLOR_ACCENT);

            btnDiscard = createStyledButton("Änderungen verwerfen", false);
            btnDiscard.setForeground(new Color(220, 38, 38));
            btnDiscard.addActionListener(_ -> discardChanges());

            btnPrimaryAction = createStyledButton("", true);
            btnPrimaryAction.addActionListener(_ -> handlePrimaryAction());

            footer.add(lblUnsavedChanges);
            footer.add(btnDiscard);
            footer.add(btnPrimaryAction);
            add(footer, BorderLayout.SOUTH);
        }

        updateUiForCurrentState();
    }

    private void initFormFields() {
        // Textfelder
        txtUsername = createModernTextField();
        txtFirstName = createModernTextField();
        txtLastName = createModernTextField();
        txtEmail = createModernTextField();
        txtPhone = createModernTextField();
        txtAddress = createModernTextField();
        cbTeam = new JComboBox<>();
        cbRole = new JComboBox<>();
        cbTeam.setBackground(Color.WHITE);
        cbRole.setBackground(Color.WHITE);
    }

    private void addModernRow(JPanel p, GridBagConstraints gbc, int row, String labelText, JComponent comp) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(COLOR_TEXT_HEADER);
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        p.add(label, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        p.add(comp, gbc);
    }

    private void updateUiForCurrentState() {
        // Felder aktivieren/deaktivieren
        enableFields(isInEditMode);
        txtUsername.setEditable(false);
        txtUsername.setBackground(new Color(245, 245, 245));

        if (btnPrimaryAction == null) return;

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

    private void handlePrimaryAction() {
        // Je nach primary oder nicht: Speichern, Verwerfen oder in den Bearbeitungsmodus wechseln
        if (isInEditMode) {
            if (hasUnsavedChanges) saveChanges();
            else { isInEditMode = false; updateUiForCurrentState(); }
        } else {
            isInEditMode = true;
            hasUnsavedChanges = false;
            updateUiForCurrentState();
        }
    }

    private void saveChanges() {
        // Änderungen speichern
        try {
            employee.setFirstName(txtFirstName.getText().trim());
            employee.setLastName(txtLastName.getText().trim());
            employee.setEMail(txtEmail.getText().trim());
            employee.setPhoneNumber(txtPhone.getText().trim());
            employee.setAddress(txtAddress.getText().trim());

            TeamItem selectedTeam = (TeamItem) cbTeam.getSelectedItem();
            employee.setTeamId((selectedTeam != null && selectedTeam.team != null) ? selectedTeam.team.getId() : 0);

            RoleItem selectedRoleItem = (RoleItem) cbRole.getSelectedItem();
            if (selectedRoleItem != null) {
                Role newRole = selectedRoleItem.role;
                Role currentRole = employee.getRoleManager().getActiveRole();
                if (currentRole == null || currentRole.getId() != newRole.getId()) {
                    employee.getRoleManager().assignRole(newRole.getId(), LocalDate.now());
                }
            }

            UIController.getInstance().updateMainWindow();
            JOptionPane.showMessageDialog(this, "Erfolgreich gespeichert!");
            isInEditMode = false;
            hasUnsavedChanges = false;
            updateUiForCurrentState();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler beim Speichern: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void discardChanges() {
        // Änderungen verwerfen
        if (JOptionPane.showConfirmDialog(this, "Änderungen wirklich verwerfen?", "Abbrechen", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            isInEditMode = false;
            hasUnsavedChanges = false;
            fillFields();
            updateUiForCurrentState();
        }
    }

    private void addChangeListeners() {
        // Änderungen überwachen
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { markAsChanged(); }
            public void removeUpdate(DocumentEvent e) { markAsChanged(); }
            public void changedUpdate(DocumentEvent e) { markAsChanged(); }
        };
        txtFirstName.getDocument().addDocumentListener(dl);
        txtLastName.getDocument().addDocumentListener(dl);
        txtEmail.getDocument().addDocumentListener(dl);
        txtPhone.getDocument().addDocumentListener(dl);
        txtAddress.getDocument().addDocumentListener(dl);
        cbTeam.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) markAsChanged(); });
        cbRole.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) markAsChanged(); });
    }

    private void markAsChanged() {
        // Markiert das Formular als geändert
        if (isInEditMode && !hasUnsavedChanges) {
            hasUnsavedChanges = true;
            updateUiForCurrentState();
        }
    }

    private void fillFields() {
        // Felder mit Daten füllen
        if (employee == null) return;
        txtUsername.setText(employee.getUsername());
        txtFirstName.setText(employee.getFirstName());
        txtLastName.setText(employee.getLastName());
        txtEmail.setText(employee.getEMail());
        txtPhone.setText(employee.getPhoneNumber());
        txtAddress.setText(employee.getAddress());

        // Team selection
        for (int i = 0; i < cbTeam.getItemCount(); i++) {
            if (cbTeam.getItemAt(i).team != null && cbTeam.getItemAt(i).team.getId() == employee.getTeamId()) {
                cbTeam.setSelectedIndex(i); break;
            }
        }
        // Role selection
        Role active = employee.getRoleManager().getActiveRole();
        if (active != null) {
            for (int i = 0; i < cbRole.getItemCount(); i++) {
                if (cbRole.getItemAt(i).role.getId() == active.getId()) {
                    cbRole.setSelectedIndex(i); break;
                }
            }
        }
    }

    private void loadComboBoxData() {
        cbTeam.removeAllItems(); cbRole.removeAllItems();
        cbTeam.addItem(new TeamItem(null));
        ServiceLocator.getTeamContainer().getTeams().forEach(t -> cbTeam.addItem(new TeamItem(t)));
        ServiceLocator.getRoleContainer().getRoles().forEach(r -> cbRole.addItem(new RoleItem(r)));
    }

    private void enableFields(boolean enable) {
        JTextField[] fields = {txtFirstName, txtLastName, txtEmail, txtPhone, txtAddress};
        for (JTextField f : fields) {
            f.setEditable(enable);
            f.setBackground(enable ? Color.WHITE : new Color(248, 248, 248));
        }
        cbTeam.setEnabled(enable);
        cbRole.setEnabled(enable);
    }

    private void showRoleHistoryDialog() {
        JDialog diag = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Rollenhistorie", Dialog.ModalityType.APPLICATION_MODAL);
        diag.setContentPane(new RoleHistoryPanel(this.employee, this.canEdit && this.isInEditMode, () -> UIController.getInstance().updateMainWindow()));
        diag.setSize(600, 400);
        diag.setLocationRelativeTo(this);
        diag.setVisible(true);
    }

    private void showSkillHistoryDialog() {
        JDialog diag = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Skill-Historie", Dialog.ModalityType.APPLICATION_MODAL);
        diag.setContentPane(new SkillHistoryPanel(this.employee, this.canEdit && this.isInEditMode, () -> UIController.getInstance().updateMainWindow()));
        diag.setSize(700, 450);
        diag.setLocationRelativeTo(this);
        diag.setVisible(true);
    }

    private boolean checkPermissions() {
        String p = ServiceLocator.getSessionManager().getUserPermission();
        return p != null && (p.toUpperCase().contains("HR") || p.toUpperCase().contains("ADMIN"));
    }

    static class TeamItem { Team team; public TeamItem(Team t) { this.team = t; } @Override public String toString() { return (team == null) ? "- Kein Team -" : team.getName(); } }
    static class RoleItem { Role role; public RoleItem(Role r) { this.role = r; } @Override public String toString() { return role.getName(); } }

    @Override public String getViewId() { return viewId; }
    @Override public String getViewTabTitle() { return tabTitle; }
    @Override public JPanel getContent() { return this; }
    @Override public boolean equals(View v) { return v != null && v.getViewId().equals(this.getViewId()); }

    @Override
    public void updateSelf() {
        if (employee == null) return;
        this.tabTitle = "Profil: " + employee.getFirstName();
        if (!isInEditMode) {
            loadComboBoxData();
            fillFields();
        }
    }
}