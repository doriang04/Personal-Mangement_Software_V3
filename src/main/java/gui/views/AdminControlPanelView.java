package gui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import core.ServiceLocator;
import core.SessionManager;
import gui.UIController;
import static gui.UITheme.COLOR_BG_CONTENT;
import static gui.UITheme.COLOR_BORDER;
import static gui.UITheme.COLOR_HEADER_BG;
import static gui.UITheme.COLOR_STATUS_GREEN;
import static gui.UITheme.COLOR_STATUS_RED;
import static gui.UITheme.COLOR_TEXT_HEADER;
import static gui.UITheme.createModernCard;
import static gui.UITheme.createStyledButton;

public class AdminControlPanelView extends JPanel implements View {

    private JLabel lblSystemStatus;
    private JButton btnToggleMaintenance;
    private JTextArea logArea;
    private final SessionManager sessionManager;

    public AdminControlPanelView() {
        this.sessionManager = ServiceLocator.getSessionManager();
        initUI();
        updateStatusDisplay();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG_CONTENT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(true);
        header.setBackground(COLOR_HEADER_BG);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel titleLabel = new JLabel("System-Administration");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_TEXT_HEADER);
        header.add(titleLabel, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel contentContainer = new JPanel(new GridBagLayout());
        contentContainer.setOpaque(false);
        contentContainer.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 25, 0);

        // Mittelteil --> Systemstatus / Kontrolle
        JPanel statusCard = createModernCard("Systemzustand & Kontrolle");
        JPanel statusContent = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        statusContent.setOpaque(false);

        lblSystemStatus = new JLabel("Lade...", SwingConstants.CENTER);
        lblSystemStatus.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblSystemStatus.setOpaque(true);
        lblSystemStatus.setForeground(Color.WHITE);
        lblSystemStatus.setPreferredSize(new Dimension(300, 50));

        btnToggleMaintenance = createStyledButton("Umschalten", true);
        btnToggleMaintenance.setPreferredSize(new Dimension(350, 50));
        btnToggleMaintenance.addActionListener(e -> toggleMaintenanceMode());

        statusContent.add(lblSystemStatus);
        statusContent.add(btnToggleMaintenance);
        statusCard.add(statusContent, BorderLayout.CENTER);
        
        contentContainer.add(statusCard, gbc);

        // Log-Bereich
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        
        JPanel logCard = createModernCard("System-Ereignisprotokoll");
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(248, 248, 248));
        logArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(new LineBorder(COLOR_BORDER, 1));
        logCard.add(logScroll, BorderLayout.CENTER);

        contentContainer.add(logCard, gbc);

        add(contentContainer, BorderLayout.CENTER);
        log("Admin-Panel Sitzung gestartet.");
    }



    // Umschalten des Wartungsmodus --> Unterschiedliche Anzeige und Log-Eintrag
    private void toggleMaintenanceMode() {
        boolean newState = !sessionManager.isMaintenanceModeActive();
        sessionManager.setMaintenanceModeActive(newState);
        updateStatusDisplay();

        if (newState) {
            log("WARNUNG: Wartungsmodus AKTIVIERT.");
            JOptionPane.showMessageDialog(this,
                    "Wartungsmodus AKTIV. Nur Administratoren können sich einloggen.",
                    "Systemstatus", JOptionPane.WARNING_MESSAGE);
        } else {
            log("System ist wieder ONLINE.");
            JOptionPane.showMessageDialog(this,
                    "System ONLINE. Alle Benutzer können sich anmelden.",
                    "Systemstatus", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Aktualisiert die Anzeige basierend auf dem aktuellen Wartungsmodus-Status
    private void updateStatusDisplay() {
        boolean active = sessionManager.isMaintenanceModeActive();

        if (active) {
            lblSystemStatus.setText("STATUS: WARTUNG");
            lblSystemStatus.setBackground(COLOR_STATUS_RED);
            btnToggleMaintenance.setText("System ONLINE schalten");
            btnToggleMaintenance.setBackground(COLOR_STATUS_GREEN);
        } else {
            lblSystemStatus.setText("STATUS: ONLINE");
            lblSystemStatus.setBackground(COLOR_STATUS_GREEN);
            btnToggleMaintenance.setText("Wartungsmodus AKTIVIEREN");
            btnToggleMaintenance.setBackground(COLOR_STATUS_RED);
        }
        
        this.revalidate();
        this.repaint();
        UIController.getInstance().updateMainWindow();
    }

    private void log(String message) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.append("[" + time + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    @Override public String getViewId() { return "admin-control-panel"; }
    @Override public String getViewTabTitle() { return "Systemsteuerung"; }
    @Override public JPanel getContent() { return this; }
    @Override public boolean equals(View view) { return view != null && view.getViewId().equals(getViewId()); }

    @Override
    public void updateSelf() {
        updateStatusDisplay();
    }
}