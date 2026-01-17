package gui.views;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import core.ServiceLocator;
import core.SessionManager;
import gui.UIController;
import static gui.UITheme.COLOR_ACCENT;
import static gui.UITheme.COLOR_BG_CONTENT;
import static gui.UITheme.COLOR_TEXT_HEADER;

public class LoginView extends JPanel implements View {

    private JTextField txtUsername;
    private JPasswordField pwdPassword;
    private JButton btnLogin;
    private JLabel lblError;
    private final SessionManager sessionManager;
    private JLabel lblMaintenanceMessage;

    public LoginView() {
        this.sessionManager = ServiceLocator.getSessionManager();
        initUI();
    }

    private void initUI() {
        setBackground(COLOR_BG_CONTENT);
        setLayout(new GridBagLayout()); //zentrieren 

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 40, 5, 40);
        gbc.gridx = 0;

        // Logo / Icon
        javax.swing.Icon icon;
        java.net.URL imgURL = getClass().getResource("src/main/resources/icons/Logo.png");
        if (imgURL != null) {
            ImageIcon raw = new ImageIcon(imgURL);
            java.awt.Image scaled = raw.getImage().getScaledInstance(70, 70, java.awt.Image.SCALE_SMOOTH);
            icon = new ImageIcon(scaled);
        } else {
            icon = new javax.swing.plaf.metal.MetalIconFactory.TreeLeafIcon();
        }
        // Logo Label
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setPreferredSize(new Dimension(100, 100));

        // System Titel
        JLabel lblHR = new JLabel("Personal Management System");
        lblHR.setForeground(COLOR_TEXT_HEADER);
        lblHR.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Überschrift 
        JLabel lblWelcome = new JLabel("Willkommen zurück!");
        lblWelcome.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);
        lblWelcome.setForeground(COLOR_ACCENT);

        // Einleitungstext
        JLabel lblStartText = new JLabel("Bitte melden Sie sich an, um fortzufahren.");
        lblStartText.setForeground(COLOR_TEXT_HEADER);
        lblStartText.setHorizontalAlignment(SwingConstants.CENTER);

        //Eingabefelder 
        JLabel lblUserTag = new JLabel("Nutzername");
        lblUserTag.setFont(new Font("SansSerif", Font.BOLD, 12));
        txtUsername = createStyledTextField("beispielname");

        JLabel lblPassTag = new JLabel("Passwort");
        lblPassTag.setFont(new Font("SansSerif", Font.BOLD, 12));
        pwdPassword = createStyledPasswordField();

        // Button zum Anmelden
        btnLogin = new JButton("Anmelden");
        styleButton(btnLogin);

        // Fehlermeldung
        lblError = new JLabel(" ", SwingConstants.CENTER);
        lblError.setForeground(Color.RED);

        lblMaintenanceMessage = new JLabel("Systemwartung aktiv. Nur Admins können sich anmelden.", SwingConstants.CENTER);
        lblMaintenanceMessage.setForeground(Color.ORANGE.darker());
        lblMaintenanceMessage.setFont(lblMaintenanceMessage.getFont().deriveFont(Font.BOLD));
        lblMaintenanceMessage.setVisible(sessionManager.isMaintenanceModeActive());

        // alles zum grid hinzufügen
        gbc.gridy = 0; add(lblIcon, gbc);
        gbc.insets = new Insets(0, 40, 20, 40);
        gbc.gridy = 1; add(lblHR, gbc);
        gbc.insets = new Insets(10, 40, 30, 40);
        gbc.gridy = 2; add(lblWelcome, gbc);
        gbc.insets = new Insets(0, 40, 30, 40);
        gbc.gridy = 3; add(lblStartText, gbc);
        
        gbc.insets = new Insets(10, 40, 5, 40);
        gbc.gridy = 4; add(lblUserTag, gbc);
        gbc.gridy = 5; add(txtUsername, gbc);
        
        gbc.gridy = 6; add(lblPassTag, gbc);
        gbc.gridy = 7; add(pwdPassword, gbc);

        gbc.insets = new Insets(20, 40, 10, 40);
        gbc.gridy = 8; add(btnLogin, gbc);
        gbc.gridy = 9; add(lblError, gbc);
        gbc.gridy = 10; add(lblMaintenanceMessage, gbc);
        
        btnLogin.addActionListener(_ -> handleLogin());
        pwdPassword.addActionListener(_ -> handleLogin());    
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField(20);
        field.setPreferredSize(new Dimension(300, 45));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
            new EmptyBorder(0, 10, 0, 10)
        ));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setPreferredSize(new Dimension(300, 45));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
            new EmptyBorder(0, 10, 0, 10)
        ));
        return field;
    }

    //wie sieht ein Button aus
    private void styleButton(JButton btn) {
        btn.setBackground(COLOR_ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(300, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        
        // Hover Effekt
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(COLOR_ACCENT.darker()); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(COLOR_ACCENT); }
        });
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

        @Override
        public void updateSelf() {
            lblMaintenanceMessage.setVisible(sessionManager.isMaintenanceModeActive());

        }

}