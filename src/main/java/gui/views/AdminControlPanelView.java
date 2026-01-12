package gui.views;

import core.SessionManager;
import core.ServiceLocator;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminControlPanelView extends JPanel implements View {

    private JLabel lblSystemStatus;
    private JButton btnToggleMaintenance;
    private JTextArea logArea;
    private final SessionManager sessionManager;

    public AdminControlPanelView() {
        this.sessionManager = ServiceLocator.getSessionManager();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initUI();
        updateStatusDisplay(); // Initialen Status laden
    }

    private void initUI() {
        // --- OBERER BEREICH: Status & Steuerung ---
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(new TitledBorder("Systemzustand"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Status Label
        lblSystemStatus = new JLabel("Lade Status...", SwingConstants.CENTER);
        lblSystemStatus.setFont(new Font("Arial", Font.BOLD, 18));
        lblSystemStatus.setOpaque(true);
        lblSystemStatus.setPreferredSize(new Dimension(300, 40));

        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(lblSystemStatus, gbc);

        // Toggle Button
        btnToggleMaintenance = new JButton("Wartungsmodus umschalten");
        btnToggleMaintenance.setPreferredSize(new Dimension(250, 40));
        btnToggleMaintenance.addActionListener(e -> toggleMaintenanceMode());

        gbc.gridy = 1;
        controlPanel.add(btnToggleMaintenance, gbc);

        add(controlPanel, BorderLayout.NORTH);

        JPanel logPanel = new JPanel(new BorderLayout(5, 5));
        logPanel.setBorder(new TitledBorder("System-Ereignisprotokoll"));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(240, 240, 240));

        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        add(logPanel, BorderLayout.CENTER);

        log("Admin-Panel geöffnet.");
    }

    private void toggleMaintenanceMode() {
        boolean currentState = sessionManager.isMaintenanceModeActive();
        boolean newState = !currentState;

        // 1. Status im SessionManager ändern (speichert auch in system.properties)
        sessionManager.setMaintenanceModeActive(newState);

        // 2. GUI aktualisieren
        updateStatusDisplay();

        // 3. Loggen & Warnen
        if (newState) {
            log("ACHTUNG: Wartungsmodus wurde AKTIVIERT.");
            log("Neue Anmeldungen für Nicht-Admins sind jetzt gesperrt.");
            JOptionPane.showMessageDialog(this,
                    "Wartungsmodus ist nun AKTIV.\nNicht-Admin Benutzer können sich nicht mehr einloggen.",
                    "Systemstatus geändert", JOptionPane.WARNING_MESSAGE);
        } else {
            log("Wartungsmodus wurde DEAKTIVIERT. System ist online.");
            JOptionPane.showMessageDialog(this,
                    "System ist wieder ONLINE.\nAnmeldungen sind wieder für alle möglich.",
                    "Systemstatus geändert", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateStatusDisplay() {
        boolean active = sessionManager.isMaintenanceModeActive();

        if (active) {
            lblSystemStatus.setText("SYSTEMSTATUS: WARTUNGSMODUS");
            lblSystemStatus.setBackground(new Color(255, 100, 100)); // Rot
            lblSystemStatus.setForeground(Color.WHITE);
            btnToggleMaintenance.setText("Wartungsmodus deaktivieren (Online gehen)");
        } else {
            lblSystemStatus.setText("SYSTEMSTATUS: ONLINE");
            lblSystemStatus.setBackground(new Color(100, 200, 100)); // Grün
            lblSystemStatus.setForeground(Color.BLACK);
            btnToggleMaintenance.setText("Wartungsmodus aktivieren (Sperren)");
        }
    }

    private void log(String message) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.append("[" + time + "] " + message + "\n");
        // Auto-Scroll nach unten
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    @Override
    public String getViewId() { return "admin-control-panel"; }

    @Override
    public String getViewTabTitle() { return "Systemsteuerung"; }

    @Override
    public JPanel getContent() { return this; }

    @Override
    public boolean equals(View view) {
        return view != null && view.getViewId().equals(getViewId());
    }
}