package controller;

import application.Helpers;
import application.SceneManager;
import application.Strings;
import database.JDBC;
import database.QueryManager;
import interfaces.INewEditCallback;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.ResourceBundle;

public class TurnoverController implements Initializable {
    /******************************************************************************
     * FXML                                                                       *
     ******************************************************************************/
    @FXML
    public DatePicker pickerDate;
    @FXML
    public ChoiceBox<BasicIdName> choiceDepartment;
    @FXML
    public ChoiceBox<BasicIdName> choiceShift;
    @FXML
    public TableView<Issue> tblIssues;
    @FXML
    public TableColumn<Issue, LocalTime> tblIssuesColStart;
    @FXML
    public TableColumn<Issue, LocalTime> tblIssuesColEnd;
    @FXML
    public TableColumn<Issue, Category> tblIssuesColCategory;
    @FXML
    public TableColumn<Issue, Subcategory> tblIssuesColSubcategory;
    @FXML
    public Button btnDeleteIssue;
    @FXML
    public Button btnAddIssue;
    @FXML
    public TableView<Downtime> tblDowntime;
    @FXML
    public TableColumn<Downtime, Category> tblDowntimeColCategory;
    @FXML
    public TableColumn<Downtime, Subcategory> tblDowntimeColSubcategory;
    @FXML
    public TableColumn<Downtime, String> tblDowntimeColDuration;
    @FXML
    public TextArea txtNotes;
    @FXML
    public Button btnDelete;
    @FXML
    public Button btnCancel;
    @FXML
    public Button btnSave;

    /******************************************************************************
     * Members                                                                    *
     ******************************************************************************/
    private Issue selectedIssue = null;
    private boolean newTurnover = true;
    private int turnoverId = 0;

    /******************************************************************************
     * Observables                                                                *
     ******************************************************************************/
    private ObservableList<BasicIdName> departments = FXCollections.observableArrayList();
    private ObservableList<BasicIdName> shifts = FXCollections.observableArrayList();
    private ObservableList<Issue> issues = FXCollections.observableArrayList();
    private ObservableList<Downtime> downtime = FXCollections.observableArrayList();

    /******************************************************************************
     * Interface Implementations                                                  *
     ******************************************************************************/
    private INewEditCallback callback;

    public void setSaveEditCallback(
            Runnable newCallback,
            Runnable editCallback
    ) {
        this.callback = new INewEditCallback(){
            @Override
            public void saveNew() {
                newCallback.run();
            }

            @Override
            public void saveChanges() {
                editCallback.run();
            }
        };
    }

    /******************************************************************************
     * Initialize                                                                 *
     ******************************************************************************/
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        JDBC.openConnection();
        departments = QueryManager.getDepartments();
        shifts = QueryManager.getShifts();
        JDBC.closeConnection();

        choiceDepartment.setItems(departments);
        choiceShift.setItems(shifts);

        tblIssuesColStart.setCellValueFactory(new PropertyValueFactory("StartTime"));
        tblIssuesColEnd.setCellValueFactory(new PropertyValueFactory("EndTime"));
        tblIssuesColCategory.setCellValueFactory(new PropertyValueFactory("Category"));
        tblIssuesColSubcategory.setCellValueFactory(new PropertyValueFactory("Subcategory"));

        tblIssues.setItems(issues);
        tblIssuesColStart.setSortType(TableColumn.SortType.DESCENDING);
        tblIssues.getSortOrder().add(tblIssuesColStart);
        tblIssues.sort();

        tblIssues.setOnMouseClicked(this::handleCellClicked);
        tblIssues.setOnKeyPressed(this::handleCellKeyPressed);

        tblDowntimeColCategory.setCellValueFactory(new PropertyValueFactory("Category"));
        tblDowntimeColSubcategory.setCellValueFactory(new PropertyValueFactory("Subcategory"));
        tblDowntimeColDuration.setCellValueFactory(p -> {
            if(p.getValue() != null) {
                Duration duration = p.getValue().getDuration();
                return new SimpleStringProperty(String.format("%02d:%02d", duration.toHours(), duration.toMinutesPart()));
            }

            return new SimpleStringProperty("");
        });
        tblDowntime.setItems(downtime);
        tblDowntimeColDuration.setSortType(TableColumn.SortType.DESCENDING);
        tblDowntime.getSortOrder().add(tblDowntimeColDuration);
        tblDowntime.sort();

        btnAddIssue.setOnAction(this::handleAddIssue);
        btnDeleteIssue.setOnAction(this::handleDeleteIssue);
        btnSave.setOnAction(this::handleSave);
        btnCancel.setOnAction(this::handleCancel);
        btnDelete.setOnAction(this::handleDelete);
    }

    /******************************************************************************
     * On Event - Delete Button                                                   *
     ******************************************************************************/
    public void handleDelete(ActionEvent actionEvent) {
        JDBC.openConnection();
        boolean didDelete = QueryManager.deleteTurnover(this.turnoverId);
        JDBC.closeConnection();

        if(didDelete) {
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            stage.close();
            this.callback.saveChanges();
        }
    }

    /******************************************************************************
     * On Event - Save Button                                                     *
     ******************************************************************************/
    public void handleSave(ActionEvent actionEvent) {
        LocalDate date = pickerDate.getValue();
        BasicIdName department = choiceDepartment.getValue();
        BasicIdName shift = choiceShift.getValue();

        if(date == null) {
            Helpers.displayError("Invalid Date", "Please choose a valid date.");
            return;
        }

        if(department == null) {
            Helpers.displayError("Invalid Department", "Please choose a valid department.");
            return;
        }

        if(shift == null) {
            Helpers.displayError("Invalid Shift", "Please choose a valid shift.");
            return;
        }

        String notes = txtNotes.getText().trim();
        Turnover turnover = new Turnover(this.turnoverId, date, shift, department, notes);

        JDBC.openConnection();
        if(QueryManager.turnoverExists(turnover)) {
            Helpers.displayError("Turnover Already Exists", "Turnover already exists with the same date, department, and shift.");
            JDBC.closeConnection();
            return;
        }

        if(newTurnover) {
            QueryManager.createTurnover(turnover, issues);
            JDBC.closeConnection();
            this.callback.saveNew();

        } else {
            QueryManager.updateTurnover(turnover, issues);
            JDBC.closeConnection();
            this.callback.saveChanges();
        }

        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    /******************************************************************************
     * On Event - Cancel Button                                                   *
     ******************************************************************************/
    public void handleCancel(ActionEvent actionEvent) {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    /******************************************************************************
     * On Event - Add Issue Button                                                *
     ******************************************************************************/
    public void handleAddIssue(ActionEvent actionEvent) {
        // Get a new id
        int id = 0;
        for(Issue i : this.issues) {
            if(i.getId() >= id) {
                id = i.getId() + 1;
            }
        }

        IssueController controller = new IssueController();
        Scene scene = SceneManager.createScene(controller, Strings.FXML_ISSUE);
        setControllerCallback(controller);
        controller.setId(id);
        SceneManager.showSceneInNewWindow( scene, Strings.TITLE_ADD_ISSUE);
    }

    /******************************************************************************
     * On Event - Delete Issue Button                                             *
     ******************************************************************************/
    public void handleDeleteIssue(ActionEvent actionEvent) {
        issues.remove(selectedIssue);
        selectedIssue = null;

        btnDeleteIssue.setDisable(true);
        updateDowntime();
    }

    /******************************************************************************
     * On Event - Key Pressed in Issues Table                                     *
     ******************************************************************************/
    public void handleCellKeyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.UP)
            selectedIssue = tblIssues.getSelectionModel().getSelectedItem();
    }

    /******************************************************************************
     * On Event - Cell Clicked in Issues Table                                    *
     ******************************************************************************/
    public void handleCellClicked(MouseEvent mouseEvent) {
        selectedIssue = tblIssues.getSelectionModel().getSelectedItem();

        if(selectedIssue == null) {
            btnDeleteIssue.setDisable(true);
            return;
        }

        btnDeleteIssue.setDisable(false);

        if(mouseEvent.getClickCount() == 2) {
            IssueController controller = new IssueController();
            Scene scene = SceneManager.createScene(controller, Strings.FXML_ISSUE);
            setControllerCallback(controller);
            controller.setId(selectedIssue.getId());
            controller.populateData(tblIssues.getSelectionModel().getSelectedIndex(), this.selectedIssue);
            SceneManager.showSceneInNewWindow( scene, Strings.TITLE_EDIT_ISSUE);
        }
    }

    /******************************************************************************
     * Update downtime table.                                                     *
     ******************************************************************************/
    public void updateDowntime() {
        downtime.clear();

        for(Issue issue : issues) {
            Optional<Downtime> optDT = this.downtime
                    .stream()
                    .filter(dt ->
                            dt.getCategory().getName().equals(issue.getCategory().getName()) &&
                            dt.getSubcategory().getName().equals(issue.getSubcategory().getName()))
                    .findFirst();
            optDT.ifPresentOrElse(
                (value) -> {
                    if(issue.getEndTime().isAfter(issue.getStartTime())) {
                        value.addDuration(Duration.between(issue.getStartTime(), issue.getEndTime()));
                    } else {
                        Duration difference = Duration.between(issue.getEndTime(), issue.getStartTime());
                        value.addDuration(Duration.ofHours(24).minus(difference));
                    }
                },
                () -> {
                    if(issue.getEndTime().isAfter(issue.getStartTime())) {
                        downtime.add(new Downtime(issue.getCategory(), issue.getSubcategory(), Duration.between(issue.getStartTime(), issue.getEndTime())));
                    } else {
                        Duration difference = Duration.between(issue.getEndTime(), issue.getStartTime());
                        downtime.add(new Downtime(issue.getCategory(), issue.getSubcategory(), Duration.ofHours(24).minus(difference)));
                    }
                }
            );
        }

        tblDowntime.sort();
    }

    /******************************************************************************
     * Populate user data                                                         *
     ******************************************************************************/
    public void populateData(Turnover turnover) {
        this.newTurnover = false;
        this.turnoverId = turnover.getId();

        pickerDate.setValue(turnover.getDate());
        if(departments.contains(turnover.getDepartment())) {
            choiceDepartment.getSelectionModel().select(turnover.getDepartment());
        }
        if(shifts.contains(turnover.getShift())) {
            choiceShift.getSelectionModel().select(turnover.getShift());
        }

        JDBC.openConnection();
        issues = QueryManager.getIssuesForTurnoverId(turnover.getId());
        JDBC.closeConnection();
        tblIssues.setItems(issues);
        tblIssuesColStart.setSortType(TableColumn.SortType.DESCENDING);
        tblIssues.getSortOrder().add(tblIssuesColStart);
        tblIssues.sort();

        // might need to updateDowntime()
        updateDowntime();

        txtNotes.setText(turnover.getNotes());
        btnDelete.setDisable(false);
    }

    /******************************************************************************
     * Set controller callback.                                                   *
     ******************************************************************************/
    private void setControllerCallback(IssueController controller) {
        controller.setSaveEditCallback(
                (issue) -> {
                    for(Issue i : this.issues) {
                        if(Helpers.doesTimeOverlap(
                            issue.getStartTime(),
                            issue.getEndTime(),
                            i.getStartTime(),
                            i.getEndTime()
                        )) {
                            return false;
                        }
                    }

                    this.issues.add(issue);
                    tblIssues.sort();
                    updateDowntime();
                    return true;
                },
                (issue, index) -> {
                    for(Issue i : this.issues) {

                        if(issue.getId() == i.getId()) {
                            continue;
                        }

                        if(Helpers.doesTimeOverlap(
                                issue.getStartTime(),
                                issue.getEndTime(),
                                i.getStartTime(),
                                i.getEndTime()
                        )) {
                            return false;
                        }
                    }

                    this.issues.set(index, issue);
                    tblIssues.sort();
                    updateDowntime();
                    return true;
                }
        );
    }
}
