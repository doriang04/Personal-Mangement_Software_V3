package gui.views;

import gui.MainWindow;
import model.ServiceLocator;
import core.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Arrays;

/**
 * Die LoginView dient der Benutzerauthentifizierung. Sie wird initial
 * im MainWindow angezeigt und meldet einen erfolgreichen Login an dieses zurück.
 *
 * @author Ihr Name
 * @version 1.0
 */
public class LoginView extends JPanel implements View {

    // UI Komponenten
    private JTextField txtUsername;
    private JPasswordField pwdPassword;
    private JButton btnLogin;
    private JLabel lblError;
    private JLabel lblMaintenanceMessage;

    // Abhängigkeiten
    private final MainWindow mainWindowCallback;
    private final SessionManager sessionManager;

    /**
     * Konstruktor für die LoginView.
     *
     * @param callback Eine Referenz auf das MainWindow, um den erfolgreichen Login zu signalisieren.
     */
    public LoginView(MainWindow callback) {
        this.mainWindowCallback = callback;
        this.sessionManager = ServiceLocator.getSessionManager();
        initUI();
    }

    /**
     * Initialisiert die Benutzeroberfläche und die Komponenten.
     */
    private void initUI() {
        // Hauptpanel nutzt GridBagLayout, um das Formular-Panel zu zentrieren
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Das eigentliche Formular in einem separaten Panel für einen schöneren Rahmen
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder("Anmeldung"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Abstand zwischen den Komponenten
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Komponenten erstellen ---

        // Wartungsmeldung
        lblMaintenanceMessage = new JLabel("Systemwartung aktiv. Nur Admins können sich anmelden.");
        lblMaintenanceMessage.setForeground(Color.ORANGE.darker());
        lblMaintenanceMessage.setFont(lblMaintenanceMessage.getFont().deriveFont(Font.BOLD));
        lblMaintenanceMessage.setVisible(sessionManager.isMaintenanceModeActive());

        // Fehlermeldung
        lblError = new JLabel(" "); // Platzhalter, um Layout-Sprünge zu vermeiden
        lblError.setForeground(Color.RED);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);

        // Eingabefelder und Labels
        JLabel lblUsername = new JLabel("Benutzername:");
        txtUsername = new JTextField(20);

        JLabel lblPassword = new JLabel("Passwort:");
        pwdPassword = new JPasswordField(20);

        // Login Button
        btnLogin = new JButton("Anmelden");


        // --- Komponenten zum formPanel hinzufügen ---

        // Zeile 0: Wartungsmeldung
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(lblMaintenanceMessage, gbc);

        // Zeile 1: Fehlermeldung
        gbc.gridy = 1;
        formPanel.add(lblError, gbc);

        // Zeile 2: Benutzername
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblUsername, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(txtUsername, gbc);

        // Zeile 3: Passwort
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(lblPassword, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(pwdPassword, gbc);

        // Zeile 4: Login Button
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(btnLogin, gbc);

        // --- Aktionen hinzufügen ---

        // Login-Aktion für Button-Klick und Enter im Passwortfeld
        btnLogin.addActionListener(e -> handleLogin());
        pwdPassword.addActionListener(e -> handleLogin());

        // Das zentrierte formPanel zum Hauptpanel hinzufügen
        add(formPanel);
    }

    /**
     * Verarbeitet den Login-Versuch. Validiert Eingaben und ruft den SessionManager auf.
     */
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        char[] password = pwdPassword.getPassword();

        // 1. Eingaben validieren
        if (username.isEmpty() || password.length == 0) {
            lblError.setText("Bitte Benutzername und Passwort eingeben.");
            Arrays.fill(password, ' '); // Passwort sicher löschen
            return;
        }

        // 2. Vorherige Fehlermeldung zurücksetzen
        lblError.setText(" ");

        // 3. Login-Versuch über den SessionManager
        try {
            sessionManager.login(username, new String(password));

            // Bei Erfolg: Callback an MainWindow auslösen
            mainWindowCallback.onLoginSuccess();

        } catch (Exception e) {
            // Bei Misserfolg: Fehlermeldung anzeigen
            // Die Exception vom SessionManager sollte eine benutzerfreundliche Nachricht enthalten.
            lblError.setText(e.getMessage());
        } finally {
            // Wichtig: Passwort-Array aus Sicherheitsgründen immer löschen
            Arrays.fill(password, ' ');
            pwdPassword.setText(""); // Feld visuell leeren
        }
    }

    @Override
    public String getViewId() {
        return "login-view";
    }

    @Override
    public String getViewTabTitle() {
        return "Login"; // Wird nicht als Tab angezeigt, aber für die Schnittstelle benötigt
    }

    @Override
    public JComponent getComponent() {
        return this;
    }
}