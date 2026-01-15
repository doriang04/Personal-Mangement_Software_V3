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
    public DashboardView() {
        /*setLayout(new GridBagLayout());
        add(new JLabel("Willkommen auf dem Dashboard!"));
        add(new JLabel(ServiceLocator.getSessionManager().getUserFirstNameAndLastName()));*/
        //ab hier alles von tim
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        // Header (oben)
        add(createHeader(), BorderLayout.NORTH);
        // Content (Mitte) mit den Kacheln
        add(createContentPanel(), BorderLayout.CENTER);
        // Daten laden
        loadDashboardData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);

        // Willkommenstext
        JLabel welcomeLabel = new JLabel("Willkommen zurück, " + ServiceLocator.getSessionManager().getUserFirstNameAndLastName());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Manager Text (kleiner)
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

        // Training Kachel
        trainingPanel = createTrainingCard();
        trainingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Skills Kachel
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

        // Titel
        JLabel titleLabel = new JLabel("Offenes Training");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        card.add(titleLabel, BorderLayout.NORTH);

        // Content (wird später gefüllt)
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

        // Titel
        JLabel titleLabel = new JLabel("Inaktive Skills");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        card.add(titleLabel, BorderLayout.NORTH);

        // Skills Liste
        JPanel skillsList = new JPanel();
        skillsList.setLayout(new BoxLayout(skillsList, BoxLayout.Y_AXIS));
        skillsList.setBackground(new Color(248, 249, 250));
        card.add(skillsList, BorderLayout.CENTER);

        return card;
    }

    private void loadDashboardData() {
        // Offenes Training laden
        boolean has_updated_training_card = false;
        ArrayList<model.TrainingManager.TrainingHistoryEntry> all_trainings = ServiceLocator.getSessionManager().getCurrentUser().getTrainingManager().getTrainingHistory();
        for (model.TrainingManager.TrainingHistoryEntry entry : all_trainings) {
            if (entry.getStatus() == TrainingManager.Status.OPEN) {
                int tid = entry.getTrainingId();
                Training training = ServiceLocator.getTrainingContainer().getTrainingById(tid);
                updateTrainingCard(training);
                has_updated_training_card = true;
                break; // Exit after finding the first open training
            }
        }
        if (!has_updated_training_card) {
            updateTrainingCard(null);   }

        // INaktive Skills laden
        ArrayList<SkillManager.SkillHistoryEntry> recentSkills = ServiceLocator.getSessionManager().getCurrentUser().getSkillManager().getInactiveSkills();     //getSkillService().getRecentSkills(5);
        updateSkillsCard(recentSkills);
    }
    private void updateTrainingCard(Training training) {
        // Content Panel aus der Kachel holen
        JPanel contentPanel = (JPanel) ((BorderLayout) trainingPanel.getLayout())
                .getLayoutComponent(BorderLayout.CENTER);
        contentPanel.removeAll();

        if (training != null) {
            // Haupt-Container für horizontale Anordnung
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(new Color(248, 249, 250));

            // Training Name links
            JLabel trainingName = new JLabel(training.getTitle());
            trainingName.setFont(new Font("Arial", Font.PLAIN, 14));
            mainPanel.add(trainingName, BorderLayout.WEST);

            // Fortfahren Button rechts
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
            // Kein Training offen
            JLabel noTraining = new JLabel("Keine offenen Schulungen. Sehr gut!");
            noTraining.setFont(new Font("Arial", Font.PLAIN, 14));
            noTraining.setForeground(Color.GRAY);
            contentPanel.add(noTraining, BorderLayout.CENTER);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void updateSkillsCard(ArrayList<SkillManager.SkillHistoryEntry> skills) {
        // Skills Liste aus der Kachel holen
        JPanel skillsList = (JPanel) ((BorderLayout) skillsPanel.getLayout())
                .getLayoutComponent(BorderLayout.CENTER);
        skillsList.removeAll();

        if (skills != null && !skills.isEmpty()) {
            for (SkillManager.SkillHistoryEntry skill: skills) {
                JPanel skillItem = new JPanel(new BorderLayout());
                skillItem.setBackground(new Color(248, 249, 250));
                skillItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30)); // Fixe Höhe

                // Bullet und Name in einem Panel
                JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                contentPanel.setBackground(new Color(248, 249, 250));

                JLabel bullet = new JLabel("•");
                bullet.setFont(new Font("Arial", Font.PLAIN, 16));
                bullet.setForeground(new Color(0, 123, 255));

                JLabel skillName = new JLabel(ServiceLocator.getSkillContainer().getSkillById(skill.getSkillId()).getName());
                skillName.setFont(new Font("Arial", Font.PLAIN, 14));

                contentPanel.add(bullet);
                contentPanel.add(skillName);
                skillItem.add(contentPanel, BorderLayout.WEST);

                skillsList.add(skillItem);
                skillsList.add(Box.createRigidArea(new Dimension(0, 8))); // Fixer Abstand zwischen Items
            }
        } else {
            JLabel noSkills = new JLabel("Noch keine Skills erworben");
            noSkills.setFont(new Font("Arial", Font.PLAIN, 14));
            noSkills.setForeground(Color.GRAY);
            skillsList.add(noSkills);
        }

        skillsList.revalidate();
        skillsList.repaint();
    }

    private void continueTraining(Training training) {
        // Navigation zum Training
        // z.B.: ServiceLocator.getNavigationService().navigateToTraining(training);
        UIController.getInstance().openTabOrFocus(new MyTrainingsView(), true);
        System.out.println("Navigiere zu Training: " + training.getTitle());
    }







    //bis hier alles tims schuld

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
        return view.getViewId().equals(this.getViewId());
    }
}
