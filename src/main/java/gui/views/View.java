package gui.views;

import javax.swing.*;

public interface View {
    String getViewId();
    String getViewTabTitle();
    JPanel getContent();
    boolean equals(View view);
    void updateSelf();
}
