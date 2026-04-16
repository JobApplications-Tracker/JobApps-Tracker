package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import logic.Application;
import logic.ApplicationController;
import logic.InterviewController;
import logic.ReminderService;
import storage.FileStorage;

import java.io.IOException;
import java.util.List;

/**
 * Controls the main window of the application.
 * Handles navigation between views and highlights the active sidebar button.
 * Owns the single instances of FileStorage, ApplicationController, InterviewController,
 * and ReminderService that are injected into all child controllers.
 */
public class MainController {

    private static final String FXML_DASHBOARD        = "/view/DashboardView.fxml";
    private static final String FXML_CALENDAR         = "/view/CalendarView.fxml";
    private static final String FXML_COMPARE          = "/view/CompareView.fxml";
    private static final String FXML_NEW_APPLICATION  = "/view/NewApplicationView.fxml";
    private static final String FXML_EDIT_APPLICATION = "/view/EditApplicationView.fxml";

    @FXML private StackPane contentArea;
    @FXML private Button btnDashboard;
    @FXML private Button btnCalendar;
    @FXML private Button btnCompare;

    private List<Button> navButtons;

    private final FileStorage fileStorage = new FileStorage();
    private final ApplicationController appController = new ApplicationController(fileStorage);
    private final InterviewController interviewController = new InterviewController(fileStorage);
    private final ReminderService reminderService = new ReminderService(fileStorage);

    /**
     * Initialises the controller after the FXML has been loaded.
     * Resets all navigation buttons to their inactive style and shows the dashboard view.
     */
    @FXML
    public void initialize() {
        navButtons = List.of(btnDashboard, btnCalendar, btnCompare);
        // Apply default style — FXML doesn't set styleClass on these buttons
        // so we initialise them all as inactive here.
        navButtons.forEach(b -> b.getStyleClass().setAll("nav-button"));
        showDashboard();
    }

    private void setActive(Button active) {
        for (Button btn : navButtons) {
            btn.getStyleClass().setAll(btn == active ? "nav-button-active" : "nav-button");
        }
    }

    /**
     * Navigates to the dashboard view and marks its sidebar button as active.
     * Also wires the new-application callback so the dashboard can trigger navigation.
     */
    @FXML
    public void showDashboard() {
        setActive(btnDashboard);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_DASHBOARD));
            Node view = loader.load();
            DashboardController controller = loader.getController();
            controller.setAppController(appController);
            controller.setOnNewApplication(this::showNewApplication);
            controller.setOnEditApplication(this::showEditApplication);
            controller.loadData();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + FXML_DASHBOARD, e);
        }
    }

    private void showEditApplication(Application app) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_EDIT_APPLICATION));
            Node view = loader.load();
            EditApplicationController controller = loader.getController();
            controller.setAppController(appController);
            controller.setOnBack(this::showDashboard);
            controller.loadApplication(app);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + FXML_EDIT_APPLICATION, e);
        }
    }

    private void showNewApplication() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_NEW_APPLICATION));
            Node view = loader.load();
            NewApplicationController controller = loader.getController();
            controller.setAppController(appController);
            controller.setOnSuccess(this::showDashboard);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + FXML_NEW_APPLICATION, e);
        }
    }

    @FXML
    private void showCalendar() {
        setActive(btnCalendar);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_CALENDAR));
            Node view = loader.load();
            CalendarController controller = loader.getController();
            controller.setAppController(appController);
            controller.setInterviewController(interviewController);
            controller.setReminderService(reminderService);
            controller.loadData();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + FXML_CALENDAR, e);
        }
    }

    @FXML
    private void showCompare() {
        setActive(btnCompare);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_COMPARE));
            Node view = loader.load();
            CompareController controller = loader.getController();
            controller.setAppController(appController);
            controller.loadData();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + FXML_COMPARE, e);
        }
    }
}