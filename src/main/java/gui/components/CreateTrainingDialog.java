package gui.components;

import core.ServiceLocator;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CreateTrainingDialog extends JDialog {

    // UI-Komponenten
    private JTextField txtTitle;
    private JTextArea txtDescription;
    private JSpinner spnLength;

    // Ersetzt JList, Model, Listener und Anzeigebereich durch eine einzige Komponente
    private SkillSelectionPanel skillSelectionPanel;

    public CreateTrainingDialog(Window owner, Runnable onSave) {
        super(owner, "Neues Training erstellen", ModalityType.APPLICATION_MODAL);

        setLayout(new BorderLayout(10, 10));

        // --- Eingabefelder ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtTitle = new JTextField(30);
        txtDescription = new JTextArea(4, 30);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        spnLength = new JSpinner(new SpinnerNumberModel(8, 1, 999, 1));

        // Initialisierung der neuen, benutzerdefinierten Komponente.
        // Die gesamte Logik für die Anzeige und Auswahl ist jetzt darin gekapselt.
        skillSelectionPanel = new SkillSelectionPanel();

        // --- Layout Aufbau ---
        int row = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST; // Labels oben ausrichten

        addFormRow(formPanel, gbc, row++, "Titel:", txtTitle);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.5; // Beschreibung bekommt etwas Platz zum Wachsen
        addFormRow(formPanel, gbc, row++, "Beschreibung:", new JScrollPane(txtDescription));

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0; // Spinner braucht nicht zu wachsen
        addFormRow(formPanel, gbc, row++, "Dauer (Stunden):", spnLength);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0; // Skill-Panel bekommt den meisten vertikalen Platz
        addFormRow(formPanel, gbc, row++, "Benötigte Skills:", skillSelectionPanel);

        // --- Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Speichern");
        JButton btnCancel = new JButton("Abbrechen");
        btnSave.addActionListener(e -> saveTraining(onSave));
        btnCancel.addActionListener(e -> dispose());
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Empfohlene Größe setzen
        setSize(new Dimension(500, 550));
        setLocationRelativeTo(owner);
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        p.add(comp, gbc);
    }

    private void saveTraining(Runnable onSave) {
        String title = txtTitle.getText().trim();
        String description = txtDescription.getText().trim();
        int length = (int) spnLength.getValue();

        // Die ausgewählten Skills werden jetzt sauber über die Schnittstelle des neuen Panels geholt.
        List<Skill> selectedSkills = skillSelectionPanel.getSelectedSkills();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Der Titel darf nicht leer sein.", "Validierungsfehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int newId = ServiceLocator.getTrainingContainer().getTrainings().stream()
                    .mapToInt(Training::getId).max().orElse(0) + 1;

            Training newTraining = new Training();
            newTraining.setId(newId);
            newTraining.setTitle(title);
            newTraining.setDescription(description);
            newTraining.setLength(length);

            TrainingSkillManager skillManager = new TrainingSkillManager(newTraining);
            for(Skill skill : selectedSkills) {
                skillManager.addSkill(skill);
            }

            newTraining.setSkillList(skillManager);
            ServiceLocator.getTrainingContainer().addTraining(newTraining);
            ServiceLocator.getTrainingSkillManagerContainer().addTrainingSkillManager(skillManager);

            JOptionPane.showMessageDialog(this, "Training erfolgreich erstellt!");
            onSave.run();
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler beim Speichern: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}