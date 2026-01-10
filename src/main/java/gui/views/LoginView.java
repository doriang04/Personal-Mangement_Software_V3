package gui.views;

import gui.UIController;
import model.ServiceLocator;
import core.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Arrays;

public class LoginView extends JPanel implements View {

    private JTextField txtUsername;
    private JPasswordField pwdPassword;
    private JButton btnLogin;
    private JLabel lblError;
    private JLabel lblMaintenanceMessage;

    private final SessionManager sessionManager;

    public LoginView() {
        this.sessionManager = ServiceLocator.getSessionManager();
        initUI();
    }

    private void initUI() {
        setLayout(new GridLayout(1, 1, 10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        formPanel.setBorder(new TitledBorder("Anmeldung"));

        lblMaintenanceMessage = new JLabel("Systemwartung aktiv. Nur Admins können sich anmelden.", SwingConstants.CENTER);
        lblMaintenanceMessage.setForeground(Color.ORANGE.darker());
        lblMaintenanceMessage.setFont(lblMaintenanceMessage.getFont().deriveFont(Font.BOLD));
        lblMaintenanceMessage.setVisible(sessionManager.isMaintenanceModeActive());

        lblError = new JLabel(" ", SwingConstants.CENTER);
        lblError.setForeground(Color.RED);

        JLabel lblUsername = new JLabel("Benutzername:");
        txtUsername = new JTextField();

        JLabel lblPassword = new JLabel("Passwort:");
        pwdPassword = new JPasswordField();

        btnLogin = new JButton("Anmelden");

        JPanel messagePanel = new JPanel(new GridLayout(0, 1, 10, 10));
        messagePanel.add(lblMaintenanceMessage);
        messagePanel.add(lblError);

        formPanel.add(messagePanel);

        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        inputPanel.add(lblUsername);
        inputPanel.add(txtUsername);

        inputPanel.add(lblPassword);
        inputPanel.add(pwdPassword);

        inputPanel.add(new JLabel());
        inputPanel.add(btnLogin);

        formPanel.add(inputPanel);

        btnLogin.addActionListener(_ -> handleLogin());
        pwdPassword.addActionListener(_ -> handleLogin());

        add(formPanel);
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        char[] password = pwdPassword.getPassword();

        if (username.isEmpty() || password.length == 0) {
            lblError.setText("Bitte Benutzername und Passwort eingeben.");
            Arrays.fill(password, ' ');
            return;
        }

        lblError.setText(" ");

        try {
            sessionManager.login(username, new String(password));
            UIController.getInstance().onLoginSuccess();
        } catch (Exception e) {
            lblError.setText(e.getMessage());
        } finally {
            Arrays.fill(password, ' ');
            pwdPassword.setText("");
        }
    }

    @Override
    public String getViewId() {
        return "login-view";
    }

    @Override
    public String getViewTabTitle() {
        return "Personalmanagement - Login";
    }

    @Override
    public JPanel getContent() {
        return this;
    }

    @Override
    public boolean equals(View view) {
        return view.getViewId().equals(this.getViewId());
    }
}