package gui;

import gui.views.Temp_Example_View;
import gui.views.View;

public class UIController {

    private final MainWindow mainWindow;

    private static UIController instance;

    public static synchronized UIController getInstance() {
        if (instance == null) instance = new UIController();
        return instance;
    }

    private UIController() {
        mainWindow = MainWindow.getInstance();
        mainWindow.showLoginView();
    }

    public void requestTabCreation(String viewId, String[] args) {
        // TODO implement request filtering and such in here before asking MainWindow to change its View
        View newView = null;

        switch(viewId) {
            case "asdf" -> {
                // TODO here you would implement things that need to be done for that specific view
                //      like objects that need to be fetched and such, based on the args
            }
            default -> newView = new Temp_Example_View();
        }

        if (newView == null) newView = new Temp_Example_View();

        mainWindow.openTab(newView, true); // TODO make closable dynamic
    }

}
