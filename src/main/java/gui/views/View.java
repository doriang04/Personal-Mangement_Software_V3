package gui.views;

import javax.swing.*;

public interface View {
    String getViewId();
    String getViewTabTitle();
    JComponent getComponent();
}
