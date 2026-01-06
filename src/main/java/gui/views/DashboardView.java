package gui.views;

import javax.swing.*;
import java.awt.*;

public class DashboardView extends JPanel implements View {
    public DashboardView() {
        setLayout(new GridBagLayout());
        add(new JLabel("Willkommen auf dem Dashboard!"));
        // TODO create real version of this
    }

    @Override
    public String getViewId() {
        return "dashboard-view";
    }

    @Override
    public String getViewTabTitle() {
        return "Dashboard";
    }

    @Override
    public JComponent getComponent() {
        return this;
    }
}
