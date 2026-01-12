package gui.views;

import gui.components.RoleHistoryPanel;
import model.Employee;
import core.ServiceLocator;

import javax.swing.*;
import java.awt.*;

public class MyProfileView extends JPanel implements View {

    private Employee currentUser;

    // Eingabefelder
    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtAddress;
    private JPasswordField txtPassword; // Passwort maskiert

    // Read-only Felder
    private JTextField txtId;
    private JTextField txtUsername;
    private JTextField txtRole;
    private JTextField txtTeam;

    public MyProfileView() {
        setLayout(new BorderLayout());

        // 1. User laden
        this.currentUser = ServiceLocator.getSessionManager().getCurrentUser();

        // 2. UI aufbauen
        initUI();

        // 3. Daten füllen
        loadData();
    }


    private void initUI() {
        // --- Header ---
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel title = new JLabel("Mein Profil bearbeiten");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        header.add(title);
        header.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(header, BorderLayout.NORTH);

        // --- Formular Bereich ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // -- Zeile 0: Titel --
        // (Optional, überspringen wir für sauberes Grid)

        // -- Felder initialisieren --
        txtId = createReadOnlyField();
        txtUsername = createReadOnlyField();
        txtRole = createReadOnlyField();
        txtTeam = createReadOnlyField();

        txtFirstName = new JTextField(20);
        txtLastName = new JTextField(20);
        txtEmail = new JTextField(20);
        txtPhone = new JTextField(20);
        txtAddress = new JTextField(20);
        txtPassword = new JPasswordField(20);

        // -- Layout aufbauen (Label links, Feld rechts) --
        int row = 0;

        // Read-Only Block
        addFormRow(formPanel, gbc, row++, "Mitarbeiter-ID:", txtId);
        addFormRow(formPanel, gbc, row++, "Benutzername:", txtUsername);

        // --- NEUER TEIL: Button neben der Rolle ---
        // Wir erstellen ein kleines Panel, um Textfeld und Button nebeneinander zu legen
        JPanel rolePanel = new JPanel(new BorderLayout(5,0));
        rolePanel.add(txtRole, BorderLayout.CENTER);
        JButton btnHistory = new JButton("Historie");
        btnHistory.setMargin(new Insets(2, 5, 2, 5)); // kleinerer Button
        btnHistory.addActionListener(_ -> showRoleHistoryDialog());
        rolePanel.add(btnHistory, BorderLayout.EAST);
        addFormRow(formPanel, gbc, row++, "Rolle:", rolePanel);

        addFormRow(formPanel, gbc, row++, "Team / Abteilung:", txtTeam);

        // Separator
        JSeparator sep = new JSeparator();
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formPanel.add(sep, gbc);
        gbc.gridwidth = 1;

        // Editierbarer Block
        addFormRow(formPanel, gbc, row++, "Vorname:", txtFirstName);
        addFormRow(formPanel, gbc, row++, "Nachname:", txtLastName);
        addFormRow(formPanel, gbc, row++, "E-Mail:", txtEmail);
        addFormRow(formPanel, gbc, row++, "Telefon:", txtPhone);
        addFormRow(formPanel, gbc, row++, "Adresse:", txtAddress);
        addFormRow(formPanel, gbc, row++, "Passwort:", txtPassword);

        // ScrollPane falls Fenster klein
        add(new JScrollPane(formPanel), BorderLayout.CENTER);

        // --- Footer mit Button ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Änderungen speichern");
        btnSave.setFont(new Font("Arial", Font.BOLD, 12));
        btnSave.setBackground(new Color(100, 200, 100)); // Leicht grünlich

        btnSave.addActionListener(e -> saveChanges());

        footer.add(btnSave);
        footer.setBorder(BorderFactory.createEmptyBorder(10,10,10,20));
        add(footer, BorderLayout.SOUTH);
    }

    private void showRoleHistoryDialog() {
        JDialog historyDialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Meine Rollenhistorie",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        // Erstelle das Panel. isEditable ist hier IMMER false!
        RoleHistoryPanel panel = new RoleHistoryPanel(this.currentUser, false);

        historyDialog.setContentPane(panel);
        historyDialog.setSize(600, 400);
        historyDialog.setLocationRelativeTo(this);
        historyDialog.setVisible(true);
        // Kein Neuladen nötig, da keine Änderungen möglich sind.
    }

    private JTextField createReadOnlyField() {
        JTextField tf = new JTextField(20);
        tf.setEditable(false);
        tf.setBackground(new Color(240, 240, 240)); // Grau hinterlegt
        return tf;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    private void loadData() {
        if (currentUser == null) return;

        // Read-Only
        txtId.setText(String.valueOf(currentUser.getId()));
        txtUsername.setText(currentUser.getUsername());

        // Rolle ermitteln
        String roleName = "-";
        try {
            if (currentUser.getRoleManager() != null && currentUser.getRoleManager().getActiveRole() != null) {
                roleName = currentUser.getRoleManager().getActiveRole().getName();
            }
        } catch (Exception e) { /* ignore */ }
        txtRole.setText(roleName);

        // Team ID (Name auflösen wäre schöner, aber ID reicht für Demo)
        txtTeam.setText("Team-ID: " + currentUser.getTeamId());

        // Editierbar
        txtFirstName.setText(currentUser.getFirstName());
        txtLastName.setText(currentUser.getLastName());
        txtEmail.setText(currentUser.getEMail());
        txtPhone.setText(currentUser.getPhoneNumber());
        txtAddress.setText(currentUser.getAddress());
        txtPassword.setText(currentUser.getPassword());
    }

    private void saveChanges() {
        if (currentUser == null) return;

        // Validierung (Basis)
        if (txtFirstName.getText().trim().isEmpty() || txtLastName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vor- und Nachname dürfen nicht leer sein.", "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Möchten Sie die Änderungen an Ihrem Profil speichern?",
                "Bestätigung", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // 1. Daten ins Objekt schreiben (RAM)
            currentUser.setFirstName(txtFirstName.getText().trim());
            currentUser.setLastName(txtLastName.getText().trim());
            currentUser.setEMail(txtEmail.getText().trim());
            currentUser.setPhoneNumber(txtPhone.getText().trim());
            currentUser.setAddress(txtAddress.getText().trim());
            currentUser.setPassword(new String(txtPassword.getPassword()));

            // 2. Hinweis: Datenbank Update
            // Da du sagtest "nur im Objekt ändern" (wie bei Zuweisung), belassen wir es dabei.
            // Falls DB gewünscht: DatabaseManager.getInstance().updateEmployee(currentUser);

            JOptionPane.showMessageDialog(this, "Profil erfolgreich aktualisiert!");

            // UI neu laden, falls Formatierungen nötig sind
            loadData();
        }
    }

    // --- View Interface ---
    @Override public String getViewId() { return "my-profile-view"; }
    @Override public String getViewTabTitle() { return "Mein Profil"; }
    @Override public JPanel getContent() { return this; }
    @Override public boolean equals(View view) { return view != null && view.getViewId().equals(getViewId()); }
}
