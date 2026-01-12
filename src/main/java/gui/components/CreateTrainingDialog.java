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
    private JList<Skill> skillList;
    private DefaultListModel<Skill> skillListModel;

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
        spnLength = new JSpinner(new SpinnerNumberModel(8, 1, 999, 1)); // Min 1h, Max 999h

        // Skill-Auswahl
        skillListModel = new DefaultListModel<>();
        skillList = new JList<>(skillListModel);
        loadAllSkills(); // Füllt die Liste mit verfügbaren Skills

        int row = 0;
        gbc.anchor = GridBagConstraints.WEST;
        addFormRow(formPanel, gbc, row++, "Titel:", txtTitle);
        gbc.fill = GridBagConstraints.BOTH; // Beschreibung darf wachsen
        gbc.weighty = 1.0;
        addFormRow(formPanel, gbc, row++, "Beschreibung:", new JScrollPane(txtDescription));
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        addFormRow(formPanel, gbc, row++, "Dauer (Stunden):", spnLength);
        gbc.fill = GridBagConstraints.BOTH; // Skill-Liste darf wachsen
        gbc.weighty = 1.0;
        addFormRow(formPanel, gbc, row++, "Benötigte Skills:", new JScrollPane(skillList));

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

        pack();
        setLocationRelativeTo(owner);
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        p.add(comp, gbc);
    }

    private void loadAllSkills() {
        // Annahme: Es gibt einen SkillContainer im ServiceLocator
        List<Skill> allSkills = ServiceLocator.getSkillContainer().getSkills();
        for (Skill s : allSkills) {
            skillListModel.addElement(s);
        }
    }

    private void saveTraining(Runnable onSave) {
        String title = txtTitle.getText().trim();
        String description = txtDescription.getText().trim();
        int length = (int) spnLength.getValue();
        List<Skill> selectedSkills = skillList.getSelectedValuesList();

        // Validierung
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Der Titel darf nicht leer sein.", "Validierungsfehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // 1. Eindeutige ID generieren (einfache Methode)
            int newId = ServiceLocator.getTrainingContainer().getTrainings().stream()
                    .mapToInt(Training::getId)
                    .max()
                    .orElse(0) + 1;

            // 2. Neues Training-Objekt erstellen
            Training newTraining = new Training();
            newTraining.setId(newId);
            newTraining.setTitle(title);
            newTraining.setDescription(description);
            newTraining.setLength(length);

            // 3. Den zugehörigen SkillManager erstellen und befüllen
            TrainingSkillManager skillManager = new TrainingSkillManager(newTraining);
            for(Skill skill : selectedSkills) {
                skillManager.addSkill(skill);
            }

            // 4. Den SkillManager mit dem Training verknüpfen
            newTraining.setSkillList(skillManager);

            // 5. Training und SkillManager in ihren Containern speichern
            ServiceLocator.getTrainingContainer().addTraining(newTraining);
            ServiceLocator.getTrainingSkillManagerContainer().addTrainingSkillManager(skillManager);

            // 6. Erfolgsmeldung und Dialog schließen
            JOptionPane.showMessageDialog(this, "Training erfolgreich erstellt!");

            // 7. Callback ausführen, um die Tabelle in der Hauptansicht zu aktualisieren
            onSave.run();

            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Fehler beim Speichern: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}