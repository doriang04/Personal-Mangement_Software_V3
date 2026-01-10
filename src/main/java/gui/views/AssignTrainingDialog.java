package gui.views;

import model.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AssignTrainingDialog extends JDialog {

    private JComboBox<EmployeeItem> cbEmployees;
    private JComboBox<TrainingItem> cbTrainings;
    private JTextField txtDate;
    private JButton btnSave, btnCancel;
    private final Runnable onSuccessCallback;

    public AssignTrainingDialog(Window owner, Runnable onSuccessCallback) {
        super(owner, "Schulung zuweisen", ModalityType.APPLICATION_MODAL);
        this.onSuccessCallback = onSuccessCallback;
        setSize(400, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        initUI();
        loadData();
    }

    private void initUI() {
        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        cbEmployees = new JComboBox<>();
        cbTrainings = new JComboBox<>();

        // Datum vorbelegen (Heute)
        txtDate = new JTextField(LocalDate.now().toString());

        form.add(new JLabel("Mitarbeiter:"));
        form.add(cbEmployees);
        form.add(new JLabel("Schulung:"));
        form.add(cbTrainings);
        form.add(new JLabel("Startdatum (YYYY-MM-DD):"));
        form.add(txtDate);

        add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        btnSave = new JButton("Speichern");
        btnCancel = new JButton("Abbrechen");

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        // Mitarbeiter laden
        for(Employee e : ServiceLocator.getEmployeeContainer().getEmployees()) {
            cbEmployees.addItem(new EmployeeItem(e));
        }
        // Schulungen laden
        for(Training t : ServiceLocator.getTrainingContainer().getTrainings()) {
            cbTrainings.addItem(new TrainingItem(t));
        }
    }

    private void onSave() {
        try {
            EmployeeItem empItem = (EmployeeItem) cbEmployees.getSelectedItem();
            TrainingItem trainItem = (TrainingItem) cbTrainings.getSelectedItem();
            String dateStr = txtDate.getText().trim();

            if (empItem == null || trainItem == null) {
                JOptionPane.showMessageDialog(this, "Bitte alles auswählen.");
                return;
            }

            // Datum parsen
            LocalDate date = LocalDate.parse(dateStr);

            // ---------------------------------------------------------
            // ÄNDERUNG: KEIN Datenbank-Aufruf mehr!
            // Wir arbeiten nur mit den Objekten im Speicher.
            // ---------------------------------------------------------

            TrainingManager tm = empItem.e.getOpenTrainingManager();
            if (tm != null) {
                // Hier wird das Training dem Objekt hinzugefügt
                tm.assignTraining(trainItem.t.getId(), date);

                System.out.println("DEBUG: Schulung '" + trainItem.t.getTitle() +
                        "' an " + empItem.e.getUsername() + " zugewiesen (Nur RAM).");
            } else {
                JOptionPane.showMessageDialog(this, "Fehler: Mitarbeiter hat keinen TrainingManager!");
                return;
            }

            // Erfolg
            JOptionPane.showMessageDialog(this, "Zugewiesen (Nur temporär im Speicher)!");

            // Tabelle aktualisieren
            if (onSuccessCallback != null) onSuccessCallback.run();

            dispose();

        } catch (java.time.format.DateTimeParseException dtpe) {
            JOptionPane.showMessageDialog(this, "Falsches Datumsformat! Bitte YYYY-MM-DD nutzen.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Fehler: " + ex.getMessage());
        }
    }

    // Hilfsklassen für ComboBox Anzeige
    class EmployeeItem {
        Employee e;
        EmployeeItem(Employee e) { this.e = e; }
        public String toString() { return e.getFirstName() + " " + e.getLastName(); }
    }
    class TrainingItem {
        Training t;
        TrainingItem(Training t) { this.t = t; }
        public String toString() { return t.getTitle(); }
    }
}