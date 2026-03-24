package gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import logic.Application;
import logic.ApplicationController;
import storage.FileStorage;

/**
 * Controls the dashboard view of the application.
 * Manages the display and filtering of internship applications in a table.
 */
public class DashboardController {

    @FXML private TextField searchField;
    @FXML private TableView<Application> applicationTable;
    @FXML private TableColumn<Application, String> colCompany;
    @FXML private TableColumn<Application, String> colRole;
    @FXML private TableColumn<Application, String> colStatus;
    @FXML private TableColumn<Application, String> colDeadline;

    private final ObservableList<Application> masterList =
            FXCollections.observableArrayList();
    private FilteredList<Application> filteredList;

    private final ApplicationController appController =
            new ApplicationController(new FileStorage());

    /** Injected by MainController — navigates to the new application form. */
    private Runnable onNewApplication;

    public void setOnNewApplication(Runnable onNewApplication) {
        this.onNewApplication = onNewApplication;
    }

    /**
     * Initializes the dashboard after the FXML has been loaded.
     * Sets up table columns and binds the filtered list to the table view.
     */
    @FXML
    public void initialize() {
        colCompany.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("roleTitle"));

        // status is an enum — convert to string
        colStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus().name()));

        // deadline is a LocalDate — handle null
        colDeadline.setCellValueFactory(cellData -> {
            var deadline = cellData.getValue().getDeadline();
            return new SimpleStringProperty(deadline != null ? deadline.toString() : "—");
        });

        filteredList = new FilteredList<>(masterList, p -> true);
        applicationTable.setItems(filteredList);

        // Load real data from storage
        masterList.setAll(appController.getAllApplications());
    }

    @FXML
    private void handleNewApplication() {
        if (onNewApplication != null) onNewApplication.run();
    }

    /**
     * Filters the application list based on the search keyword.
     * Matches entries by company name or role title.
     */
    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().toLowerCase().trim();
        filteredList.setPredicate(entry ->
                keyword.isEmpty()
                        || entry.getCompanyName().toLowerCase().contains(keyword)
                        || entry.getRoleTitle().toLowerCase().contains(keyword)
        );
    }
}