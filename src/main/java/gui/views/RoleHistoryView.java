package gui.views;

import core.ServiceLocator;
import gui.components.RoleHistoryPanel;
import model.Employee;

import javax.swing.*;
import java.awt.*;

public class RoleHistoryView extends JPanel implements View {

    private final String viewId;
    private final String tabTitle;
    private final Employee employee;

    public RoleHistoryView(int employeeId) {
        this.employee = findEmployeeById(employeeId);

        if (this.employee != null) {
            this.viewId = "role-history-" + employee.getId();
            this.tabTitle = "Rollenhistorie: " + employee.getFirstName();
            initUI();
        } else {
            this.viewId = "role-history-error-" + employeeId;
            this.tabTitle = "Fehler";
            add(new JLabel("Mitarbeiter mit ID " + employeeId + " nicht gefunden."));
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Berechtigungen prüfen, um zu entscheiden, ob die Ansicht bearbeitbar ist
        boolean canEdit = checkPermissions();

        RoleHistoryPanel historyPanel = new RoleHistoryPanel(employee, canEdit);
        add(historyPanel, BorderLayout.CENTER);
    }

    private Employee findEmployeeById(int id) {
        // Diese Logik ist redundant, sollte in einen Service ausgelagert werden
        return ServiceLocator.getEmployeeContainer().getEmployeeById(id); // Annahme: Es gibt so eine Methode
    }

    private boolean checkPermissions() {
        String role = ServiceLocator.getSessionManager().getUserPermission();
        if (role == null) return false;
        role = role.toUpperCase();
        return role.contains("HR") || role.contains("ADMIN");
    }

    @Override
    public String getViewId() {
        return viewId;
    }

    @Override
    public String getViewTabTitle() {
        return tabTitle;
    }

    @Override
    public JPanel getContent() {
        return this;
    }

    @Override
    public boolean equals(View view) {
        return view != null && view.getViewId().equals(this.getViewId());
    }
}