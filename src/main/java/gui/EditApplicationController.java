package gui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import logic.Application;
import logic.ApplicationController;
import logic.ApplicationStatus;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Controls the edit-application view.
 * Displays read-only details and lets the user update status, deadline, and notes.
 */
public class EditApplicationController {

    @FXML private Label pageTitleLabel;
    @FXML private Label companyLabel;
    @FXML private Label roleLabel;
    @FXML private Label payLabel;
    @FXML private Label locationLabel;
    @FXML private Label dateAppliedLabel;
    @FXML private ChoiceBox<ApplicationStatus> statusChoice;
    @FXML private Label currentStatusLabel;
    @FXML private DatePicker deadlinePicker;
    @FXML private TextArea notesArea;
    @FXML private Label feedbackLabel;

    private ApplicationController appController;
    private Application application;
    private Runnable onBack;

    /**
     * Sets the ApplicationController used to persist changes.
     * Must be called by MainController before the view is displayed.
     *
     * @param appController The application controller to use.
     */
    public void setAppController(ApplicationController appController) {
        this.appController = appController;
    }

    /**
     * Registers a callback to return to the dashboard.
     *
     * @param onBack Runnable to execute when navigating back.
     */
    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    /**
     * Loads the given application's data into the view.
     * Must be called after FXML injection and after setAppController.
     *
     * @param app The application to display and edit.
     */
    public void loadApplication(Application app) {
        this.application = app;

        pageTitleLabel.setText(app.getCompanyName() + " — " + app.getRoleTitle());
        companyLabel.setText(app.getCompanyName());
        roleLabel.setText(app.getRoleTitle());
        payLabel.setText(app.getPay() > 0 ? String.format("$%.0f", app.getPay()) : "—");
        locationLabel.setText(app.getLocation() != null && !app.getLocation().isBlank()
                ? app.getLocation() : "—");
        dateAppliedLabel.setText(app.getDateApplied().toString());

        statusChoice.getItems().setAll(ApplicationStatus.values());
        statusChoice.setValue(app.getStatus());
        currentStatusLabel.setText("Currently: " + app.getStatus().name());

        if (app.getDeadline() != null) {
            deadlinePicker.setValue(app.getDeadline());
        }

        notesArea.setText(app.getNotes() != null ? app.getNotes() : "");
        feedbackLabel.setText("");
    }

    @FXML
    private void handleClearDeadline() {
        deadlinePicker.setValue(null);
    }

    @FXML
    private void handleSave() {
        feedbackLabel.setText("");
        boolean hasChanged = false;

        ApplicationStatus selectedStatus = statusChoice.getValue();
        if (selectedStatus != null && selectedStatus != application.getStatus()) {
            try {
                appController.updateStatus(application.getId(), selectedStatus);
                hasChanged = true;
            } catch (IllegalStateException e) {
                feedbackLabel.setText(e.getMessage());
                feedbackLabel.setStyle("-fx-text-fill: #d45b5b;");
                statusChoice.setValue(application.getStatus());
                return;
            }
        }

        LocalDate newDeadline = deadlinePicker.getValue();
        LocalDate currentDeadline = application.getDeadline();
        boolean hasDeadlineChanged = (newDeadline == null && currentDeadline != null)
                || (newDeadline != null && !newDeadline.equals(currentDeadline));
        if (hasDeadlineChanged) {
            try {
                appController.updateDeadline(application.getId(), newDeadline);
                hasChanged = true;
            } catch (RuntimeException e) {
                feedbackLabel.setText("Failed to update deadline: " + e.getMessage());
                feedbackLabel.setStyle("-fx-text-fill: #d45b5b;");
                return;
            }
        }

        String newNotes = notesArea.getText().trim();
        String currentNotes = application.getNotes() != null ? application.getNotes() : "";
        if (!newNotes.equals(currentNotes)) {
            try {
                appController.updateNotes(application.getId(), newNotes);
                hasChanged = true;
            } catch (RuntimeException e) {
                feedbackLabel.setText("Failed to update notes: " + e.getMessage());
                feedbackLabel.setStyle("-fx-text-fill: #d45b5b;");
                return;
            }
        }

        if (hasChanged) {
            this.application = appController.getApplicationById(application.getId());
            currentStatusLabel.setText("Currently: " + application.getStatus().name());
            feedbackLabel.setText("Changes saved successfully.");
            feedbackLabel.setStyle("-fx-text-fill: #3ea87a;");
        } else {
            feedbackLabel.setText("No changes to save.");
            feedbackLabel.setStyle("-fx-text-fill: #a0a4be;");
        }
    }

    @FXML
    private void handleDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Application");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will permanently delete the application for "
                + application.getCompanyName() + " — " + application.getRoleTitle() + ".");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                appController.deleteApplication(application.getId());
            } catch (RuntimeException e) {
                GuiUtils.showError("Could Not Delete", e.getMessage());
                return;
            }
            if (onBack != null) {
                onBack.run();
            }
        }
    }

    @FXML
    private void handleBack() {
        if (onBack != null) {
            onBack.run();
        }
    }
}
