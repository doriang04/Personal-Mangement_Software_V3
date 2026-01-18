package gui.components;

import core.ServiceLocator;
import model.Skill;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SkillSelectionPanel extends JPanel {

    private final List<SkillToggleItem> skillItems = new ArrayList<>();

    public SkillSelectionPanel() {
        super(new BorderLayout());

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        populateSkills(listPanel);

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void populateSkills(JPanel panel) {
        for (Skill skill : ServiceLocator.getSkillContainer().getSkills()) {
            SkillToggleItem item = new SkillToggleItem(skill);
            skillItems.add(item);
            panel.add(item);
        }
    }

    public List<Skill> getSelectedSkills() {
        return skillItems.stream()
                .filter(SkillToggleItem::isSelected)
                .map(SkillToggleItem::getSkill)
                .collect(Collectors.toList());
    }
}