package gui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import core.ServiceLocator;
import core.SessionManager;
import gui.UIController;
import static gui.UITheme.COLOR_ACCENT;
import static gui.UITheme.COLOR_BG_CONTENT;
import static gui.UITheme.COLOR_BORDER;
import static gui.UITheme.COLOR_HEADER_BG;
import static gui.UITheme.COLOR_HOVER;
import static gui.UITheme.COLOR_TEXT_HEADER;
import gui.components.AssignTrainingDialog;
import gui.components.CreateTrainingDialog;
import model.Employee;
import model.Skill;
import model.Training;
import model.TrainingManager;
import model.TrainingManager.TrainingHistoryEntry;

public class TrainingManagementView extends JPanel implements View {

    private final SessionManager sessionManager;
    private final String currentUserRole;
    private final Employee currentUser;

    private JTabbedPane innerTabbedPane;
    private JTable teamProgressTable;
    private DefaultTableModel teamProgressModel;
    private JTable trainingCatalogTable;
    private DefaultTableModel trainingCatalogModel;

    private int hoveredRow = -1;

    public TrainingManagementView() {
        this.sessionManager = ServiceLocator.getSessionManager();
        String role = sessionManager.getUserPermission();
        this.currentUserRole = (role != null) ? role.toUpperCase() : "GUEST";
        this.currentUser = sessionManager.getCurrentUser();

        setLayout(new BorderLayout());
        setBackground(COLOR_BG_CONTENT);
        initUI();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(true);
        header.setBackground(COLOR_HEADER_BG);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel titleWrapper = new JPanel(new GridLayout(2, 1, 2, 2));
        titleWrapper.setOpaque(false);

        JLabel titleLabel = new JLabel("Schulungsverwaltung");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_TEXT_HEADER);

        JLabel subLabel = new JLabel("Status: Angemeldet als " + currentUserRole);
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subLabel.setForeground(new Color(107, 114, 128));

        titleWrapper.add(titleLabel);
        titleWrapper.add(subLabel);
        header.add(titleWrapper, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        innerTabbedPane = new JTabbedPane();
        innerTabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));

        innerTabbedPane.addTab("Schulungskatalog", createCatalogPanel());
        if (isPrivilegedManager()) {
            innerTabbedPane.addTab("Team-Fortschritt", createTeamProgressPanel());
        }

        JPanel tabWrapper = new JPanel(new BorderLayout());
        tabWrapper.setOpaque(false);
        tabWrapper.setBorder(new EmptyBorder(20, 20, 20, 20));
        tabWrapper.add(innerTabbedPane, BorderLayout.CENTER);
        add(tabWrapper, BorderLayout.CENTER);
    }

    private JPanel createCatalogPanel() {
        JPanel card = createCardPanel();
        String[] columns = {"ID", "Titel", "Beschreibung", "Dauer (h)", "Vermittelte Skills"};
        trainingCatalogModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        trainingCatalogTable = createStyledTable(trainingCatalogModel);
        card.add(new JScrollPane(trainingCatalogTable), BorderLayout.CENTER);

        if (isPrivilegedAdminOrHR()) {
            JButton btnCreate = createStyledButton("Neues Training erstellen");
            btnCreate.addActionListener(_ -> openCreateTrainingDialog());
            card.add(createButtonWrapper(btnCreate), BorderLayout.SOUTH);
        }
        loadCatalogData();
        return card;
    }

    private JPanel createTeamProgressPanel() {
        JPanel card = createCardPanel();
        String[] columns = {"Mitarbeiter", "Schulung", "Status", "Zuweisungsdatum"};
        teamProgressModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        teamProgressTable = createStyledTable(teamProgressModel);
        card.add(new JScrollPane(teamProgressTable), BorderLayout.CENTER);

        JButton btnAssign = createStyledButton("Schulung zuweisen");
        btnAssign.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            new AssignTrainingDialog(parentWindow, () -> UIController.getInstance().updateMainWindow()).setVisible(true);
        });
        card.add(createButtonWrapper(btnAssign), BorderLayout.SOUTH);
        loadTeamProgressData();
        return card;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(Color.WHITE);
        table.setSelectionBackground(Color.WHITE);
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(COLOR_BORDER);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        SelectionIndicatorRenderer renderer = new SelectionIndicatorRenderer();
        table.setDefaultRenderer(Object.class, renderer);

        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                table.repaint();
            }
        });
        return table;
    }

    private class SelectionIndicatorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            boolean isHovered = (row == hoveredRow);
            c.setBackground(isHovered ? COLOR_HOVER : Color.WHITE);
            c.setFont(new Font("SansSerif", isSelected ? Font.BOLD : Font.PLAIN, 13));
            if (column == 0 && isHovered) {
                c.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, COLOR_ACCENT),
                        new EmptyBorder(0, 11, 0, 10)
                ));
            } else {
                c.setBorder(new EmptyBorder(0, 15, 0, 10));
            }
            return c;
        }
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        return panel;
    }

    private JPanel createButtonWrapper(JButton btn) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        wrapper.setOpaque(false);
        wrapper.add(btn);
        return wrapper;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setBackground(COLOR_ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private boolean isPrivilegedAdminOrHR() { return currentUserRole.contains("ADMIN") || currentUserRole.contains("HR"); }
    private boolean isPrivilegedManager() {
        return currentUserRole.contains("ADMIN") || currentUserRole.contains("HR") ||
                currentUserRole.contains("CEO") || currentUserRole.contains("LEAD") ||
                currentUserRole.contains("MANAGER");
    }

    private void openCreateTrainingDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        new CreateTrainingDialog(parentWindow, () -> UIController.getInstance().updateMainWindow()).setVisible(true);
    }

    private void loadCatalogData() {
        if (trainingCatalogModel == null) return;
        trainingCatalogModel.setRowCount(0);
        for (Training t : ServiceLocator.getTrainingContainer().getTrainings()) {
            String skillsText = t.getSkillManager().getSkills().stream()
                    .map(entry -> ServiceLocator.getSkillContainer().getSkillById(entry.getSkillId()))
                    .filter(Objects::nonNull).map(Skill::getName).collect(Collectors.joining(", "));
            trainingCatalogModel.addRow(new Object[]{t.getId(), t.getTitle(), t.getDescription(), t.getLength(), skillsText.isEmpty() ? "-" : skillsText});
        }
    }

    public void loadTeamProgressData() {
        if (teamProgressModel == null) return;
        teamProgressModel.setRowCount(0);
        List<Employee> allEmployees = ServiceLocator.getEmployeeContainer().getEmployees();
        boolean seeAll = currentUserRole.contains("HR") || currentUserRole.contains("ADMIN") || currentUserRole.contains("CEO");
        for (Employee emp : allEmployees) {
            if (seeAll || (currentUser != null && emp.getTeamId() == currentUser.getTeamId())) {
                TrainingManager tm = emp.getTrainingManager();
                List<TrainingHistoryEntry> history = (tm != null) ? tm.getTrainingHistory() : null;
                if (history == null || history.isEmpty()) {
                    teamProgressModel.addRow(new Object[]{emp.getFirstName() + " " + emp.getLastName(), "-", "Keine Zuweisung", "-"});
                } else {
                    for (TrainingHistoryEntry entry : history) {
                        Training tr = ServiceLocator.getTrainingContainer().getTrainingById(entry.getTrainingId());
                        teamProgressModel.addRow(new Object[]{emp.getFirstName() + " " + emp.getLastName(), tr != null ? tr.getTitle() : "ID: " + entry.getTrainingId(), entry.getStatus(), entry.getAssignedAt()});
                    }
                }
            }
        }
    }

    @Override public String getViewId() { return "training-management-view"; }
    @Override public String getViewTabTitle() { return "Schulungsverwaltung"; }
    @Override public JPanel getContent() { return this; }
    @Override public boolean equals(View view) { return view != null && view.getViewId().equals(this.getViewId()); }
    @Override public void updateSelf() { loadCatalogData(); if (teamProgressModel != null) loadTeamProgressData(); }
}