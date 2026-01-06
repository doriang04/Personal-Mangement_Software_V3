package gui;

import model.ServiceLocator;
import gui.views.DashboardView;
import gui.views.EmployeeDetailView;
import gui.views.EmployeeSearchView;
import gui.views.View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// TODO make it look human
// TODO test this class

/**
 * Das Hauptfenster der Anwendung, das nach dem Login angezeigt wird.
 * Es verwaltet die globale Navigation und die in Tabs dargestellten Inhalts-Views.
 *
 * @author Ihr Name
 * @version 2.0 (Tab-basiert)
 */
public class MainWindow extends JFrame {

    private static MainWindow instance;

    private JTabbedPane tabbedPane;
    private JLabel userLabel;

    public static synchronized MainWindow getInstance() {
        if (instance == null) {
            instance = new MainWindow();
        }
        return instance;
    }

    private MainWindow() {
        setTitle("Personalmanagement Software");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        initUI();
    }

    /**
     * Initialisiert die Hauptkomponenten der Benutzeroberfläche (Header, Navigation, Tab-Bereich).
     */
    private void initUI() {
        setLayout(new BorderLayout(5, 5)); // Abstände zwischen den Bereichen

        // 1. Oberer Bereich (NORTH)
        add(createNorthPanel(), BorderLayout.NORTH);

        // 2. Linker Bereich (WEST) - Navigation
        add(createWestPanel(), BorderLayout.WEST);

        // 3. Zentraler Bereich (CENTER) - Tab-Paneel
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT); // Erlaubt Scrollen bei vielen Tabs
        add(tabbedPane, BorderLayout.CENTER);

        // 4. Standard-Tab öffnen (Dashboard), nicht schließbar
        openTab(new DashboardView(), false);
    }

    /**
     * Erstellt das obere Panel mit Benutzerinformationen und Logout-Button.
     */
    private JPanel createNorthPanel() {
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBorder(new EmptyBorder(5, 10, 5, 10)); // Innenabstand

        userLabel = new JLabel("Angemeldet als: " + ServiceLocator.getSessionManager().getUserFirstNameAndLastName() + " (" + ServiceLocator.getSessionManager().getUserPermission() + ")");
        userLabel.setFont(userLabel.getFont().deriveFont(Font.BOLD));
        northPanel.add(userLabel, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            // Hier würde die Logout-Logik implementiert
            System.out.println("Logout-Prozess gestartet...");
            // z.B. this.dispose(); LoginView.getInstance().setVisible(true);
            JOptionPane.showMessageDialog(this, "Sie wurden abgemeldet.", "Logout", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0); // Für dieses Beispiel beenden wir die Anwendung
        });
        northPanel.add(btnLogout, BorderLayout.EAST);

        return northPanel;
    }

    /**
     * Erstellt das linke Navigationspanel basierend auf der Rolle des Benutzers.
     */
    private JComponent createWestPanel() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        navPanel.add(new JLabel("Navigation") {
            {
                setFont(getFont().deriveFont(Font.BOLD, 16f));
                setBorder(new EmptyBorder(0, 0, 10, 0));
            }
        });

        // --- Aktionen für alle Rollen ---
        addNavButton(navPanel, "Dashboard", () -> focusOrCreateTab(DashboardView.class, () -> new DashboardView(), false));
        addNavButton(navPanel, "Mitarbeiter suchen", () -> openTab(new EmployeeSearchView(), true));
        addNavButton(navPanel, "Mein Profil", () -> openTab(new EmployeeDetailView("Eigenes Profil"), true));

        navPanel.add(Box.createVerticalStrut(20)); // Abstand

        // --- Rollenspezifische Aktionen ---
        if (ServiceLocator.getSessionManager().getUserPermission().equals("HR")) {
            addSectionTitle(navPanel, "HR");
            addNavButton(navPanel, "Neuen Mitarbeiter anlegen", () -> openTab(new EmployeeDetailView(), true));
            // addNavButton(navPanel, "Schulungsverwaltung", () -> openTab(new TrainingManagementView(), true)); TODO
        }

        if (ServiceLocator.getSessionManager().getUserPermission().equals("TEAM_LEAD")) {
            addSectionTitle(navPanel, "Teamleiter");
            // addNavButton(navPanel, "Mein Team-Überblick", () -> openTab(new TeamOverviewView(), true)); TODO
            // addNavButton(navPanel, "Schulungsvorschlag", () -> new SubmitTrainingSuggestionDialog(this).setVisible(true)); TODO
        }

        if (ServiceLocator.getSessionManager().getUserPermission().equals("ADMIN")) {
            addSectionTitle(navPanel, "Admin");
            // addNavButton(navPanel, "Systemsteuerung", () -> openTab(new AdminControlPanelView(), true)); TODO
        }

        navPanel.add(Box.createVerticalGlue()); // Füllt den restlichen Platz

        // Umschließen mit einem JScrollPane, falls die Navigation zu lang wird
        return new JScrollPane(navPanel);
    }

    /**
     * Fügt dem Navigationspanel eine Sektionsüberschrift hinzu.
     */
    private void addSectionTitle(JPanel panel, String title) {
        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.ITALIC, 14f));
        label.setBorder(new EmptyBorder(5, 0, 5, 0));
        panel.add(label);
    }

    /**
     * Hilfsmethode zum Erstellen und Hinzufügen eines Navigationsbuttons.
     */
    private void addNavButton(JPanel panel, String text, Runnable action) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
        button.addActionListener(e -> action.run());
        panel.add(button);
    }

    /**
     * Öffnet eine neue View in einem Tab. Wenn `closable` true ist, wird ein "x"-Button hinzugefügt.
     *
     * @param view Die anzuzeigende View-Instanz.
     * @param closable Gibt an, ob der Tab vom Benutzer geschlossen werden kann.
     */
    public void openTab(View view, boolean closable) {
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
            // Styling des Buttons für ein minimalistisches Aussehen
            closeButton.setMargin(new Insets(0, 2, 0, 2));
            closeButton.setFont(new Font("Arial", Font.BOLD, 12));
            closeButton.setContentAreaFilled(false);
            closeButton.setBorderPainted(false);
            closeButton.setFocusable(false);

            // Hover-Effekt
            closeButton.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    closeButton.setForeground(Color.RED);
                }
                public void mouseExited(MouseEvent e) {
                    closeButton.setForeground(Color.BLACK);
                }
            });

            closeButton.addActionListener(e -> {
                int i = tabbedPane.indexOfTabComponent(tabComponent);
                if (i != -1) {
                    tabbedPane.remove(i);
                }
            });
            tabComponent.add(closeButton);
        }

        tabbedPane.setTabComponentAt(index, tabComponent);

        // Den neu geöffneten Tab auswählen
        tabbedPane.setSelectedComponent(component);
    }

    /**
     * Fokussiert einen bereits offenen Tab eines bestimmten Typs oder erstellt ihn neu,
     * falls er nicht existiert. Nützlich für "Single-Instance"-Views wie das Dashboard.
     *
     * @param viewClass Die Klasse der zu suchenden View.
     * @param viewSupplier Eine Funktion, die bei Bedarf eine neue Instanz der View erstellt.
     * @param closable Gibt an, ob der Tab schließbar sein soll, falls er neu erstellt wird.
     */
    public <T extends View> void focusOrCreateTab(Class<T> viewClass, java.util.function.Supplier<T> viewSupplier, boolean closable) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component comp = tabbedPane.getComponentAt(i);
            if (viewClass.isInstance(comp)) {
                tabbedPane.setSelectedIndex(i);
                return; // Tab gefunden und fokussiert
            }
        }
        // Tab nicht gefunden, also neu erstellen
        openTab(viewSupplier.get(), closable);
    }

}