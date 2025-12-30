package gui.views;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class Temp_Example_View extends JPanel implements View {

    private final String viewId = "temp-example-view";

    public Temp_Example_View() {
        setLayout(new GridLayout());
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        add(new JLabel("Hello! I am the Temp_Example_View!"));
    }

    @Override
    public String getViewId() {
        return viewId;
    }

}
