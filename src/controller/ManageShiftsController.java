package controller;

import application.SceneManager;
import application.Strings;
import database.JDBC;
import database.QueryManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.BasicIdName;

import java.net.URL;
import java.util.ResourceBundle;

public class ManageShiftsController implements Initializable {
    /******************************************************************************
     * FXML                                                                       *
     ******************************************************************************/
    @FXML
    public TableView<BasicIdName> tblDepartments;
    @FXML
    public TableColumn<BasicIdName, Integer> tblDepartmentsColId;
    @FXML
    public TableColumn<BasicIdName, String> tblDepartmentsColCategory;
    @FXML
    public Button btnNew;
    @FXML
    public Button btnDelete;
    @FXML
    public Button btnClose;

    /******************************************************************************
     * Members                                                                   *
     ******************************************************************************/
    private BasicIdName selectedShifts;

    /******************************************************************************
     * Observables                                                                *
     ******************************************************************************/
    ObservableList<BasicIdName> shifts = FXCollections.observableArrayList();

    /******************************************************************************
     * Initialize                                                                 *
     ******************************************************************************/
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tblDepartmentsColId.setCellValueFactory(new PropertyValueFactory<>("Id"));
        tblDepartmentsColCategory.setCellValueFactory(new PropertyValueFactory<>("Name"));

        JDBC.openConnection();
        shifts = QueryManager.getShifts();
        JDBC.closeConnection();

        tblDepartments.setItems(shifts);
        tblDepartments.setOnMouseClicked(this::handleCategoryCellClicked);

        btnNew.setOnAction(this::handleNewCategory);
        btnDelete.setOnAction(this::handleDeleteCategory);
        btnClose.setOnAction(this::handleClose);
    }

    /******************************************************************************
     * On Event - Cell Clicked in Categories Table                                *
     ******************************************************************************/
    public void handleCategoryCellClicked(MouseEvent mouseEvent) {
        selectedShifts = tblDepartments.getSelectionModel().getSelectedItem();

        if(selectedShifts == null) {
            btnDelete.setDisable(true);
            return;
        }

        btnDelete.setDisable(false);

        if(mouseEvent.getClickCount() == 2) {
            AddViewShiftController controller = new AddViewShiftController();
            Scene scene = SceneManager.createScene(controller, Strings.FXML_SETTINGS_ADD_VIEW_CATEGORY);
            controller.setCallback(() -> {
                JDBC.openConnection();
                shifts.clear();
                shifts.addAll(QueryManager.getShifts());
                JDBC.closeConnection();
                btnDelete.setDisable(true);
            });
            controller.populateData(selectedShifts);
            SceneManager.showSceneInNewWindow( scene, Strings.TITLE_SETTINGS_EDIT_SHIFT);
        }
    }

    /******************************************************************************
     * On Event - New Button                                                      *
     ******************************************************************************/
    public void handleNewCategory(ActionEvent actionEvent) {
        AddViewShiftController controller = new AddViewShiftController();
        Scene scene = SceneManager.createScene(controller, Strings.FXML_SETTINGS_ADD_VIEW_CATEGORY);
        controller.setCallback(() -> {
            JDBC.openConnection();
            shifts.clear();
            shifts.addAll(QueryManager.getShifts());
            JDBC.closeConnection();
            btnDelete.setDisable(true);
        });
        SceneManager.showSceneInNewWindow( scene, Strings.TITLE_SETTINGS_ADD_SHIFT);
    }

    /******************************************************************************
     * On Event - Delete Button                                                   *
     ******************************************************************************/
    public void handleDeleteCategory(ActionEvent actionEvent) {
        JDBC.openConnection();
        QueryManager.deleteShift(selectedShifts.getId());
        shifts.clear();
        shifts.addAll(QueryManager.getShifts());
        JDBC.closeConnection();
        btnDelete.setDisable(true);
    }

    /******************************************************************************
     * On Event - Close Button                                                    *
     ******************************************************************************/
    public void handleClose(ActionEvent actionEvent) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}
