package gui.views;

import database.DatabaseManager;
import model.*; // Importiert Employee, SkillManager, TrainingManager, Role etc.
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Date;
import java.util.ArrayList;
import java.time.LocalDate; // Wichtig für Datum

public class EmployeeManagementView extends JPanel implements View {

    private JList<String> employeeList;
    private DefaultListModel<String> listModel;
    private ArrayList<Employee> currentListCache;

    // Eingabefelder
    private JTextField txtFirstName, txtLastName, txtUsername, txtPassword;

    public EmployeeManagementView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- LINKES PANEL: Liste & Löschen ---
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(new TitledBorder("Mitarbeiter verwalten"));

        listModel = new DefaultListModel<>();
        employeeList = new JList<>(listModel);
        leftPanel.add(new JScrollPane(employeeList), BorderLayout.CENTER);

        JButton btnDelete = new JButton("Ausgewählten Mitarbeiter löschen");
        btnDelete.setBackground(new Color(255, 100, 100));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(e -> deleteSelectedEmployee());
        leftPanel.add(btnDelete, BorderLayout.SOUTH);

        // --- RECHTES PANEL: Hinzufügen ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(new TitledBorder("Neuen Mitarbeiter anlegen"));

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));

        txtFirstName = new JTextField();
        txtLastName = new JTextField();
        txtUsername = new JTextField();
        txtPassword = new JTextField();

        formPanel.add(new JLabel("Vorname:")); formPanel.add(txtFirstName);
        formPanel.add(new JLabel("Nachname:")); formPanel.add(txtLastName);
        formPanel.add(new JLabel("Benutzername:")); formPanel.add(txtUsername);
        formPanel.add(new JLabel("Passwort:")); formPanel.add(txtPassword);

        JButton btnAdd = new JButton("Mitarbeiter hinzufügen");
        btnAdd.addActionListener(e -> createEmployee());

        rightPanel.add(formPanel, BorderLayout.CENTER);
        rightPanel.add(btnAdd, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(300);
        add(splitPane, BorderLayout.CENTER);

        refreshList();
    }

    private void refreshList() {
        listModel.clear();
        currentListCache = EmployeeContainer.getInstance().getEmployees();

        for (Employee e : currentListCache) {
            listModel.addElement(e.getId() + " | " + e.getFirstName() + " " + e.getLastName() + " (" + e.getUsername() + ")");
        }
    }

    private void deleteSelectedEmployee() {
        int index = employeeList.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Bitte wählen Sie einen Mitarbeiter aus.");
            return;
        }

        Employee toDelete = currentListCache.get(index);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Sollen " + toDelete.getFirstName() + " " + toDelete.getLastName() + " wirklich gelöscht werden?",
                "Löschen bestätigen", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            EmployeeContainer.getInstance().removeEmployee(toDelete);
            DatabaseManager.getInstance().deleteEmployee(toDelete.getId());
            refreshList();
            JOptionPane.showMessageDialog(this, "Mitarbeiter gelöscht!");
        }
    }

    private void createEmployee() {
        if (txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Benutzername und Passwort sind Pflicht!");
            return;
        }

        // 1. ID generieren
        int newId = 1;
        for (Employee e : EmployeeContainer.getInstance().getEmployees()) {
            if (e.getId() >= newId) newId = e.getId() + 1;
        }

        try {
            // 2. Manager initialisieren (WICHTIG!)
            SkillManager newSkillManager = new SkillManager();
            TrainingManager newTrainingManager = new TrainingManager();

            // 3. Employee erstellen
            Employee newEmp = new Employee(
                    newId,
                    0, // Team ID 0
                    txtUsername.getText().trim(),
                    txtPassword.getText().trim(),
                    txtFirstName.getText().trim(),
                    txtLastName.getText().trim(),
                    "email@placeholder.com",
                    new Date(),
                    "Unbekannt",
                    'X',
                    new Date(),
                    1,
                    true,
                    "0000",
                    newSkillManager,    // Hier übergeben wir jetzt die leeren Manager
                    newTrainingManager
            );


            if (!RoleContainer.getInstance().getRoles().isEmpty()) {
                Role defaultRole = RoleContainer.getInstance().getRoles().get(0);
                newEmp.getRoleManager().assignRole(defaultRole.getId(), LocalDate.now());
            }

            // 5. Speichern
            EmployeeContainer.getInstance().addEmployee(newEmp);
            DatabaseManager.getInstance().addEmployee(newEmp);

            refreshList();

            // Felder leeren
            txtFirstName.setText("");
            txtLastName.setText("");
            txtUsername.setText("");
            txtPassword.setText("");

            JOptionPane.showMessageDialog(this, "Mitarbeiter angelegt (ID: " + newId + ")");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Fehler beim Erstellen: " + ex.getMessage());
        }
    }

    @Override
    public String getViewId() { return "admin-employee-management"; }

    @Override
    public String getViewTabTitle() { return "Personalverwaltung"; }

    @Override
    public JPanel getContent() { return this; }

    @Override
    public boolean equals(View view) {
        return view != null && view.getViewId().equals(getViewId());
    }
}