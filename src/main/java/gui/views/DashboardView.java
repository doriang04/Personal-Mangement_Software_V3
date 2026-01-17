package gui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import core.ServiceLocator;
import gui.UIController;
import static gui.UITheme.COLOR_ACCENT;
import static gui.UITheme.COLOR_BG_CONTENT;
import static gui.UITheme.COLOR_BORDER;
import static gui.UITheme.COLOR_HEADER_BG;
import static gui.UITheme.COLOR_TEXT_BODY;
import static gui.UITheme.COLOR_TEXT_HEADER;
import model.Skill;
import model.SkillManager;
import model.Training;
import model.TrainingManager;
public class DashboardView extends JPanel implements View {

    private JPanel trainingPanel;
    private JPanel skillsPanel;
    private JLabel welcomeLabel;

    public DashboardView() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG_CONTENT);

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Mittelteil
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setOpaque(false);
        centerContainer.setBorder(new EmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 25, 0);

        trainingPanel = createDashboardCard("Nächste anstehende Schulung");
        skillsPanel = createDashboardCard("Inaktive Skills (Handlungsbedarf)");

        centerContainer.add(trainingPanel, gbc);
        centerContainer.add(skillsPanel, gbc);
        gbc.weighty = 1.0;
        centerContainer.add(Box.createGlue(), gbc);

        add(new JScrollPane(centerContainer), BorderLayout.CENTER);

        loadDashboardData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(true);
        header.setBackground(COLOR_HEADER_BG);
        header.setBorder(new EmptyBorder(25, 40, 25, 40));

        JPanel textWrapper = new JPanel(new GridLayout(2, 1, 2, 2));
        textWrapper.setOpaque(false);

        welcomeLabel = new JLabel("Willkommen zurück, " + ServiceLocator.getSessionManager().getUserFirstNameAndLastName());
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        welcomeLabel.setForeground(COLOR_TEXT_HEADER);

        JLabel subLabel = new JLabel("Hier ist deine aktuelle Übersicht");
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subLabel.setForeground(COLOR_TEXT_BODY);

        textWrapper.add(welcomeLabel);
        textWrapper.add(subLabel);

        header.add(textWrapper, BorderLayout.WEST);
        return header;
    }

    private JPanel createDashboardCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(25, 25, 25, 25)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(COLOR_TEXT_HEADER);
        card.add(titleLabel, BorderLayout.NORTH);

        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setOpaque(false);
        card.add(contentArea, BorderLayout.CENTER);

        return card;
    }

    private void loadDashboardData() {
        Training openTraining = null;
        if (ServiceLocator.getSessionManager().getCurrentUser() != null) {
            ArrayList<TrainingManager.TrainingHistoryEntry> allTrainings = 
                ServiceLocator.getSessionManager().getCurrentUser().getTrainingManager().getTrainingHistory();
            for (TrainingManager.TrainingHistoryEntry entry : allTrainings) {
                if (entry.getStatus() == TrainingManager.Status.OPEN) {
                    openTraining = ServiceLocator.getTrainingContainer().getTrainingById(entry.getTrainingId());
                    break;
                }
            }
        }
        updateTrainingCard(openTraining);

        if (ServiceLocator.getSessionManager().getCurrentUser() != null) {
            ArrayList<SkillManager.SkillHistoryEntry> inactive = 
                ServiceLocator.getSessionManager().getCurrentUser().getSkillManager().getInactiveSkills();
            updateSkillsCard(inactive);
        }
    }

    private void updateTrainingCard(Training training) {
        JPanel contentArea = (JPanel) trainingPanel.getComponent(1);
        contentArea.removeAll();

        if (training != null) {
            JPanel infoPanel = new JPanel(new BorderLayout(15, 0));
            infoPanel.setOpaque(false);

            JLabel nameLabel = new JLabel(training.getTitle());
            nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
            nameLabel.setForeground(COLOR_TEXT_HEADER);
            infoPanel.add(nameLabel, BorderLayout.CENTER);

            JButton btn = createStyledButton("Zu meinen Schulungen", true);
            btn.addActionListener(e -> UIController.getInstance().openTabOrFocus(new MyTrainingsView(), true));
            infoPanel.add(btn, BorderLayout.EAST);

            contentArea.add(infoPanel, BorderLayout.CENTER);
        } else {
            JLabel noTask = new JLabel("Alle Schulungen abgeschlossen. Gute Arbeit!");
            noTask.setFont(new Font("SansSerif", Font.ITALIC, 14));
            noTask.setForeground(COLOR_TEXT_BODY);
            contentArea.add(noTask, BorderLayout.CENTER);
        }
        contentArea.revalidate();
        contentArea.repaint();
    }

    private void updateSkillsCard(ArrayList<SkillManager.SkillHistoryEntry> skills) {
        JPanel contentArea = (JPanel) skillsPanel.getComponent(1);
        contentArea.removeAll();

        if (skills != null && !skills.isEmpty()) {
            JPanel listWrapper = new JPanel();
            listWrapper.setLayout(new BoxLayout(listWrapper, BoxLayout.Y_AXIS));
            listWrapper.setOpaque(false);

            for (SkillManager.SkillHistoryEntry entry : skills) {
                Skill s = ServiceLocator.getSkillContainer().getSkillById(entry.getSkillId());
                if (s == null) continue;

                JLabel skillLabel = new JLabel("• " + s.getName());
                skillLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
                skillLabel.setForeground(COLOR_TEXT_HEADER);
                skillLabel.setBorder(new EmptyBorder(4, 0, 4, 0));
                listWrapper.add(skillLabel);
            }
            contentArea.add(listWrapper, BorderLayout.CENTER);
        } else {
            JLabel allGood = new JLabel("Keine inaktiven Skills vorhanden.");
            allGood.setFont(new Font("SansSerif", Font.ITALIC, 14));
            allGood.setForeground(COLOR_TEXT_BODY);
            contentArea.add(allGood, BorderLayout.CENTER);
        }
        contentArea.revalidate();
        contentArea.repaint();
    }

    private JButton createStyledButton(String text, boolean primary) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false); 

        if (primary) {
            btn.setBackground(COLOR_ACCENT);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(COLOR_TEXT_HEADER);
            btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(10, 20, 10, 20)
            ));
            btn.setBorderPainted(true);
        }
        return btn;
    }

    @Override public String getViewId() { return "dashboard-view"; }
    @Override public String getViewTabTitle() { return "Dashboard"; }
    @Override public JPanel getContent() { return this; }
    @Override public boolean equals(View v) { return v != null && v.getViewId().equals(getViewId()); }

    @Override
    public void updateSelf() {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Willkommen zurück, " + ServiceLocator.getSessionManager().getUserFirstNameAndLastName());
        }
        loadDashboardData();
    }
}