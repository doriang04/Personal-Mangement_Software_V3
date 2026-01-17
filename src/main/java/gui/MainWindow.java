package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import core.ServiceLocator;
import static gui.UITheme.COLOR_ACCENT;
import static gui.UITheme.COLOR_BG_CONTENT;
import static gui.UITheme.COLOR_HEADER_BG;
import static gui.UITheme.COLOR_TEXT_HEADER;
import gui.views.View;

public class MainWindow extends JFrame {

    private static MainWindow instance;
    private final JPanel contentWrapper;
    private JTabbedPane tabbedPane;
    private JPanel navigationPanel;
    private JLabel systemStatusLabel;
    private JPanel northHeader;

    public static synchronized MainWindow getInstance() {
        if (instance == null) {
            instance = new MainWindow();
        }
        return instance;
    }

    private MainWindow() {
        setTitle("Personalmanagement Software");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(COLOR_BG_CONTENT);
        setContentPane(contentWrapper);
    }

    public void showSingleView(View view, String title, int width, int height) {
        // Entferne alle vorherigen Inhalte
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
        // Entferne alle vorherigen Inhalte
        contentWrapper.removeAll();
        contentWrapper.setLayout(new BorderLayout());

        // links: Navigation
        contentWrapper.add(createWestPanelContainer(logoutAction), BorderLayout.WEST);

        // Center: Header + Tabs
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(COLOR_BG_CONTENT);
        centerWrapper.setBorder(new EmptyBorder(20, 25, 20, 25));

        northHeader = createModernHeader(userName, role);
        centerWrapper.add(northHeader, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setBorder(new EmptyBorder(10, 0, 0, 0));
        centerWrapper.add(tabbedPane, BorderLayout.CENTER);

        contentWrapper.add(centerWrapper, BorderLayout.CENTER);

        setTitle("Personalmanagement Software");
        setSize(1280, 850);
        setLocationRelativeTo(null);
        
        updateSystemStatusLabel(); 
        
        revalidate();
        repaint();
    }

    //  Header
    private JPanel createModernHeader(String name, String role) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        
        JLabel lblWelcome = new JLabel("Hallo, " + name);
        lblWelcome.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblWelcome.setForeground(COLOR_TEXT_HEADER);
        
        JLabel lblRole = new JLabel("Rolle: " + role);
        lblRole.setForeground(Color.GRAY);
        
        textPanel.add(lblWelcome);
        textPanel.add(lblRole);
        
        header.add(textPanel, BorderLayout.WEST);

        systemStatusLabel = new JLabel();
        systemStatusLabel.setForeground(new Color(217, 56, 56));
        systemStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(systemStatusLabel, BorderLayout.CENTER);
        
        javax.swing.Icon avatarIcon;
        java.net.URL avatarURL = getClass().getResource("/gui/Icons/Avatar.png");
        if (avatarURL != null) {
            ImageIcon raw = new ImageIcon(avatarURL);
            java.awt.Image scaled = raw.getImage().getScaledInstance(56, 56, java.awt.Image.SCALE_SMOOTH);
            avatarIcon = new ImageIcon(scaled);
        } else {
            avatarIcon = new javax.swing.plaf.metal.MetalIconFactory.TreeLeafIcon();
        }
        JLabel avatar = new JLabel(avatarIcon);
        avatar.setPreferredSize(new Dimension(56, 56));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setOpaque(false);
        avatar.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true));
        header.add(avatar, BorderLayout.EAST);
        
        return header;
    }

    private JScrollPane createWestPanelContainer(ActionListener logoutAction) {
        // Navigation Panel
        navigationPanel = new JPanel();
        navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.Y_AXIS));
        navigationPanel.setBorder(new EmptyBorder(20, 15, 20, 15));
        navigationPanel.setBackground(COLOR_HEADER_BG);

        JLabel logo = new JLabel("Personal Manager");
        logo.setFont(new Font("SansSerif", Font.BOLD, 18));
        logo.setForeground(COLOR_ACCENT);
        logo.setBorder(new EmptyBorder(0, 10, 30, 0));
        navigationPanel.add(logo);

        JScrollPane scrollPane = new JScrollPane(navigationPanel);
        scrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));
        scrollPane.setPreferredSize(new Dimension(240, 0));
        return scrollPane;
    }

    public void addNavigationSection(String title) {
        // Section Label
        if (navigationPanel == null) return;
        JLabel label = new JLabel(title.toUpperCase());
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(new Color(156, 163, 175));
        label.setBorder(new EmptyBorder(20, 10, 10, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        navigationPanel.add(label);
        navigationPanel.revalidate();
    }

    public void addNavigationEntry(String text, Runnable onClickAction) {
        // Navigation Button
        if (navigationPanel == null) return;

        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(COLOR_HEADER_BG);
        button.setForeground(new Color(75, 85, 99));
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setBorder(new EmptyBorder(0, 10, 0, 0));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                button.setBackground(new Color(243, 232, 255)); 
                button.setForeground(COLOR_ACCENT);
            }
            public void mouseExited(MouseEvent e) { 
                button.setBackground(COLOR_HEADER_BG); 
                button.setForeground(new Color(75, 85, 99));
            }
        });

        button.addActionListener(_ -> onClickAction.run());
        navigationPanel.add(button);
        navigationPanel.revalidate();
    }

    public void addSpacerToNav() {
        if(navigationPanel != null) navigationPanel.add(Box.createVerticalStrut(15));
    }

    // Fügt am Ende der Navigation einen Logout-Button hinzu
    public void addGlueToNav(ActionListener logoutAction) {
        if (navigationPanel == null) return;

        navigationPanel.add(Box.createVerticalGlue());
        navigationPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        navigationPanel.add(Box.createVerticalStrut(10));

        // Logout Button
        JButton btnLogout = new JButton("🚪 Logout");
        btnLogout.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogout.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLogout.setForeground(new Color(220, 38, 38));
        btnLogout.setContentAreaFilled(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnLogout.addActionListener(logoutAction);
        
        // Hover Effekt für Logout
        btnLogout.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnLogout.setForeground(Color.RED.darker()); }
            public void mouseExited(MouseEvent e) { btnLogout.setForeground(new Color(220, 38, 38)); }
        });

        navigationPanel.add(btnLogout);
        navigationPanel.add(Box.createVerticalStrut(10));
    }

    public void openTab(View view, boolean closable) {
        JComponent component = view.getContent();
        component.putClientProperty("viewId", view.getViewId()); 
        
        component.setBackground(Color.WHITE);
        String title = view.getViewTabTitle();

        tabbedPane.addTab(title, component);
        int index = tabbedPane.indexOfComponent(component);

        JPanel tabComponent = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabComponent.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(55, 65, 81));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        tabComponent.add(titleLabel);

        if (closable) {
            JButton closeButton = new JButton("×");
            closeButton.setFont(new Font("SansSerif", Font.PLAIN, 16));
            closeButton.setBorderPainted(false);
            closeButton.setContentAreaFilled(false);
            closeButton.setForeground(Color.GRAY);
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
        // Prüfe ob Tab mit der View bereits existiert
        if (tabbedPane == null) return false;
        
        String searchId = view.getViewId();
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component c = tabbedPane.getComponentAt(i);
            if (c instanceof JComponent) {
                Object tabId = ((JComponent) c).getClientProperty("viewId");
                if (searchId.equals(tabId)) {
                    tabbedPane.setSelectedIndex(i);
                    return true;
                }
            }
        }
        return false;
    }

    private void updateSystemStatusLabel() {
        // Aktualisiere Systemstatus im Header
        if (systemStatusLabel == null) return;

        boolean maintenance =
                ServiceLocator.getSessionManager().isMaintenanceModeActive();

        systemStatusLabel.setText(
                maintenance ? "SYSTEM IST IM WARTUNGSMODUS" : ""
        );
    }

    public void updateSelf() {
        // Aktualisiere Header und alle Tabs
        updateSystemStatusLabel();

        if (northHeader != null) {
            northHeader.revalidate();
            northHeader.repaint();
        }

        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            View tab_component = (View) tabbedPane.getComponentAt(i);
            tab_component.updateSelf();
        }

        revalidate();
        repaint();
    }
}