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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
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
import static gui.UITheme.createStyledButton;
import gui.components.RoleHistoryPanel;
import gui.components.SkillHistoryPanel;
import model.Employee;

public class MyProfileView extends JPanel implements View {

    private Employee currentUser;
    private boolean isInEditMode = false;
    private boolean hasUnsavedChanges = false;

    private JTextField txtFirstName, txtLastName, txtEmail, txtPhone, txtAddress;
    private JPasswordField txtPassword;
    private JTextField txtId, txtUsername, txtRole, txtTeam, txtSkills;

    private JButton btnPrimaryAction;
    private JButton btnDiscard;
    private JLabel lblUnsavedChanges;

    public MyProfileView() {
        this.currentUser = ServiceLocator.getSessionManager().getCurrentUser();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG_CONTENT);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(true);
        header.setBackground(COLOR_HEADER_BG);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));
        JLabel title = new JLabel("Mein Profil");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(COLOR_TEXT_HEADER);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        initFields();

        int row = 0;
        addModernRow(cardPanel, gbc, row++, "Mitarbeiter-ID:", txtId);
        addModernRow(cardPanel, gbc, row++, "Benutzername:", txtUsername);

        JPanel rolePanel = new JPanel(new BorderLayout(10, 0));
        rolePanel.setOpaque(false);
        rolePanel.add(txtRole, BorderLayout.CENTER);
        JButton btnRoleHistory = createStyledButton("Historie", false);
        btnRoleHistory.addActionListener(_ -> showRoleHistoryDialog());
        rolePanel.add(btnRoleHistory, BorderLayout.EAST);
        addModernRow(cardPanel, gbc, row++, "Rolle:", rolePanel);

        JPanel skillPanel = new JPanel(new BorderLayout(10, 0));
        skillPanel.setOpaque(false);
        skillPanel.add(txtSkills, BorderLayout.CENTER);
        JButton btnSkillHistory = createStyledButton("Historie", false);
        btnSkillHistory.addActionListener(_ -> showSkillHistoryDialog(false));
        skillPanel.add(btnSkillHistory, BorderLayout.EAST);
        addModernRow(cardPanel, gbc, row++, "Skills:", skillPanel);

        addModernRow(cardPanel, gbc, row++, "Team / Abteilung:", txtTeam);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 20, 10);
        cardPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(8, 10, 8, 10);

        addModernRow(cardPanel, gbc, row++, "Vorname:", txtFirstName);
        addModernRow(cardPanel, gbc, row++, "Nachname:", txtLastName);
        addModernRow(cardPanel, gbc, row++, "E-Mail:", txtEmail);
        addModernRow(cardPanel, gbc, row++, "Telefon:", txtPhone);
        addModernRow(cardPanel, gbc, row++, "Adresse:", txtAddress);
        addModernRow(cardPanel, gbc, row++, "Passwort:", txtPassword);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(cardPanel, new GridBagConstraints());

        JScrollPane scrollPane = new JScrollPane(centerWrapper);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        footer.setOpaque(true);
        footer.setBackground(COLOR_BG_CONTENT);
        footer.setBorder(new EmptyBorder(0, 0, 10, 30));

        lblUnsavedChanges = new JLabel("* Ungespeicherte Änderungen");
        lblUnsavedChanges.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblUnsavedChanges.setForeground(COLOR_ACCENT);

        btnDiscard = createStyledButton("Änderungen verwerfen", false);
        btnDiscard.setForeground(new Color(220, 38, 38));
        btnDiscard.addActionListener(_ -> discardChanges());

        btnPrimaryAction = createStyledButton("Profil bearbeiten", true);
        btnPrimaryAction.addActionListener(_ -> handlePrimaryAction());

        footer.add(lblUnsavedChanges);
        footer.add(btnDiscard);
        footer.add(btnPrimaryAction);
        add(footer, BorderLayout.SOUTH);

        loadData();
        addChangeListeners();
        updateUiForCurrentState();
    }

    private void initFields() {
        txtId = createModernTextField(false);
        txtUsername = createModernTextField(false);
        txtRole = createModernTextField(false);
        txtSkills = createModernTextField(false);
        txtTeam = createModernTextField(false);
        txtFirstName = createModernTextField(true);
        txtLastName = createModernTextField(true);
        txtEmail = createModernTextField(true);
        txtPhone = createModernTextField(true);
        txtAddress = createModernTextField(true);

        txtPassword = new JPasswordField(20);
        txtPassword.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1), new EmptyBorder(8, 10, 8, 10)));
    }

    private JTextField createModernTextField(boolean editable) {
        JTextField tf = new JTextField(20);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tf.setEditable(editable);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1), new EmptyBorder(8, 10, 8, 10)));
        if (!editable) tf.setBackground(new Color(245, 245, 245));
        return tf;
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
        JTextField[] fields = {txtFirstName, txtLastName, txtEmail, txtPhone, txtAddress, txtPassword};
        for (JTextField f : fields) {
            f.setEditable(enable);
            f.setBackground(enable ? Color.WHITE : new Color(248, 248, 248));
        }
    }

    private void handlePrimaryAction() {
        if (isInEditMode) {
            if (hasUnsavedChanges) saveChanges();
            else {
                isInEditMode = false;
                loadData();
                updateUiForCurrentState();
            }
        } else {
            isInEditMode = true;
            hasUnsavedChanges = false;
            txtPassword.setText("");
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
        String newPwd = new String(txtPassword.getPassword());
        if (!newPwd.isEmpty() && !newPwd.equals("********")) currentUser.setPassword(newPwd);
        JOptionPane.showMessageDialog(this, "Profil erfolgreich aktualisiert!");
        isInEditMode = false;
        hasUnsavedChanges = false;
        UIController.getInstance().updateMainWindow();
        loadData();
        updateUiForCurrentState();
    }

    private void discardChanges() {
        if (JOptionPane.showConfirmDialog(this, "Änderungen verwerfen?", "Abbrechen", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            isInEditMode = false;
            hasUnsavedChanges = false;
            loadData();
            updateUiForCurrentState();
        }
    }

    private void markAsChanged() {
        if (isInEditMode && !hasUnsavedChanges) {
            hasUnsavedChanges = true;
            updateUiForCurrentState();
        }
    }

    private void addChangeListeners() {
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
        txtPassword.getDocument().addDocumentListener(dl);
    }

    private void loadData() {
        if (currentUser == null) return;
        txtId.setText(String.valueOf(currentUser.getId()));
        txtUsername.setText(currentUser.getUsername());
        String roleName = (currentUser.getRoleManager() != null && currentUser.getRoleManager().getActiveRole() != null)
                ? currentUser.getRoleManager().getActiveRole().getName() : "-";
        txtRole.setText(roleName);
        int activeSkillsCount = (currentUser.getSkillManager() != null) ? currentUser.getSkillManager().getActiveSkills().size() : 0;
        txtSkills.setText(activeSkillsCount + (activeSkillsCount == 1 ? " aktiver Skill" : " aktive Skills"));
        txtTeam.setText("Team-ID: " + currentUser.getTeamId());
        txtFirstName.setText(currentUser.getFirstName());
        txtLastName.setText(currentUser.getLastName());
        txtEmail.setText(currentUser.getEMail());
        txtPhone.setText(currentUser.getPhoneNumber());
        txtAddress.setText(currentUser.getAddress());
        txtPassword.setText("********");
    }

    private void showRoleHistoryDialog() {
        JDialog diag = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Meine Rollenhistorie", Dialog.ModalityType.APPLICATION_MODAL);
        diag.setContentPane(new RoleHistoryPanel(this.currentUser, false, null));
        diag.setSize(600, 400);
        diag.setLocationRelativeTo(this);
        diag.setVisible(true);
    }

    private void showSkillHistoryDialog(boolean isEditable) {
        if (this.currentUser == null) return;
        JDialog diag = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Meine Skill-Historie", Dialog.ModalityType.APPLICATION_MODAL);
        diag.setContentPane(new SkillHistoryPanel(this.currentUser, isEditable, () -> UIController.getInstance().updateMainWindow()));
        diag.setSize(700, 450);
        diag.setLocationRelativeTo(this);
        diag.setVisible(true);
    }

    @Override public String getViewId() { return "my-profile-view"; }
    @Override public String getViewTabTitle() { return "Mein Profil"; }
    @Override public JPanel getContent() { return this; }
    @Override public boolean equals(View view) { return view != null && view.getViewId().equals(getViewId()); }

    @Override
    public void updateSelf() {
        this.currentUser = ServiceLocator.getSessionManager().getCurrentUser();
        if (this.currentUser != null && !isInEditMode) loadData();
    }
}