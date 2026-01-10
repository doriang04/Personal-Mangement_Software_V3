package gui.views;

import javax.swing.*;

public class TrainingManagementView extends JPanel implements View {

    public TrainingManagementView() {
        add(new JLabel("Schulungsverwaltungsview"));
        // TODO create real version of this
    }

    @Override
    public String getViewId() {
        return "training-management-view";
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