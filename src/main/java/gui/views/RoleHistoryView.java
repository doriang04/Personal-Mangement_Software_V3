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

    // 1. Store a reference to the child panel.
    private RoleHistoryPanel historyPanel;

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

        // Check permissions to decide if the view is editable
        boolean canEdit = checkPermissions();

        // 2. Create and assign the panel to the class field.
        this.historyPanel = new RoleHistoryPanel(employee, canEdit);
        add(this.historyPanel, BorderLayout.CENTER);
    }

    private Employee findEmployeeById(int id) {
        return ServiceLocator.getEmployeeContainer().getEmployeeById(id);
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

    /**
     * Refreshes the view by delegating the update call to the contained
     * RoleHistoryPanel. This ensures the table and form within the panel
     * are reloaded with the latest data.
     */
    @Override
    public void updateSelf() {
        // 3. If the panel exists, tell it to reload its own data.
        if (this.historyPanel != null) {
            // This assumes RoleHistoryPanel has a public `loadData()` method.
            this.historyPanel.loadData();
        }
    }
}