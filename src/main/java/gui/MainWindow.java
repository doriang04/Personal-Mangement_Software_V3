package gui;

import gui.views.Temp_Example_View;
import gui.views.View;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private static MainWindow instance;

    private JPanel coreContentPanel;

    public static synchronized MainWindow getInstance() {
        if (instance == null) instance = new MainWindow();
        return instance;
    }

    private MainWindow() {
        setTitle("Personalmanagement Software");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        init();
        setVisible(true);
    }

    private void init() {
        // TODO write the init function: initializes main visual components that are to be seen regardless of which
        //      view is active; below is just temporary code to test functionality

        setLayout(new BorderLayout());

        JLabel north_label = new JLabel("Hello, I am the north_label");
        JLabel south_label = new JLabel("Hello, I am the south_label");
        JLabel east_label = new JLabel("Hello, I am the east_label");
        JLabel west_label = new JLabel("Hello, I am the west_label");

        add(north_label, BorderLayout.NORTH);
        add(south_label, BorderLayout.SOUTH);
        add(east_label, BorderLayout.EAST);
        add(west_label, BorderLayout.WEST);

        // Todo - below is what could actually be in the function

        changeCoreContentPanel(new Temp_Example_View()); // TODO change to default screen
    }

    protected void changeCoreContentPanel(JPanel view) {
        Component activeViewComponent = ((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (activeViewComponent != null) {
            remove(activeViewComponent);
        }

        coreContentPanel = view;
        add(coreContentPanel, BorderLayout.CENTER);
    }

}
