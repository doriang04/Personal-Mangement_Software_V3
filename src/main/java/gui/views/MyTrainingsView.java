package gui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import core.ServiceLocator;
import gui.UIController;
import static gui.UITheme.COLOR_ACCENT;
import static gui.UITheme.COLOR_BG_CONTENT;
import static gui.UITheme.COLOR_BORDER;
import static gui.UITheme.COLOR_HEADER_BG;
import static gui.UITheme.COLOR_HOVER;
import static gui.UITheme.COLOR_TEXT_HEADER;
import static gui.UITheme.createStyledButton;
import gui.components.SkillHistoryPanel;
import model.Employee;
import model.Training;
import model.TrainingManager;
import model.TrainingManager.Status;
import model.TrainingManager.TrainingHistoryEntry;

public class MyTrainingsView extends JPanel implements View {
    private Employee currentUser;
    private JTabbedPane tabbedPane;
    private JTable openTable;
    private DefaultTableModel openModel;
    private JTable historyTable;
    private DefaultTableModel historyModel;
    private SkillHistoryPanel mySkillsPanel;
    
    private int hoveredRow = -1; 

    public MyTrainingsView() {
        this.currentUser = ServiceLocator.getSessionManager().getCurrentUser();
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG_CONTENT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(true);
        header.setBackground(COLOR_HEADER_BG);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("Meine Schulungsübersicht");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_TEXT_HEADER);
        header.add(titleLabel, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));
        
        tabbedPane.addTab("Offene Schulungen", createOpenTrainingsPanel());
        tabbedPane.addTab("Historie (Erledigt)", createHistoryPanel());
        tabbedPane.addTab("Meine Skills", createMySkillsPanel());

        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.setOpaque(false);
        cardWrapper.setBorder(new EmptyBorder(30, 30, 30, 30));
        cardWrapper.add(tabbedPane, BorderLayout.CENTER);

        add(cardWrapper, BorderLayout.CENTER);
    }

    private JPanel createOpenTrainingsPanel() {
        // Tab Inhalt Trainings
        JPanel panel = createTabContentPanel();
        String[] columns = {"ID", "Schulung", "Beschreibung", "Zugewiesen am"};
        openModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        openTable = createStyledTable(openModel);
        openTable.getColumnModel().getColumn(0).setMinWidth(0);
        openTable.getColumnModel().getColumn(0).setMaxWidth(0);

        panel.add(new JScrollPane(openTable), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 15));
        footer.setOpaque(false);
        JButton btnComplete = createStyledButton("Als erledigt markieren", true);
        btnComplete.addActionListener(_ -> completeSelectedTraining());
        footer.add(btnComplete);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createHistoryPanel() {
        // Tab Inhalt Historie
        JPanel panel = createTabContentPanel();
        String[] columns = {"Schulung", "Abschlussdatum", "Zertifikat"};
        historyModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        historyTable = createStyledTable(historyModel);
        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMySkillsPanel() {
        // Tab Inhalt Meine Skills
        this.mySkillsPanel = new SkillHistoryPanel(this.currentUser, false, null);
        this.mySkillsPanel.setBackground(Color.WHITE);
        this.mySkillsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return this.mySkillsPanel;
    }

    private JPanel createTabContentPanel() {
        // Gemeinsames Panel-Layout für Tabs
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        return panel;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        // Gemeinsamer Tabellenstil
        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setGridColor(COLOR_BORDER);
        
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(Color.WHITE);
        table.setSelectionForeground(Color.BLACK);
        
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(COLOR_TEXT_HEADER);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        
        SelectionIndicatorRenderer renderer = new SelectionIndicatorRenderer();
        table.setDefaultRenderer(Object.class, renderer);

        // Hover-Effekt
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
        // Renderer für Hover-Effekt
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                                                       boolean hasFocus, int row, int column) {
            
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            boolean isHovered = (row == hoveredRow);
            
            if (isHovered) {
                c.setBackground(COLOR_HOVER);
                int firstVisibleCol = table.getColumnModel().getColumn(0).getWidth() > 0 ? 0 : 1;

                if (column == firstVisibleCol) {
                    c.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 5, 0, 0, COLOR_ACCENT), 
                        new EmptyBorder(0, 10, 0, 5) 
                    ));
                } else {
                    c.setBorder(new EmptyBorder(0, 15, 0, 5));
                }
            } else {
                c.setBackground(Color.WHITE);
                c.setBorder(new EmptyBorder(0, 15, 0, 5));
            }

            return c;
        }
    }


    private void loadData() {
        openModel.setRowCount(0);
        historyModel.setRowCount(0);

        if (currentUser == null) return;
        TrainingManager tm = currentUser.getTrainingManager();
        if (tm == null) return;

        ArrayList<TrainingHistoryEntry> entries = tm.getTrainingHistory();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        for (TrainingHistoryEntry entry : entries) {
            Training t = ServiceLocator.getTrainingContainer().getTrainingById(entry.getTrainingId());
            String title = (t != null) ? t.getTitle() : "Unbekannt (ID: " + entry.getTrainingId() + ")";
            String desc = (t != null) ? t.getDescription() : "-";

            if (entry.getStatus() == null || entry.getStatus() == Status.OPEN) {
                openModel.addRow(new Object[]{ entry.getTrainingId(), title, desc, entry.getAssignedAt().format(dtf) });
            } else if (entry.getStatus() == Status.DONE) {
                String date = (entry.getCompletedAt() != null) ? entry.getCompletedAt().format(dtf) : "-";
                historyModel.addRow(new Object[]{ title, date, "Anzeigen" });
            }
        }
    }

    private void completeSelectedTraining() {
        // Markierte Schulung als erledigt setzen
        int selectedRow = openTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Bitte eine Schulung auswählen.");
            return;
        }

        int modelRow = openTable.convertRowIndexToModel(selectedRow);
        int trainingId = (int) openModel.getValueAt(modelRow, 0);
        String title = (String) openModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Haben Sie '" + title + "' wirklich abgeschlossen?", "Bestätigung", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                currentUser.getTrainingManager().completeTraining(trainingId, LocalDate.now());
                UIController.getInstance().updateMainWindow();
                JOptionPane.showMessageDialog(this, "Erfolgreich erledigt!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Fehler: " + ex.getMessage());
            }
        }
    }

    @Override public String getViewId() { return "my-trainings-view"; }
    @Override public String getViewTabTitle() { return "Meine Schulungen"; }
    @Override public JPanel getContent() { return this; }
    @Override public boolean equals(View v) { return v != null && v.getViewId().equals(this.getViewId()); }

    @Override
    public void updateSelf() {
        this.currentUser = ServiceLocator.getSessionManager().getCurrentUser();
        loadData();
        if (this.mySkillsPanel != null) {
            this.mySkillsPanel.updateEmployee(this.currentUser);
            this.mySkillsPanel.loadData();
        }
    }
}