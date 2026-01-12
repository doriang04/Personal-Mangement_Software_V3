package gui.components;

import model.Skill;

import javax.swing.*;
import java.awt.*;

/**
 * Repräsentiert eine einzelne, auswählbare Zeile für einen Skill in der SkillSelectionPanel.
 * Besteht aus einem Label für den Namen und einem Toggle-Button zum Auswählen.
 */
public class SkillToggleItem extends JPanel {

    private final Skill skill;
    private final JToggleButton toggleButton;

    public SkillToggleItem(Skill skill) {
        this.skill = skill;

        // Layout und Design
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Skill-Name
        JLabel skillNameLabel = new JLabel(skill.getName());
        add(skillNameLabel, BorderLayout.CENTER);

        // Toggle-Button
        toggleButton = new JToggleButton("Wählen");
        toggleButton.setFocusable(false); // Verhindert unschöne Fokus-Rahmen

        // Ändert Text und Farbe des Buttons je nach Zustand (ausgewählt/nicht ausgewählt)
        toggleButton.addActionListener(e -> {
            if (toggleButton.isSelected()) {
                toggleButton.setText("Ausgewählt");
                toggleButton.setBackground(new Color(130, 190, 130)); // Grünton
            } else {
                toggleButton.setText("Wählen");
                toggleButton.setBackground(UIManager.getColor("Button.background")); // Standardfarbe
            }
        });
        add(toggleButton, BorderLayout.EAST);
    }

    /**
     * Gibt zurück, ob dieses Element vom Benutzer ausgewählt wurde.
     * @return true, wenn der Toggle-Button aktiviert ist, sonst false.
     */
    public boolean isSelected() {
        return toggleButton.isSelected();
    }

    /**
     * Gibt das mit diesem Item verknüpfte Skill-Objekt zurück.
     * @return Das Skill-Objekt.
     */
    public Skill getSkill() {
        return skill;
    }
}