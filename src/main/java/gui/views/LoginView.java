package gui.views;

import javax.swing.*;

public class LoginView extends JPanel implements View {

    public LoginView() {
        add(new JLabel("Login View"));
        // TODO create real version of this
    }

    @Override
    public String getViewId() {
        return "login-view";
    }

    @Override
    public String getViewTabTitle() {
        return "Login";
    }

    @Override public JComponent getComponent() { return this; }

}