package gui;

import gui.views.View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainWindow extends JFrame {

    private static MainWindow instance;

    private final JPanel contentWrapper;
    private JTabbedPane tabbedPane;

    private JPanel navigationPanel;

    public static synchronized MainWindow getInstance() {
        if (instance == null) {
            instance = new MainWindow();
        }
        return instance;
    }

    private MainWindow() {
        setTitle("Personalmanagement Software");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 550);
        setLocationRelativeTo(null);

        contentWrapper = new JPanel(new BorderLayout());
        setContentPane(contentWrapper);
    }

    public void showSingleView(View view, String title, int width, int height) {
        contentWrapper.removeAll();
        contentWrapper.setLayout(new BorderLayout());
        contentWrapper.add(view.getContent(), BorderLayout.CENTER);

        setTitle(title);
        setSize(width, height);
        setLocationRelativeTo(null);

        revalidate();
        repaint();

        if (!isVisible()) setVisible(true);
    }

    public void setupMainLayout(String userName, String role, ActionListener logoutAction) {
        contentWrapper.removeAll();
        contentWrapper.setLayout(new BorderLayout(5, 5));

        contentWrapper.add(createNorthPanel(userName, role, logoutAction), BorderLayout.NORTH);

        contentWrapper.add(createWestPanelContainer(), BorderLayout.WEST);

        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setBorder(new EmptyBorder(0, 5, 0, 0));
        contentWrapper.add(tabbedPane, BorderLayout.CENTER);

        setTitle("Personalmanagement Software");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        revalidate();
        repaint();
    }

    private JPanel createNorthPanel(String name, String role, ActionListener logoutAction) {
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBorder(new EmptyBorder(8, 12, 8, 12));
        northPanel.setBackground(new Color(230, 230, 230));

        JLabel userLabel = new JLabel("Angemeldet als: " + name + " (" + role + ")");
        userLabel.setFont(userLabel.getFont().deriveFont(Font.BOLD));
        northPanel.add(userLabel, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(logoutAction);
        northPanel.add(btnLogout, BorderLayout.EAST);

        return northPanel;
    }

    private JScrollPane createWestPanelContainer() {
        navigationPanel = new JPanel();
        navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.Y_AXIS));
        navigationPanel.setBorder(new EmptyBorder(10, 5, 10, 5));
        navigationPanel.setBackground(new Color(245, 245, 245));

        JLabel navTitle = new JLabel("Navigation");
        navTitle.setFont(navTitle.getFont().deriveFont(Font.BOLD, 16f));
        navTitle.setBorder(new EmptyBorder(0, 5, 15, 0));
        navTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        navigationPanel.add(navTitle);

        JScrollPane scrollPane = new JScrollPane(navigationPanel);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(220, 0));
        return scrollPane;
    }

    public void addNavigationSection(String title) {
        if (navigationPanel == null) return;
        JLabel label = new JLabel(title.toUpperCase());
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
        label.setForeground(Color.GRAY);
        label.setBorder(new EmptyBorder(15, 8, 5, 0)); // Etwas mehr Abstand oben
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        navigationPanel.add(label);
        navigationPanel.revalidate();
    }

    public void addNavigationEntry(String text, Runnable onClickAction) {
        if (navigationPanel == null) return;

        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(navigationPanel.getBackground());
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setBorder(new EmptyBorder(0, 10, 0, 0));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(new Color(220, 220, 220)); }
            public void mouseExited(MouseEvent e) { button.setBackground(navigationPanel.getBackground()); }
        });

        button.addActionListener(_ -> onClickAction.run());
        navigationPanel.add(button);
        navigationPanel.revalidate();
    }

    public void addSpacerToNav() {
        if(navigationPanel != null) navigationPanel.add(Box.createVerticalStrut(20));
    }

    public void addGlueToNav() {
        if(navigationPanel != null) navigationPanel.add(Box.createVerticalGlue());
    }

    public void openTab(View view, boolean closable) {
        JComponent component = view.getContent();
        String title = view.getViewTabTitle();

        tabbedPane.addTab(title, component);
        int index = tabbedPane.indexOfComponent(component);

        JPanel tabComponent = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabComponent.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        tabComponent.add(titleLabel);

        if (closable) {
            JButton closeButton = new JButton("x");
            closeButton.setBorderPainted(false);
            closeButton.setContentAreaFilled(false);
            closeButton.setMargin(new Insets(0,2,0,2));
            closeButton.addActionListener(_ -> {
                int i = tabbedPane.indexOfTabComponent(tabComponent);
                if (i != -1) tabbedPane.remove(i);
            });
            tabComponent.add(closeButton);
        }

        tabbedPane.setTabComponentAt(index, tabComponent);
        tabbedPane.setSelectedComponent(component);
    }

    public boolean selectTabIfExists(View view) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (view.equals(tabbedPane.getComponentAt(i))) {
                tabbedPane.setSelectedIndex(i);
                return true;
            }
        }
        return false;
    }

    public boolean isTabOpen(View view) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (view.equals(tabbedPane.getComponentAt(i))) return true;
        }
        return false;
    }
}