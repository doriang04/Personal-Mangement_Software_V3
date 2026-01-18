package gui.components;

import model.Skill;

import javax.swing.*;
import java.awt.*;

public class SkillToggleItem extends JPanel {

    private final Skill skill;
    private final JToggleButton toggleButton;

    public SkillToggleItem(Skill skill) {
        this.skill = skill;

        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JLabel skillNameLabel = new JLabel(skill.getName());
        add(skillNameLabel, BorderLayout.CENTER);

        toggleButton = new JToggleButton("Wählen");
        toggleButton.setFocusable(false);

        toggleButton.addActionListener(e -> {
            if (toggleButton.isSelected()) {
                toggleButton.setText("Ausgewählt");
                toggleButton.setBackground(new Color(130, 190, 130));
            } else {
                toggleButton.setText("Wählen");
                toggleButton.setBackground(UIManager.getColor("Button.background"));
            }
        });
        add(toggleButton, BorderLayout.EAST);
    }

    public boolean isSelected() {
        return toggleButton.isSelected();
    }

    public Skill getSkill() {
        return skill;
    }
}