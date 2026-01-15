package gui.views;

import core.SessionManager;
import core.ServiceLocator;
import gui.UIController;

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
        updateStatusDisplay(); // Load initial status
    }

    private void initUI() {
        // --- UPPER AREA: Status & Control ---
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

        // --- LOWER AREA: Log ---
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

        // 1. Change status in SessionManager (also saves to system.properties)
        sessionManager.setMaintenanceModeActive(newState);

        // 2. Update GUI
        updateStatusDisplay();

        // 3. Log & Warn
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
            lblSystemStatus.setBackground(new Color(255, 100, 100)); // Red
            lblSystemStatus.setForeground(Color.WHITE);
            btnToggleMaintenance.setText("Wartungsmodus deaktivieren (Online gehen)");
        } else {
            lblSystemStatus.setText("SYSTEMSTATUS: ONLINE");
            lblSystemStatus.setBackground(new Color(100, 200, 100)); // Green
            lblSystemStatus.setForeground(Color.BLACK);
            btnToggleMaintenance.setText("Wartungsmodus aktivieren (Sperren)");
        }

        // This call might be needed to update other parts of the application,
        // e.g., a global status bar in the main window.
        UIController.getInstance().updateMainWindow();
    }

    private void log(String message) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.append("[" + time + "] " + message + "\n");
        // Auto-scroll to the bottom
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

    /**
     * Refreshes the view's components by re-fetching data from the core services.
     * This is typically called when the view becomes visible (e.g., tab is selected)
     * to ensure it displays the most current system state.
     */
    @Override
    public void updateSelf() {
        log("Ansicht wird aktualisiert...");
        updateStatusDisplay();
    }
}