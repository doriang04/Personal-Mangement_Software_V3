package gui.views;

import core.ServiceLocator;
import model.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ConfigurationView extends JPanel implements View {

    private final String viewId = "configuration-view";
    private final String tabTitle = "Verwaltung";

    public ConfigurationView() {
        setLayout(new BorderLayout());

        // Haupt-Tab-Container
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tabs hinzufügen
        tabbedPane.addTab("Abteilungen", new DepartmentManagementPanel());
        tabbedPane.addTab("Teams", new TeamManagementPanel());
        tabbedPane.addTab("Rollen", new RoleManagementPanel());
        tabbedPane.addTab("Skills", new SkillManagementPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    @Override
    public String getViewId() { return viewId; }

    @Override
    public String getViewTabTitle() { return tabTitle; }

    @Override
    public JPanel getContent() { return this; }

    @Override
    public boolean equals(View view) {
        return view.getViewId().equals(this.getViewId());
    }

    /**
     * Tab 1: Abteilungen (Departments)
     */
    private class DepartmentManagementPanel extends JPanel {
        private DefaultListModel<Department> listModel;
        private JList<Department> list;
        private JTextField txtName;
        private JComboBox<Company> cbCompany; // Angenommen es gibt ein Company Modell

        public DepartmentManagementPanel() {
            setLayout(new GridLayout(1, 2, 10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Linke Seite: Liste
            listModel = new DefaultListModel<>();
            list = new JList<>(listModel);
            // Custom Renderer damit nur der Name angezeigt wird
            list.setCellRenderer((ctx, value, index, isSelected, cellHasFocus) -> {
                JLabel lbl = new JLabel(value.getName());
                lbl.setOpaque(true);
                lbl.setBackground(isSelected ? Color.LIGHT_GRAY : Color.WHITE);
                return lbl;
            });

            JPanel listPanel = new JPanel(new BorderLayout());
            listPanel.setBorder(new TitledBorder("Vorhandene Abteilungen"));
            listPanel.add(new JScrollPane(list), BorderLayout.CENTER);
            JButton btnDelete = new JButton("Ausgewählte löschen");
            btnDelete.addActionListener(_ -> {
                try {
                    deleteSelected();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            listPanel.add(btnDelete, BorderLayout.SOUTH);

            // Rechte Seite: Erstellen
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(new TitledBorder("Neue Abteilung erstellen"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            txtName = new JTextField(15);
            cbCompany = new JComboBox<>(); // Muss mit Companies gefüllt werden
            JButton btnAdd = new JButton("Hinzufügen");
            btnAdd.addActionListener(e -> addNew());

            addFormRow(formPanel, gbc, 0, "Name:", txtName);
            addFormRow(formPanel, gbc, 1, "Firma:", cbCompany);
            gbc.gridx = 1; gbc.gridy = 2;
            formPanel.add(btnAdd, gbc);

            add(listPanel);
            add(formPanel);

            loadData();
        }

        private void loadData() {
            listModel.clear();
            for (Department d : ServiceLocator.getDepartmentContainer().getDepartments()) {
                listModel.addElement(d);
            }
            // Fülle ComboBox für Companies
            cbCompany.removeAllItems();
            for (Company c : ServiceLocator.getCompanyContainer().getCompanies()) {
                cbCompany.addItem(c);
            }
        }

        private void addNew() {
            String name = txtName.getText().trim();
            Company selectedCompany = (Company) cbCompany.getSelectedItem();

            // Validierung
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name darf nicht leer sein.");
                return;
            }

            int companyId = (selectedCompany != null) ? selectedCompany.getId() : 0;

            // Modell erstellen
            Department newDept = new Department(name, companyId);

            ServiceLocator.getDepartmentContainer().addDepartment(newDept);

            txtName.setText("");
            loadData();
        }

        private void deleteSelected() throws Exception {
            Department selected = list.getSelectedValue();
            if (selected == null) return;

            boolean isReferenced = ServiceLocator.getTeamContainer().getTeamById(selected.getId()) == null;

            if (isReferenced) {
                JOptionPane.showMessageDialog(this, "Kann nicht gelöscht werden!\nEs existieren noch Teams in dieser Abteilung.", "Fehler", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ServiceLocator.getDepartmentContainer().removeDepartment(selected);
            loadData();
        }
    }

    /**
     * Tab 2: Teams
     */
    private class TeamManagementPanel extends JPanel {
        private DefaultListModel<Team> listModel;
        private JList<Team> list;
        private JTextField txtName;
        private JComboBox<Department> cbDepartment;

        public TeamManagementPanel() {
            setLayout(new GridLayout(1, 2, 10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Liste
            listModel = new DefaultListModel<>();
            list = new JList<>(listModel);
            list.setCellRenderer((ctx, val, idx, sel, foc) -> {
                JLabel lbl = new JLabel(val.getName());
                lbl.setOpaque(true);
                lbl.setBackground(sel ? Color.LIGHT_GRAY : Color.WHITE); return lbl;
            });

            JPanel listPanel = new JPanel(new BorderLayout());
            listPanel.setBorder(new TitledBorder("Vorhandene Teams"));
            listPanel.add(new JScrollPane(list), BorderLayout.CENTER);
            JButton btnDelete = new JButton("Team löschen");
            btnDelete.addActionListener(_ -> {
                try {
                    deleteSelected();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            listPanel.add(btnDelete, BorderLayout.SOUTH);

            // Formular
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(new TitledBorder("Neues Team"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5,5,5,5); gbc.fill = GridBagConstraints.HORIZONTAL;

            txtName = new JTextField(15);
            cbDepartment = new JComboBox<>();
            JButton btnAdd = new JButton("Erstellen");
            btnAdd.addActionListener(e -> addNew());

            addFormRow(formPanel, gbc, 0, "Name:", txtName);
            addFormRow(formPanel, gbc, 1, "Abteilung:", cbDepartment);
            gbc.gridx=1; gbc.gridy=2; formPanel.add(btnAdd, gbc);

            add(listPanel);
            add(formPanel);

            loadData();
        }

        private void loadData() {
            listModel.clear();
            for (Team t : ServiceLocator.getTeamContainer().getTeams()) listModel.addElement(t);

            cbDepartment.removeAllItems();
            for (Department d : ServiceLocator.getDepartmentContainer().getDepartments()) cbDepartment.addItem(d);
        }

        private void addNew() {
            if (txtName.getText().trim().isEmpty() || cbDepartment.getSelectedItem() == null) return;

            Team t = new Team(txtName.getText().trim(), ((Department)cbDepartment.getSelectedItem()).getId());

            ServiceLocator.getTeamContainer().addTeam(t);
            txtName.setText("");
            loadData();
        }

        private void deleteSelected() throws Exception {
            Team selected = list.getSelectedValue();
            if (selected == null) return;

            boolean used = false;
            for (Employee e : ServiceLocator.getEmployeeContainer().getEmployees()) {
                if (e.getTeamId() == selected.getId()) {
                    used = true; break;
                }
            }

            if (used) {
                JOptionPane.showMessageDialog(this, "Team kann nicht gelöscht werden, da Mitarbeiter zugewiesen sind.", "Fehler", JOptionPane.ERROR_MESSAGE);
            } else {
                ServiceLocator.getTeamContainer().removeTeam(selected);
                loadData();
            }
        }
    }

    /**
     * Tab 3: Rollen (Roles)
     */
    private class RoleManagementPanel extends JPanel {
        private DefaultListModel<Role> listModel;
        private JList<Role> list;
        private JTextField txtName, txtPermission;
        private JTextArea txtDesc;

        public RoleManagementPanel() {
            setLayout(new GridLayout(1, 2, 10, 10));
            setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

            // Liste
            listModel = new DefaultListModel<>();
            list = new JList<>(listModel);
            list.setCellRenderer((ctx, val, idx, sel, foc) -> {
                JLabel lbl = new JLabel(val.getName());
                lbl.setOpaque(true); lbl.setBackground(sel ? Color.LIGHT_GRAY : Color.WHITE); return lbl;
            });
            JPanel listPanel = new JPanel(new BorderLayout());
            listPanel.setBorder(new TitledBorder("Rollen"));
            listPanel.add(new JScrollPane(list), BorderLayout.CENTER);
            JButton btnDel = new JButton("Rolle löschen");
            btnDel.addActionListener(_ -> {
                try {
                    deleteSelected();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            listPanel.add(btnDel, BorderLayout.SOUTH);

            // Formular
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(new TitledBorder("Neue Rolle"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5,5,5,5); gbc.fill = GridBagConstraints.HORIZONTAL;

            txtName = new JTextField(15);
            txtPermission = new JTextField(15);
            txtDesc = new JTextArea(3, 15);
            txtDesc.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            JButton btnAdd = new JButton("Speichern");
            btnAdd.addActionListener(e -> addNew());

            addFormRow(formPanel, gbc, 0, "Name:", txtName);
            addFormRow(formPanel, gbc, 1, "Beschreibung:", new JScrollPane(txtDesc));
            addFormRow(formPanel, gbc, 2, "Permission Key:", txtPermission);
            gbc.gridx=1; gbc.gridy=3; formPanel.add(btnAdd, gbc);

            add(listPanel);
            add(formPanel);

            loadData();
        }

        private void loadData() {
            listModel.clear();
            for(Role r : ServiceLocator.getRoleContainer().getRoles()) listModel.addElement(r);
        }

        private void addNew() {
            if(txtName.getText().trim().isEmpty()) return;
            Role r = new Role(txtName.getText().trim(), txtDesc.getText(), txtPermission.getText());

            ServiceLocator.getRoleContainer().addRole(r);
            txtName.setText(""); txtDesc.setText(""); txtPermission.setText("");
            loadData();
        }

        private void deleteSelected() throws Exception {
            Role selected = list.getSelectedValue();
            if (selected == null) return;

            boolean used = false;
            for(Employee e : ServiceLocator.getEmployeeContainer().getEmployees()) {
                try {
                    if (e.getRoleManager().getActiveRole() != null &&
                            e.getRoleManager().getActiveRole().getId() == selected.getId()) {
                        used = true; break;
                    }
                } catch (Exception _) { }
            }

            if(used) {
                JOptionPane.showMessageDialog(this, "Rolle wird noch von Mitarbeitern verwendet!", "Fehler", JOptionPane.ERROR_MESSAGE);
            } else {
                ServiceLocator.getRoleContainer().removeRole(selected);
                loadData();
            }
        }
    }

    /**
     * Tab 4: Skills
     */
    private class SkillManagementPanel extends JPanel {
        private DefaultListModel<Skill> listModel;
        private JList<Skill> list;
        private JTextField txtName;
        private JSpinner spYears;
        private JTextArea txtDesc;

        public SkillManagementPanel() {
            setLayout(new GridLayout(1, 2, 10, 10));
            setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

            // Liste
            listModel = new DefaultListModel<>();
            list = new JList<>(listModel);
            list.setCellRenderer((ctx, val, idx, sel, foc) -> {
                JLabel lbl = new JLabel(val.getName() + " (" + val.getRequired_years() + "y)");
                lbl.setOpaque(true); lbl.setBackground(sel ? Color.LIGHT_GRAY : Color.WHITE); return lbl;
            });
            JPanel listPanel = new JPanel(new BorderLayout());
            listPanel.setBorder(new TitledBorder("Skills"));
            listPanel.add(new JScrollPane(list), BorderLayout.CENTER);
            JButton btnDel = new JButton("Skill löschen");
            btnDel.addActionListener(_ -> {
                try {
                    deleteSelected();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            listPanel.add(btnDel, BorderLayout.SOUTH);

            // Formular
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(new TitledBorder("Neuer Skill"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5,5,5,5); gbc.fill = GridBagConstraints.HORIZONTAL;

            txtName = new JTextField(15);
            spYears = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
            txtDesc = new JTextArea(3, 15);
            txtDesc.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            JButton btnAdd = new JButton("Speichern");
            btnAdd.addActionListener(e -> addNew());

            addFormRow(formPanel, gbc, 0, "Name:", txtName);
            addFormRow(formPanel, gbc, 1, "Jahre Exp:", spYears);
            addFormRow(formPanel, gbc, 2, "Beschreibung:", new JScrollPane(txtDesc));
            gbc.gridx=1; gbc.gridy=3; formPanel.add(btnAdd, gbc);

            add(listPanel);
            add(formPanel);

            loadData();
        }

        private void loadData() {
            listModel.clear();
            for(Skill s : ServiceLocator.getSkillContainer().getSkills()) listModel.addElement(s);
        }

        private void addNew() {
            if(txtName.getText().trim().isEmpty()) return;
            Skill s = new Skill((Integer) spYears.getValue(), txtName.getText().trim(), txtDesc.getText());

            ServiceLocator.getSkillContainer().addSkill(s);
            txtName.setText(""); txtDesc.setText("");
            loadData();
        }

        private void deleteSelected() throws Exception {
            Skill selected = list.getSelectedValue();
            if(selected == null) return;

            boolean used = false;
            for(Employee e : ServiceLocator.getEmployeeContainer().getEmployees()) {
                if(e.getSkillManager().getSkillById(selected.getId()) != null) { used = true; break; }
            }
            if(used) {
                JOptionPane.showMessageDialog(this, "Kann nicht gelöscht werden!\nEs existieren noch Mitarbeiter mit diesem Skill.", "Fehler", JOptionPane.ERROR_MESSAGE);
            } else {
                ServiceLocator.getSkillContainer().removeSkill(selected);
            }
        }
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        p.add(comp, gbc);
    }
}