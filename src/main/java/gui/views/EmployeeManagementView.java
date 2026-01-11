package gui.views;

import model.Employee;
import model.EmployeeContainer;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Date;
import java.util.ArrayList;
import database.DatabaseManager;

public class EmployeeManagementView extends JPanel implements View {

    private JList<String> employeeList;
    private DefaultListModel<String> listModel;
    private ArrayList<Employee> currentListCache; // Hilft uns, das Objekt zur Zeile zu finden

    // Eingabefelder für neuen Mitarbeiter
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
        btnDelete.setBackground(new Color(255, 100, 100)); // Rötlich
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
        txtPassword = new JTextField(); // Einfachheitshalber als Textfeld, besser JPasswordField

        formPanel.add(new JLabel("Vorname:")); formPanel.add(txtFirstName);
        formPanel.add(new JLabel("Nachname:")); formPanel.add(txtLastName);
        formPanel.add(new JLabel("Benutzername:")); formPanel.add(txtUsername);
        formPanel.add(new JLabel("Passwort:")); formPanel.add(txtPassword);

        JButton btnAdd = new JButton("Mitarbeiter hinzufügen");
        btnAdd.addActionListener(e -> createEmployee());

        rightPanel.add(formPanel, BorderLayout.CENTER);
        rightPanel.add(btnAdd, BorderLayout.SOUTH);

        // Split Pane für Layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(300);
        add(splitPane, BorderLayout.CENTER);

        // Daten initial laden
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
        // Validierung
        if (txtUsername.getText().isEmpty() || txtPassword.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Benutzername und Passwort sind Pflicht!");
            return;
        }

        // 1. Neue ID generieren (Max ID + 1)
        int newId = 1;
        for (Employee e : EmployeeContainer.getInstance().getEmployees()) {
            if (e.getId() >= newId) newId = e.getId() + 1;
        }

        // 2. Dummy-Daten für Felder, die wir im Schnell-Formular nicht abfragen
        // (Damit der riesige Employee Konstruktor zufrieden ist)
        try {
            Employee newEmp = new Employee(
                    newId,
                    0, // No Team yet
                    txtUsername.getText(),
                    txtPassword.getText(),
                    txtFirstName.getText(),
                    txtLastName.getText(),
                    "email@placeholder.com", // Dummy Email
                    new Date(), // Dummy Geburtsdatum
                    "Unbekannt", // Dummy Adresse
                    'X', // Dummy Geschlecht
                    new Date(), // Hire Date heute
                    1, // Manager ID default
                    true, // Active
                    "0000", // Phone
                    null, // SkillManager
                    null  // TrainingManager
            );

            // 3. Hinzufügen
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
