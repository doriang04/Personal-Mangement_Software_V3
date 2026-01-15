package gui.views;

import core.ServiceLocator;
import gui.UIController;
import model.Skill;
import model.SkillManager;
import model.Training;
import model.TrainingManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class DashboardView extends JPanel implements View {
    private JButton continueButton;
    private JPanel trainingPanel;
    private JPanel skillsPanel;
    private JLabel welcomeLabel; // Made into a field to allow updating

    public DashboardView() {
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        // Header (top)
        add(createHeader(), BorderLayout.NORTH);
        // Content (center) with the tiles
        add(createContentPanel(), BorderLayout.CENTER);
        // Load data
        loadDashboardData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);

        // Welcome text
        welcomeLabel = new JLabel("Willkommen zurück, " + ServiceLocator.getSessionManager().getUserFirstNameAndLastName());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Manager text (smaller) - this part is static and doesn't need to be a field
        JLabel managerLabel = new JLabel("Manager");
        managerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        managerLabel.setForeground(Color.GRAY);
        managerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(welcomeLabel);
        header.add(Box.createRigidArea(new Dimension(0, 5)));
        header.add(managerLabel);

        return header;
    }

    private JPanel createContentPanel() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);

        // Training tile
        trainingPanel = createTrainingCard();
        trainingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Skills tile
        skillsPanel = createSkillsCard();
        skillsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(Box.createRigidArea(new Dimension(0, 20)));
        content.add(trainingPanel);
        content.add(Box.createRigidArea(new Dimension(0, 20)));
        content.add(skillsPanel);

        return content;
    }

    private JPanel createTrainingCard() {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(15, 15));
        card.setBackground(new Color(248, 249, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        // Title
        JLabel titleLabel = new JLabel("Offenes Training");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        card.add(titleLabel, BorderLayout.NORTH);

        // Content (will be filled later)
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(248, 249, 250));
        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }


    private JPanel createSkillsCard() {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(15, 15));
        card.setBackground(new Color(248, 249, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        // Title
        JLabel titleLabel = new JLabel("Inaktive Skills");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        card.add(titleLabel, BorderLayout.NORTH);

        // Skills list
        JPanel skillsList = new JPanel();
        skillsList.setLayout(new BoxLayout(skillsList, BoxLayout.Y_AXIS));
        skillsList.setBackground(new Color(248, 249, 250));
        card.add(skillsList, BorderLayout.CENTER);

        return card;
    }

    private void loadDashboardData() {
        // Load open training
        boolean hasUpdatedTrainingCard = false;
        if (ServiceLocator.getSessionManager().getCurrentUser() != null && ServiceLocator.getSessionManager().getCurrentUser().getTrainingManager() != null) {
            ArrayList<TrainingManager.TrainingHistoryEntry> allTrainings = ServiceLocator.getSessionManager().getCurrentUser().getTrainingManager().getTrainingHistory();
            for (TrainingManager.TrainingHistoryEntry entry : allTrainings) {
                if (entry.getStatus() == TrainingManager.Status.OPEN) {
                    Training training = ServiceLocator.getTrainingContainer().getTrainingById(entry.getTrainingId());
                    updateTrainingCard(training);
                    hasUpdatedTrainingCard = true;
                    break; // Exit after finding the first open training
                }
            }
        }
        if (!hasUpdatedTrainingCard) {
            updateTrainingCard(null);
        }

        // Load inactive skills
        if (ServiceLocator.getSessionManager().getCurrentUser() != null && ServiceLocator.getSessionManager().getCurrentUser().getSkillManager() != null) {
            ArrayList<SkillManager.SkillHistoryEntry> inactiveSkills = ServiceLocator.getSessionManager().getCurrentUser().getSkillManager().getInactiveSkills();
            updateSkillsCard(inactiveSkills);
        } else {
            updateSkillsCard(new ArrayList<>()); // Pass an empty list if no user/manager
        }
    }
    private void updateTrainingCard(Training training) {
        // Get content panel from the card
        JPanel contentPanel = (JPanel) ((BorderLayout) trainingPanel.getLayout())
                .getLayoutComponent(BorderLayout.CENTER);
        contentPanel.removeAll();

        if (training != null) {
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(new Color(248, 249, 250));

            JLabel trainingName = new JLabel(training.getTitle());
            trainingName.setFont(new Font("Arial", Font.PLAIN, 14));
            mainPanel.add(trainingName, BorderLayout.WEST);

            continueButton = new JButton("Meine Schulungen");
            continueButton.setFont(new Font("Arial", Font.BOLD, 14));
            continueButton.setBackground(new Color(0, 123, 255));
            continueButton.setForeground(Color.WHITE);
            continueButton.setFocusPainted(false);
            continueButton.setOpaque(true);
            continueButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            continueButton.addActionListener(e -> continueTraining(training));
            mainPanel.add(continueButton, BorderLayout.EAST);

            contentPanel.add(mainPanel, BorderLayout.CENTER);
        } else {
            JLabel noTraining = new JLabel("Keine offenen Schulungen. Sehr gut!");
            noTraining.setFont(new Font("Arial", Font.PLAIN, 14));
            noTraining.setForeground(Color.GRAY);
            contentPanel.add(noTraining, BorderLayout.CENTER);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void updateSkillsCard(ArrayList<SkillManager.SkillHistoryEntry> skills) {
        // Get skills list from the card
        JPanel skillsList = (JPanel) ((BorderLayout) skillsPanel.getLayout())
                .getLayoutComponent(BorderLayout.CENTER);
        skillsList.removeAll();

        if (skills != null && !skills.isEmpty()) {
            for (SkillManager.SkillHistoryEntry skillEntry: skills) {
                Skill skill = ServiceLocator.getSkillContainer().getSkillById(skillEntry.getSkillId());
                if (skill == null) continue; // Skip if skill not found

                JPanel skillItem = new JPanel(new BorderLayout());
                skillItem.setBackground(new Color(248, 249, 250));
                skillItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

                JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                contentPanel.setBackground(new Color(248, 249, 250));

                JLabel bullet = new JLabel("•");
                bullet.setFont(new Font("Arial", Font.PLAIN, 16));
                bullet.setForeground(new Color(0, 123, 255));

                JLabel skillName = new JLabel(skill.getName());
                skillName.setFont(new Font("Arial", Font.PLAIN, 14));

                contentPanel.add(bullet);
                contentPanel.add(skillName);
                skillItem.add(contentPanel, BorderLayout.WEST);

                skillsList.add(skillItem);
                skillsList.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        } else {
            JLabel noSkills = new JLabel("Keine inaktiven Skills vorhanden.");
            noSkills.setFont(new Font("Arial", Font.PLAIN, 14));
            noSkills.setForeground(Color.GRAY);
            skillsList.add(noSkills);
        }

        skillsList.revalidate();
        skillsList.repaint();
    }

    /**
     * This method handles navigation. It does not modify any core data.
     * Therefore, a call to UIController.updateMainWindow() is not needed here.
     * The responsibility for data modification lies within the `MyTrainingsView` it opens.
     */
    private void continueTraining(Training training) {
        UIController.getInstance().openTabOrFocus(new MyTrainingsView(), true);
    }

    @Override
    public String getViewId() {
        return "dashboard-view";
    }

    @Override
    public String getViewTabTitle() {
        return "Dashboard";
    }

    @Override
    public JPanel getContent() {
        return this;
    }

    @Override
    public boolean equals(View view) {
        // Added null check for safety
        return view != null && view.getViewId().equals(this.getViewId());
    }

    /**
     * Refreshes all dynamic content on the dashboard.
     * This method is CALLED BY the global UI update mechanism; it should not trigger one itself.
     */
    @Override
    public void updateSelf() {
        // Update the welcome message in case the user's name has changed in the session.
        if (welcomeLabel != null) {
            welcomeLabel.setText("Willkommen zurück, " + ServiceLocator.getSessionManager().getUserFirstNameAndLastName());
        }

        // The existing loadDashboardData() method already contains all the logic
        // to fetch fresh data and update the UI cards.
        loadDashboardData();
    }
}