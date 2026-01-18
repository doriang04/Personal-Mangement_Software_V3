package gui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import core.ServiceLocator;
import gui.UIController;
import static gui.UITheme.COLOR_ACCENT;
import static gui.UITheme.COLOR_BG_CONTENT;
import static gui.UITheme.COLOR_BORDER;
import static gui.UITheme.COLOR_HEADER_BG;
import static gui.UITheme.COLOR_HOVER;
import static gui.UITheme.COLOR_TEXT_HEADER;
import static gui.UITheme.createModernCard;
import static gui.UITheme.createStyledButton;
import model.Company;
import model.Department;
import model.Role;
import model.Skill;
import model.Team;

public class ConfigurationView extends JPanel implements View {

    private DepartmentManagementPanel departmentPanel;
    private TeamManagementPanel teamPanel;
    private RoleManagementPanel rolePanel;
    private SkillManagementPanel skillPanel;

    private int hoveredIndex = -1;

    public ConfigurationView() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG_CONTENT);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(true);
        header.setBackground(COLOR_HEADER_BG);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));
        JLabel titleLabel = new JLabel("System-Verwaltung");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_TEXT_HEADER);
        header.add(titleLabel, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));

        departmentPanel = new DepartmentManagementPanel();
        teamPanel = new TeamManagementPanel();
        rolePanel = new RoleManagementPanel();
        skillPanel = new SkillManagementPanel();

        tabbedPane.addTab("Abteilungen", departmentPanel);
        tabbedPane.addTab("Teams", teamPanel);
        tabbedPane.addTab("Rollen", rolePanel);
        tabbedPane.addTab("Skills", skillPanel);

        JPanel tabWrapper = new JPanel(new BorderLayout());
        tabWrapper.setOpaque(false);
        tabWrapper.setBorder(new EmptyBorder(20, 20, 20, 20));
        tabWrapper.add(tabbedPane, BorderLayout.CENTER);

        add(tabWrapper, BorderLayout.CENTER);
    }

    private <T> void setupList(JList<T> list, DefaultListModel<T> model, java.util.function.Function<T, String> mapper) {
        list.setModel(model);
        list.setFixedCellHeight(35);
        list.setSelectionBackground(Color.WHITE);
        list.setSelectionForeground(Color.BLACK);

        list.setCellRenderer((l, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(" " + mapper.apply(value));
            lbl.setOpaque(true);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            if (index == hoveredIndex) {
                lbl.setBackground(COLOR_HOVER);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 5, 0, 0, COLOR_ACCENT),
                        new EmptyBorder(0, 10, 0, 5)
                ));
            } else {
                lbl.setBackground(Color.WHITE);
                lbl.setBorder(new EmptyBorder(0, 15, 0, 5));
            }
            return lbl;
        });

        list.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                if (index != hoveredIndex) {
                    hoveredIndex = index;
                    list.repaint();
                }
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredIndex = -1;
                list.repaint();
            }
        });
    }

    private class DepartmentManagementPanel extends JPanel {
        private DefaultListModel<Department> listModel = new DefaultListModel<>();
        private JList<Department> list = new JList<>();
        private JTextField txtName = new JTextField();
        private JComboBox<Company> cbCompany = new JComboBox<>();

        public DepartmentManagementPanel() {
            setLayout(new GridLayout(1, 2, 20, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel listCard = createModernCard("Vorhandene Abteilungen");
            setupList(list, listModel, Department::getName);
            listCard.add(new JScrollPane(list), BorderLayout.CENTER);
            JButton btnDel = createStyledButton("Ausgewählte löschen", false);
            btnDel.addActionListener(_ -> deleteSelected());
            listCard.add(btnDel, BorderLayout.SOUTH);

            JPanel formCard = createModernCard("Neue Abteilung");
            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 0, 5, 0);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            form.add(new JLabel("Name"), gbc);
            gbc.gridy = 1; form.add(txtName, gbc);
            gbc.gridy = 2; form.add(new JLabel("Firma"), gbc);
            gbc.gridy = 3; form.add(cbCompany, gbc);
            gbc.gridy = 4; gbc.insets = new Insets(15, 0, 0, 0);
            JButton btnAdd = createStyledButton("Hinzufügen", true);
            btnAdd.addActionListener(_ -> addNew());
            form.add(btnAdd, gbc);
            formCard.add(form, BorderLayout.NORTH);

            add(listCard);
            add(formCard);
            loadData();
        }

        public void loadData() {
            listModel.clear();
            ServiceLocator.getDepartmentContainer().getDepartments().forEach(listModel::addElement);
            cbCompany.removeAllItems();
            ServiceLocator.getCompanyContainer().getCompanies().forEach(cbCompany::addItem);
        }

        private void addNew() {
            String name = txtName.getText().trim();
            if (name.isEmpty()) return;
            Company sel = (Company) cbCompany.getSelectedItem();
            ServiceLocator.getDepartmentContainer().addDepartment(new Department(name, sel != null ? sel.getId() : 0));
            txtName.setText("");
            UIController.getInstance().updateMainWindow();
        }

        private void deleteSelected() {
            Department sel = list.getSelectedValue();
            if (sel == null) return;
            boolean ref = ServiceLocator.getTeamContainer().getTeams().stream().anyMatch(t -> t.getDepartmentId() == sel.getId());
            if (ref) { JOptionPane.showMessageDialog(this, "Abteilung enthält noch Teams!"); return; }
            try { ServiceLocator.getDepartmentContainer().removeDepartment(sel); UIController.getInstance().updateMainWindow(); }
            catch (Exception e) { JOptionPane.showMessageDialog(this, "Fehler: " + e.getMessage()); }
        }
    }

    private class TeamManagementPanel extends JPanel {
        private DefaultListModel<Team> listModel = new DefaultListModel<>();
        private JList<Team> list = new JList<>();
        private JTextField txtName = new JTextField();
        private JComboBox<Department> cbDept = new JComboBox<>();

        public TeamManagementPanel() {
            setLayout(new GridLayout(1, 2, 20, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel listCard = createModernCard("Vorhandene Teams");
            setupList(list, listModel, Team::getName);
            listCard.add(new JScrollPane(list), BorderLayout.CENTER);
            JButton btnDel = createStyledButton("Team löschen", false);
            btnDel.addActionListener(_ -> { try { deleteSelected(); } catch (Exception e) { JOptionPane.showMessageDialog(this, "Fehler: " + e.getMessage()); } });
            listCard.add(btnDel, BorderLayout.SOUTH);

            JPanel formCard = createModernCard("Neues Team");
            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; gbc.gridx = 0; gbc.insets = new Insets(5,0,5,0);
            form.add(new JLabel("Team Name"), gbc);
            gbc.gridy = 1; form.add(txtName, gbc);
            gbc.gridy = 2; form.add(new JLabel("Zugehörige Abteilung"), gbc);
            gbc.gridy = 3; form.add(cbDept, gbc);
            gbc.gridy = 4; gbc.insets = new Insets(15,0,0,0);
            JButton btnAdd = createStyledButton("Erstellen", true);
            btnAdd.addActionListener(_ -> addNew());
            form.add(btnAdd, gbc);
            formCard.add(form, BorderLayout.NORTH);

            add(listCard); add(formCard);
            loadData();
        }

        public void loadData() {
            listModel.clear();
            ServiceLocator.getTeamContainer().getTeams().forEach(listModel::addElement);
            cbDept.removeAllItems();
            ServiceLocator.getDepartmentContainer().getDepartments().forEach(cbDept::addItem);
        }

        private void addNew() {
            if (txtName.getText().trim().isEmpty() || cbDept.getSelectedItem() == null) return;
            ServiceLocator.getTeamContainer().addTeam(new Team(txtName.getText().trim(), ((Department)cbDept.getSelectedItem()).getId()));
            txtName.setText("");
            UIController.getInstance().updateMainWindow();
        }

        private void deleteSelected() throws Exception {
            Team sel = list.getSelectedValue();
            if (sel == null) return;
            boolean used = ServiceLocator.getEmployeeContainer().getEmployees().stream().anyMatch(e -> e.getTeamId() == sel.getId());
            if (used) { JOptionPane.showMessageDialog(this, "Team hat noch Mitarbeiter!"); return; }
            ServiceLocator.getTeamContainer().removeTeam(sel);
            UIController.getInstance().updateMainWindow();
        }
    }

    private class RoleManagementPanel extends JPanel {
        private DefaultListModel<Role> listModel = new DefaultListModel<>();
        private JList<Role> list = new JList<>();
        private JTextField txtName = new JTextField();
        private JTextField txtPerm = new JTextField();
        private JTextArea txtDesc = new JTextArea(3, 20);

        public RoleManagementPanel() {
            setLayout(new GridLayout(1, 2, 20, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel listCard = createModernCard("System-Rollen");
            setupList(list, listModel, Role::getName);
            listCard.add(new JScrollPane(list), BorderLayout.CENTER);
            JButton btnDel = createStyledButton("Rolle entfernen", false);
            btnDel.addActionListener(_ -> { try { deleteSelected(); } catch (Exception e) { JOptionPane.showMessageDialog(this, "Fehler: " + e.getMessage()); } });
            listCard.add(btnDel, BorderLayout.SOUTH);

            JPanel formCard = createModernCard("Neue Rolle definieren");
            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; gbc.gridx = 0; gbc.insets = new Insets(3,0,3,0);
            form.add(new JLabel("Rollenbezeichnung"), gbc);
            gbc.gridy = 1; form.add(txtName, gbc);
            gbc.gridy = 2; form.add(new JLabel("Berechtigungsschlüssel"), gbc);
            gbc.gridy = 3; form.add(txtPerm, gbc);
            gbc.gridy = 4; form.add(new JLabel("Beschreibung"), gbc);
            gbc.gridy = 5; txtDesc.setBorder(new LineBorder(COLOR_BORDER)); form.add(new JScrollPane(txtDesc), gbc);
            gbc.gridy = 6; gbc.insets = new Insets(10,0,0,0);
            JButton btnAdd = createStyledButton("Rolle speichern", true);
            btnAdd.addActionListener(_ -> addNew());
            form.add(btnAdd, gbc);
            formCard.add(form, BorderLayout.NORTH);

            add(listCard); add(formCard);
            loadData();
        }

        public void loadData() {
            listModel.clear();
            ServiceLocator.getRoleContainer().getRoles().forEach(listModel::addElement);
        }

        private void addNew() {
            if (txtName.getText().trim().isEmpty()) return;
            ServiceLocator.getRoleContainer().addRole(new Role(txtName.getText().trim(), txtDesc.getText(), txtPerm.getText()));
            txtName.setText(""); txtDesc.setText(""); txtPerm.setText("");
            UIController.getInstance().updateMainWindow();
        }

        private void deleteSelected() throws Exception {
            Role sel = list.getSelectedValue();
            if (sel == null) return;
            ServiceLocator.getRoleContainer().removeRole(sel);
            UIController.getInstance().updateMainWindow();
        }
    }

    private class SkillManagementPanel extends JPanel {
        private DefaultListModel<Skill> listModel = new DefaultListModel<>();
        private JList<Skill> list = new JList<>();
        private JTextField txtName = new JTextField();
        private JSpinner spYears = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
        private JTextArea txtDesc = new JTextArea(3, 20);

        public SkillManagementPanel() {
            setLayout(new GridLayout(1, 2, 20, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel listCard = createModernCard("Skill-Katalog");
            setupList(list, listModel, s -> s.getName() + " (" + s.getRequired_years() + "J)");
            listCard.add(new JScrollPane(list), BorderLayout.CENTER);
            JButton btnDel = createStyledButton("Skill löschen", false);
            btnDel.addActionListener(_ -> { try { deleteSelected(); } catch (Exception e) { JOptionPane.showMessageDialog(this, "Fehler: " + e.getMessage()); } });
            listCard.add(btnDel, BorderLayout.SOUTH);

            JPanel formCard = createModernCard("Neuen Skill anlegen");
            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; gbc.gridx = 0; gbc.insets = new Insets(3,0,3,0);
            form.add(new JLabel("Skill Name"), gbc);
            gbc.gridy = 1; form.add(txtName, gbc);
            gbc.gridy = 2; form.add(new JLabel("Mindestjahre Erfahrung"), gbc);
            gbc.gridy = 3; form.add(spYears, gbc);
            gbc.gridy = 4; form.add(new JLabel("Beschreibung"), gbc);
            gbc.gridy = 5; txtDesc.setBorder(new LineBorder(COLOR_BORDER)); form.add(new JScrollPane(txtDesc), gbc);
            gbc.gridy = 6; gbc.insets = new Insets(10,0,0,0);
            JButton btnAdd = createStyledButton("Skill hinzufügen", true);
            btnAdd.addActionListener(_ -> addNew());
            form.add(btnAdd, gbc);
            formCard.add(form, BorderLayout.NORTH);

            add(listCard); add(formCard);
            loadData();
        }

        public void loadData() {
            listModel.clear();
            ServiceLocator.getSkillContainer().getSkills().forEach(listModel::addElement);
        }

        private void addNew() {
            if (txtName.getText().trim().isEmpty()) return;
            ServiceLocator.getSkillContainer().addSkill(new Skill((Integer)spYears.getValue(), txtName.getText().trim(), txtDesc.getText()));
            txtName.setText(""); txtDesc.setText("");
            UIController.getInstance().updateMainWindow();
        }

        private void deleteSelected() throws Exception {
            Skill sel = list.getSelectedValue();
            if (sel == null) return;
            ServiceLocator.getSkillContainer().removeSkill(sel);
            UIController.getInstance().updateMainWindow();
        }
    }

    @Override public String getViewId() { return "configuration-view"; }
    @Override public String getViewTabTitle() { return "Verwaltung"; }
    @Override public JPanel getContent() { return this; }
    @Override public boolean equals(View v) { return v != null && v.getViewId().equals(getViewId()); }

    @Override
    public void updateSelf() {
        departmentPanel.loadData();
        teamPanel.loadData();
        rolePanel.loadData();
        skillPanel.loadData();
    }
}