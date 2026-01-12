package gui.components;

import core.ServiceLocator;
import model.Skill;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Eine benutzerdefinierte Komponente, die eine scrollbare Liste von SkillToggleItems anzeigt.
 * Sie ersetzt die JList und bietet eine einfachere Methode zur Mehrfachauswahl.
 */
public class SkillSelectionPanel extends JPanel {

    private final List<SkillToggleItem> skillItems = new ArrayList<>();

    public SkillSelectionPanel() {
        super(new BorderLayout());

        // Ein Panel, das alle SkillToggleItems vertikal anordnet
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        // Daten laden und die UI befüllen
        populateSkills(listPanel);

        // Das Panel in eine JScrollPane packen, um Scrollen zu ermöglichen
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Scroll-Geschwindigkeit verbessern

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Holt alle Skills vom ServiceLocator und erstellt für jeden ein SkillToggleItem.
     */
    private void populateSkills(JPanel panel) {
        List<Skill> allSkills = ServiceLocator.getSkillContainer().getSkills();
        for (Skill skill : allSkills) {
            SkillToggleItem item = new SkillToggleItem(skill);
            skillItems.add(item); // Zur internen Liste für die Logik hinzufügen
            panel.add(item);      // Zum visuellen Panel hinzufügen
        }
    }

    /**
     * Die öffentliche Schnittstelle, um alle vom Benutzer ausgewählten Skills abzurufen.
     * @return Eine Liste der ausgewählten Skill-Objekte.
     */
    public List<Skill> getSelectedSkills() {
        return skillItems.stream()
                .filter(SkillToggleItem::isSelected)
                .map(SkillToggleItem::getSkill)
                .collect(Collectors.toList());
    }
}