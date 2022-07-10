package controller;

import application.Helpers;
import application.PermissionHelper;
import application.SceneManager;
import application.Strings;
import database.JDBC;
import database.QueryManager;
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
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    /******************************************************************************
     * FXML                                                                       *
     ******************************************************************************/
    @FXML
    public MenuBar menuBar;
    @FXML
    public MenuItem menuNewTurnover;
    @FXML
    public MenuItem menuLogout;
    @FXML
    public Menu menuSettings;
    @FXML
    public MenuItem menuManageUsers;
    @FXML
    public MenuItem menuManageCategories;
    @FXML
    public MenuItem menuManageDepartments;
    @FXML
    public MenuItem menuManageShifts;
    @FXML
    public MenuItem menuReport;
    @FXML
    public DatePicker pickerStart;
    @FXML
    public DatePicker pickerEnd;
    @FXML
    public Button btnClearDates;
    @FXML
    public ChoiceBox<BasicIdName> choiceDepartments;
    @FXML
    public ChoiceBox<BasicIdName> choiceShifts;
    @FXML
    public TableView<Turnover> tblTurnover;
    @FXML
    public TableColumn<Turnover, LocalDate> tblTurnoverColDate;
    @FXML
    public TableColumn<Turnover, BasicIdName> tblTurnoverColDepartment;
    @FXML
    public TableColumn<Turnover, BasicIdName> tblTurnoverColShift;
    @FXML
    public TableColumn<Turnover, String> tblTurnoverColIssues;
    @FXML
    public TableColumn<Turnover, String> tblTurnoverColDowntime;

    /******************************************************************************
     * Members                                                                    *
     ******************************************************************************/
    private ObservableList<BasicIdName> listDepartments = FXCollections.observableArrayList();
    private ObservableList<BasicIdName> listShifts = FXCollections.observableArrayList();
    private ObservableList<Turnover> listTurnover = FXCollections.observableArrayList();
    private Turnover selectedTurnover;

    /******************************************************************************
     * Initialize                                                                 *
     ******************************************************************************/
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if(PermissionHelper.isAdmin()) {
            menuSettings.setDisable(false);
        }

        JDBC.openConnection();
        listDepartments = QueryManager.getDepartments();
        listShifts = QueryManager.getShifts();
        JDBC.closeConnection();
        listDepartments.add(0, new BasicIdName(0, "All"));
        listShifts.add(0, new BasicIdName(0, "All"));

        choiceDepartments.setItems(listDepartments);
        choiceDepartments.getSelectionModel().select(0);
        choiceDepartments.setOnAction(this::handleDepartmentSelect);

        choiceShifts.setItems(listShifts);
        choiceShifts.getSelectionModel().select(0);
        choiceShifts.setOnAction(this::handleShiftSelect);

        tblTurnoverColDate.setCellValueFactory(new PropertyValueFactory("Date"));
        tblTurnoverColDepartment.setCellValueFactory(new PropertyValueFactory("Department"));
        tblTurnoverColShift.setCellValueFactory(new PropertyValueFactory("Shift"));
        tblTurnoverColIssues.setCellValueFactory(p -> {
            if(p.getValue() != null) {
                JDBC.openConnection();
                int numIssues = QueryManager.getIssuesCountForTurnoverId(p.getValue().getId());
                JDBC.closeConnection();
                return new SimpleStringProperty(Integer.toString(numIssues));
            }

            return new SimpleStringProperty("");
        });
        tblTurnoverColDowntime.setCellValueFactory(p -> {
            Duration duration = Duration.ZERO;

            if(p.getValue() != null) {
                JDBC.openConnection();
                List<Issue> issues = QueryManager.getIssuesForTurnoverId(p.getValue().getId());
                JDBC.closeConnection();

                for(Issue issue : issues) {
                    if(issue.getEndTime().isAfter(issue.getStartTime())) {
                        duration = duration.plus(Duration.between(issue.getStartTime(), issue.getEndTime()));
                    } else {
                        Duration difference = Duration.between(issue.getEndTime(), issue.getStartTime());
                        duration = duration.plus(Duration.ofHours(24).minus(difference));
                    }
                }
            }

            return new SimpleStringProperty(String.format("%02d:%02d", duration.toHours(), duration.toMinutesPart()));
        });

        JDBC.openConnection();
        listTurnover = QueryManager.getTurnoverList();
        JDBC.closeConnection();

        tblTurnover.setItems(listTurnover);
        tblTurnoverColDate.setSortType(TableColumn.SortType.DESCENDING);
        tblTurnover.getSortOrder().add(tblTurnoverColDate);
        tblTurnover.sort();
        tblTurnover.setOnMouseClicked(this::handleCellClicked);
        tblTurnover.setOnKeyPressed(this::handleCellKeyPressed);

        menuNewTurnover.setOnAction(this::handleNewTurnover);
        menuLogout.setOnAction(this::handleLogout);
        menuManageUsers.setOnAction(this::handleManageUsers);
        menuManageCategories.setOnAction(this::handleManageCategories);
        menuManageDepartments.setOnAction(this::handleManageDepartments);
        menuManageShifts.setOnAction(this::handleManageShifts);
        menuReport.setOnAction(this::handleAllReport);

        pickerStart.setOnAction(this::handlePickerChoice);
        pickerEnd.setOnAction(this::handlePickerChoice);
        btnClearDates.setOnAction(this::handleClearDates);
    }

    /******************************************************************************
     * On Event - New Turnover Menu Item                                          *
     ******************************************************************************/
    public void handleNewTurnover(ActionEvent actionEvent) {
        TurnoverController controller = new TurnoverController();
        Scene scene = SceneManager.createScene(controller,Strings.FXML_TURNOVER);
        setControllerCallback(controller);
        SceneManager.showSceneInNewWindow(scene, Strings.TITLE_ADD_TURNOVER);
    }

    /******************************************************************************
     * On Event - Manage Users Menu Item                                          *
     ******************************************************************************/
    public void handleManageUsers(ActionEvent actionEvent) {
        ManageUsersController controller = new ManageUsersController();
        Scene scene = SceneManager.createScene(controller, Strings.FXML_SETTINGS_MANAGE_USERS);
        SceneManager.showSceneInNewWindow(scene, Strings.TITLE_SETTINGS_MANAGE_USERS);
    }

    /******************************************************************************
     * On Event - Manage Categories Menu Item                                     *
     ******************************************************************************/
    public void handleManageCategories(ActionEvent actionEvent) {
        ManageCategoriesController controller = new ManageCategoriesController();
        Scene scene = SceneManager.createScene(controller, Strings.FXML_SETTINGS_MANAGE_CATEGORIES);
        SceneManager.showSceneInNewWindow(scene, Strings.TITLE_SETTINGS_MANAGE_CATEGORIES);
    }

    /******************************************************************************
     * On Event - Manage Departments Menu Item                                    *
     ******************************************************************************/
    public void handleManageDepartments(ActionEvent actionEvent) {
        ManageDepartmentsController controller = new ManageDepartmentsController();
        Scene scene = SceneManager.createScene(controller, Strings.FXML_SETTINGS_MANAGE_DEPARTMENTS);
        SceneManager.showSceneInNewWindow(scene, Strings.TITLE_SETTINGS_MANAGE_DEPARTMENTS);
    }

    /******************************************************************************
     * On Event - Manage Shifts Menu Item                                         *
     ******************************************************************************/
    public void handleManageShifts(ActionEvent actionEvent) {
        ManageShiftsController controller = new ManageShiftsController();
        Scene scene = SceneManager.createScene(controller, Strings.FXML_SETTINGS_MANAGE_DEPARTMENTS);
        SceneManager.showSceneInNewWindow(scene, Strings.TITLE_SETTINGS_MANAGE_SHIFTS);
    }

    /******************************************************************************
     * On Event - Report Menu Item                                                *
     ******************************************************************************/
    public void handleAllReport(ActionEvent actionEvent) {
        ReportController controller = new ReportController();
        Scene scene = SceneManager.createScene(controller, Strings.FXML_REPORT);
        SceneManager.showSceneInNewWindow(scene, Strings.TITLE_REPORT);
    }

    /******************************************************************************
     * On Event - Logout Menu Item                                                *
     ******************************************************************************/
    public void handleLogout(ActionEvent actionEvent) {
        LoginController controller = new LoginController();
        Stage stage = (Stage) menuBar.getScene().getWindow();
        Scene scene = SceneManager.createScene(controller, Strings.FXML_LOGIN);
        SceneManager.showScene(stage, scene, Strings.TITLE_LOGIN);
    }

    /******************************************************************************
     * On Event - Clear Dates Button                                              *
     ******************************************************************************/
    public void handleClearDates(ActionEvent actionEvent) {
        pickerStart.setValue(null);
        pickerEnd.setValue(null);
        BasicIdName shift = choiceShifts.getSelectionModel().getSelectedItem();

        JDBC.openConnection();
        listTurnover = QueryManager.getTurnoverList();
        JDBC.closeConnection();

        if(shift.getId() == 0) {
            tblTurnover.setItems(listTurnover);
        } else {
            tblTurnover.setItems(FXCollections.observableArrayList(listTurnover.filtered(turnover -> turnover.getShift().getName().equals(shift.getName())).stream().toList()));
        }
    }

    /******************************************************************************
     * On Event - Select Department                                               *
     ******************************************************************************/
    public void handleDepartmentSelect(ActionEvent actionEvent) {
        BasicIdName department = choiceDepartments.getSelectionModel().getSelectedItem();

        if(department.getId() == 0) {
            tblTurnover.setItems(listTurnover);
        } else {
            tblTurnover.setItems(FXCollections.observableArrayList(listTurnover.filtered(turnover -> turnover.getDepartment().getName().equals(department.getName())).stream().toList()));
        }
    }

    /******************************************************************************
     * On Event - Select Shift                                                    *
     ******************************************************************************/
    public void handleShiftSelect(ActionEvent actionEvent) {
        BasicIdName shift = choiceShifts.getSelectionModel().getSelectedItem();

        if(shift.getId() == 0) {
            tblTurnover.setItems(listTurnover);
        } else {
            tblTurnover.setItems(FXCollections.observableArrayList(listTurnover.filtered(turnover -> turnover.getShift().getName().equals(shift.getName())).stream().toList()));
        }
    }

    /******************************************************************************
     * On Event - Start & End Date Pickers                                        *
     ******************************************************************************/
    public void handlePickerChoice(ActionEvent actionEvent) {
        LocalDate start = pickerStart.getValue();
        LocalDate end = pickerEnd.getValue();

        if(start == null || end == null)
            return;

        if(end.isBefore(start)) {
            Helpers.displayError("End Date is before Start Date", "Please select an End Date that is after the Start Date, or the same as the Start Date.");
            return;
        }

        JDBC.openConnection();
        listTurnover = QueryManager.getTurnoverListBetweenDates(start, end);
        JDBC.closeConnection();

        tblTurnover.setItems(listTurnover);
        tblTurnoverColDate.setSortType(TableColumn.SortType.DESCENDING);
        tblTurnover.getSortOrder().add(tblTurnoverColDate);
        tblTurnover.sort();
    }

    /******************************************************************************
     * On Event - Key Pressed in Turnover Table                                   *
     ******************************************************************************/
    public void handleCellKeyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.UP)
            selectedTurnover = tblTurnover.getSelectionModel().getSelectedItem();
    }

    /******************************************************************************
     * On Event - Cell Clicked in Turnover Table                                  *
     ******************************************************************************/
    public void handleCellClicked(MouseEvent mouseEvent) {
        selectedTurnover = tblTurnover.getSelectionModel().getSelectedItem();

        if(selectedTurnover == null) {
            return;
        }

        if(mouseEvent.getClickCount() == 2) {
            TurnoverController controller = new TurnoverController();
            Scene scene = SceneManager.createScene(controller, Strings.FXML_TURNOVER);
            setControllerCallback(controller);
            controller.populateData(selectedTurnover);
            SceneManager.showSceneInNewWindow( scene, Strings.TITLE_EDIT_TURNOVER);
        }
    }

    /******************************************************************************
     * Set controller callback.                                                   *
     ******************************************************************************/
    private void setControllerCallback(TurnoverController controller) {
        controller.setSaveEditCallback(
                () -> {
                    JDBC.openConnection();
                    listTurnover = QueryManager.getTurnoverList();
                    JDBC.closeConnection();

                    tblTurnover.setItems(listTurnover);
                    tblTurnoverColDate.setSortType(TableColumn.SortType.DESCENDING);
                    tblTurnover.getSortOrder().add(tblTurnoverColDate);
                    tblTurnover.sort();
                },
                () -> {
                    JDBC.openConnection();
                    listTurnover = QueryManager.getTurnoverList();
                    JDBC.closeConnection();

                    tblTurnover.setItems(listTurnover);
                    tblTurnoverColDate.setSortType(TableColumn.SortType.DESCENDING);
                    tblTurnover.getSortOrder().add(tblTurnoverColDate);
                    tblTurnover.sort();
                }
        );
    }
}
