package gui.views;

import javax.swing.*;

public class EmployeeSearchView extends JPanel implements View {

    public EmployeeSearchView() {
        add(new JLabel("Mitarbeitersuche View"));
        // TODO create real version of this
    }

    @Override
    public String getViewId() {
        return "employee-search-view";
    }

    @Override
    public String getViewTabTitle() {
        return "Mitarbeiter suchen";
    }

    @Override public JComponent getComponent() { return this; }

}