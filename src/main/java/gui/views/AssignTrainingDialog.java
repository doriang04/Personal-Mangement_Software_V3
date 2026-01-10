package gui.views;

import model.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class AssignTrainingDialog extends JDialog {

    private JComboBox<EmployeeItem> cbEmployees;
    private JComboBox<TrainingItem> cbTrainings;
    private JTextField txtDate;
    private JButton btnSave, btnCancel;
    private final Runnable onSuccessCallback;

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
        for(Employee e : ServiceLocator.getEmployeeContainer().getEmployees()) cbEmployees.addItem(new EmployeeItem(e));
        for(Training t : ServiceLocator.getTrainingContainer().getTrainings()) cbTrainings.addItem(new TrainingItem(t));
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

            LocalDate date = LocalDate.parse(dateStr);

            TrainingManager tm = empItem.e.getTrainingManager();
            if (tm != null) {
                tm.assignTraining(trainItem.t.getId(), date);

                System.out.println("DEBUG: Schulung '" + trainItem.t.getTitle() +
                        "' an " + empItem.e.getUsername() + " zugewiesen.");
            } else {
                JOptionPane.showMessageDialog(this, "Fehler: Mitarbeiter hat keinen TrainingManager!");
                return;
            }

            JOptionPane.showMessageDialog(this, "Zugewiesen!");
            if (onSuccessCallback != null) onSuccessCallback.run();
            dispose();

        } catch (java.time.format.DateTimeParseException dtpe) {
            JOptionPane.showMessageDialog(this, "Falsches Datumsformat! Bitte YYYY-MM-DD nutzen.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Fehler: " + e.getMessage());
        }
    }
}