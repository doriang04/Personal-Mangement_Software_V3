package gui;

import model.ServiceLocator;
import core.SessionManager;
import gui.views.DashboardView;
import gui.views.EmployeeDetailView;
import gui.views.EmployeeSearchView;
import gui.views.LoginView;
import gui.views.View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// TODO make it look human
// TODO test this class

/**
 * Das Hauptfenster der Anwendung. Es agiert als Container, der entweder die
 * Login-Ansicht oder die Hauptanwendungsoberfläche nach einem erfolgreichen
 * Login anzeigt.
 *
 * @author Ihr Name
 * @version 2.1 (Mit Login-Zustandsmanagement)
 */
public class MainWindow extends JFrame {

    private static MainWindow instance;

    // Ein Haupt-Panel, das es uns erlaubt, einfach zwischen Login und Haupt-UI zu wechseln.
    private final JPanel contentWrapper;
    private JTabbedPane tabbedPane;
    private final SessionManager sessionManager;

    public static synchronized MainWindow getInstance() {
        if (instance == null) {
            instance = new MainWindow();
        }
        return instance;
    }

    private MainWindow() {
        setTitle("Personalmanagement Software");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Startgröße kann kleiner sein, wird nach Login angepasst
        setSize(450, 550);
        setLocationRelativeTo(null);

        // Wir verwenden einen ServiceLocator, um lose Kopplung zu gewährleisten
        this.sessionManager = ServiceLocator.getSessionManager();

        // Statt direkt dem JFrame Komponenten hinzuzufügen, nutzen wir einen Wrapper.
        // Das macht das Austauschen des Inhalts (Login vs. App) trivial.
        contentWrapper = new JPanel(new BorderLayout());
        setContentPane(contentWrapper);
    }

    /**
     * Zeigt die Login-Ansicht an. Dies ist der initiale Zustand der Anwendung.
     */
    public void showLoginView() {
        // Alte UI-Komponenten entfernen, falls vorhanden (nach Logout)
        contentWrapper.removeAll();

        // Hier wird angenommen, dass LoginView ein JPanel ist und das MainWindow als
        // Callback benötigt, um den Login-Erfolg zu melden.
        LoginView loginView = new LoginView(this);
        contentWrapper.add(loginView, BorderLayout.CENTER);

        setTitle("Personalmanagement Software - Login");
        setSize(450, 550);
        setLocationRelativeTo(null);

        revalidate();
        repaint();

        // Erst jetzt das Fenster sichtbar machen
        if (!isVisible()) {
            setVisible(true);
        }
    }

    /**
     * Diese Methode wird von der LoginView aufgerufen, nachdem die Authentifizierung erfolgreich war.
     * Sie initialisiert die komplette Hauptansicht für den eingeloggten Benutzer.
     */
    public void onLoginSuccess() {
        // Login-View entfernen
        contentWrapper.removeAll();

        // Die volle UI für den eingeloggten Zustand initialisieren
        initializeLoggedInUI();

        setTitle("Personalmanagement Software");
        setSize(1200, 800);
        setLocationRelativeTo(null);

        revalidate();
        repaint();
    }

    /**
     * Initialisiert die Hauptkomponenten der Benutzeroberfläche für einen eingeloggten Benutzer.
     */
    private void initializeLoggedInUI() {
        contentWrapper.setLayout(new BorderLayout(5, 5));

        // 1. Oberer Bereich (NORTH)
        contentWrapper.add(createNorthPanel(), BorderLayout.NORTH);

        // 2. Linker Bereich (WEST) - Navigation
        contentWrapper.add(createWestPanel(), BorderLayout.WEST);

        // 3. Zentraler Bereich (CENTER) - Tab-Paneel
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setBorder(new EmptyBorder(0, 5, 0, 0));
        contentWrapper.add(tabbedPane, BorderLayout.CENTER);

        // 4. Standard-Tab öffnen (Dashboard), nicht schließbar
        openTab(new DashboardView(), false);
    }

    private JPanel createNorthPanel() {
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBorder(new EmptyBorder(8, 12, 8, 12));
        northPanel.setBackground(new Color(230, 230, 230)); // Heller Hintergrund

        JLabel userLabel = new JLabel("Angemeldet als: " + sessionManager.getUserFirstNameAndLastName() + " (" + sessionManager.getUserPermission() + ")");
        userLabel.setFont(userLabel.getFont().deriveFont(Font.BOLD));
        northPanel.add(userLabel, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            sessionManager.logout(); // Session beenden
            showLoginView();       // Zurück zum Login-Bildschirm
        });
        northPanel.add(btnLogout, BorderLayout.EAST);

        return northPanel;
    }

    private JComponent createWestPanel() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(new EmptyBorder(10, 5, 10, 5));
        navPanel.setBackground(new Color(245, 245, 245)); // Etwas dunkler als weiß

        JLabel navTitle = new JLabel("Navigation");
        navTitle.setFont(navTitle.getFont().deriveFont(Font.BOLD, 16f));
        navTitle.setBorder(new EmptyBorder(0, 5, 15, 0));
        navTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        navPanel.add(navTitle);

        String userPermission = sessionManager.getUserPermission();

        // --- Aktionen für alle Rollen ---
        addNavButton(navPanel, "Dashboard", "icons/dashboard.png", () -> focusOrCreateTab(DashboardView.class, DashboardView::new, false));
        addNavButton(navPanel, "Mitarbeiter suchen", "icons/search.png", () -> openTab(new EmployeeSearchView(), true));
        addNavButton(navPanel, "Mein Profil", "icons/profile.png", () -> openTab(new EmployeeDetailView("Eigenes Profil"), true));

        navPanel.add(Box.createVerticalStrut(20));

        // --- Rollenspezifische Aktionen ---
        if ("HR".equals(userPermission)) {
            addSectionTitle(navPanel, "HR");
            addNavButton(navPanel, "Neuen Mitarbeiter anlegen", "icons/add_user.png", () -> openTab(new EmployeeDetailView(), true));
        }

        if ("TEAM_LEAD".equals(userPermission)) {
            addSectionTitle(navPanel, "Teamleiter");
            // addNavButton(navPanel, "Mein Team-Überblick", "icons/team.png", () -> openTab(new TeamOverviewView(), true));
        }

        if ("ADMIN".equals(userPermission)) {
            addSectionTitle(navPanel, "Admin");
            // addNavButton(navPanel, "Systemsteuerung", "icons/settings.png", () -> openTab(new AdminControlPanelView(), true));
        }

        navPanel.add(Box.createVerticalGlue()); // Füllt den restlichen Platz

        JScrollPane scrollPane = new JScrollPane(navPanel);
        scrollPane.setBorder(null); // Kein Rand für den ScrollPane
        scrollPane.setPreferredSize(new Dimension(220, 0));
        return scrollPane;
    }

    private void addSectionTitle(JPanel panel, String title) {
        JLabel label = new JLabel(title.toUpperCase());
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
        label.setForeground(Color.GRAY);
        label.setBorder(new EmptyBorder(10, 8, 5, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
    }

    /**
     * Erstellt einen modernen, flachen Navigationsbutton (ähnlich einem Link).
     */
    private void addNavButton(JPanel panel, String text, String iconPath, Runnable action) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true); // Wichtig für Hintergrundfarbe
        button.setBackground(panel.getBackground());
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setBorder(new EmptyBorder(0, 10, 0, 0));

        // Optional: Icon hinzufügen (Pfad muss stimmen!)
        // try {
        //     button.setIcon(new ImageIcon(new URL(iconPath)));
        //     button.setIconTextGap(10);
        // } catch (Exception e) { /* Icon nicht gefunden, ignoriere */ }

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(220, 220, 220));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(panel.getBackground());
            }
        });

        button.addActionListener(e -> action.run());
        panel.add(button);
    }

    public void openTab(View view, boolean closable) {
        // Identisch zu deiner Version, aber verwendet die Interface-Methode
        JComponent component = view.getComponent();
        String title = view.getViewTabTitle();

        // Neuen Tab hinzufügen und seinen Index ermitteln
        tabbedPane.addTab(title, component);
        int index = tabbedPane.indexOfComponent(component);

        // Ein benutzerdefiniertes Panel für den Tab-Reiter erstellen (Titel + Schließen-Button)
        JPanel tabComponent = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabComponent.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        tabComponent.add(titleLabel);

        if (closable) {
            JButton closeButton = new JButton("x");
            closeButton.setMargin(new Insets(0, 2, 0, 2));
            closeButton.setFont(new Font("Arial", Font.BOLD, 12));
            closeButton.setContentAreaFilled(false);
            closeButton.setBorderPainted(false);
            closeButton.setFocusable(false);
            closeButton.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { closeButton.setForeground(Color.RED); }
                public void mouseExited(MouseEvent e) { closeButton.setForeground(Color.BLACK); }
            });
            closeButton.addActionListener(e -> {
                int i = tabbedPane.indexOfTabComponent(tabComponent);
                if (i != -1) tabbedPane.remove(i);
            });
            tabComponent.add(closeButton);
        }

        tabbedPane.setTabComponentAt(index, tabComponent);
        tabbedPane.setSelectedComponent(component);
    }

    public <T extends View> void focusOrCreateTab(Class<T> viewClass, java.util.function.Supplier<T> viewSupplier, boolean closable) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (viewClass.isInstance(tabbedPane.getComponentAt(i))) {
                tabbedPane.setSelectedIndex(i);
                return;
            }
        }
        openTab(viewSupplier.get(), closable);
    }
}