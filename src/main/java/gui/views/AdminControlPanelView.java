package gui.views;

import javax.swing.*;

public class AdminControlPanelView extends JPanel implements View {

    public AdminControlPanelView() {
        add(new JLabel("Admin Systemsteuerung"));
        // TODO create real version of this
    }

    @Override
    public String getViewId() {
        return "admin-control-panel-view";
    }

    @Override
    public String getViewTabTitle() {
        return "Schulungsverwaltung";
    }

    @Override public JPanel getContent() { return this; }

    @Override
    public boolean equals(View view) {
        return view.getViewId().equals(this.getViewId());
    }

}