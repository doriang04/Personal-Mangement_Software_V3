package gui.views;

import javax.swing.*;

public class EmployeeDetailView extends JPanel implements View {

    private final String title;

    public EmployeeDetailView(String name) {
        this.title = "Profil: " + name;
        add(new JLabel("Detailansicht für: " + name));
        // TODO create real version of this
    }

    public EmployeeDetailView() {
        this.title = "Neuer Mitarbeiter";
        add(new JLabel("Formular zum Erstellen eines neuen Mitarbeiters"));
    }

    @Override
    public String getViewId() {
        return "employee-detail-view";
    }

    @Override
    public String getViewTabTitle() {
        return title;
    }

    @Override public JPanel getContent() { return this; }

    @Override
    public boolean equals(View view) {
        return view.getViewId().equals(this.getViewId());
    }
}