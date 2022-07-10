package controller;

import application.Helpers;
import database.JDBC;
import database.QueryManager;
import interfaces.ICallback;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.BasicIdName;

import java.net.URL;
import java.util.ResourceBundle;

public class AddViewDepartmentController implements Initializable {
    /******************************************************************************
     * FXML                                                                       *
     ******************************************************************************/
    @FXML
    public TextField txtName;
    @FXML
    public Button btnCancel;
    @FXML
    public Button btnSave;

    /******************************************************************************
     * Members                                                                    *
     ******************************************************************************/
    private BasicIdName selectedDepartment;

    /******************************************************************************
     * Interface Implementations                                                  *
     ******************************************************************************/
    private ICallback callback;

    public void setCallback(ICallback callback) {
        this.callback = callback;
    }

    /******************************************************************************
     * Initialize                                                                 *
     ******************************************************************************/
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnCancel.setOnAction(this::handleClose);
        btnSave.setOnAction(this::handleSave);
    }

    /******************************************************************************
     * On Event - Close Button                                                    *
     ******************************************************************************/
    public void handleClose(ActionEvent actionEvent) {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    /******************************************************************************
     * On Event - Save Button                                                     *
     ******************************************************************************/
    public void handleSave(ActionEvent actionEvent) {
        String name = txtName.getText().trim();

        if(name.isEmpty()) {
            Helpers.displayError("Invalid Name", "Please enter a name.");
            return;
        }

        JDBC.openConnection();
        boolean nameAvailable = QueryManager.checkDepartmentNameAvailable(name);

        if(selectedDepartment == null) {
            if(nameAvailable) {
                QueryManager.createDepartment(name);
            } else {
                JDBC.closeConnection();
                Helpers.displayError("Department Exists", "Please choose a different department name.");
                return;

            }
            JDBC.closeConnection();
            this.callback.callback();
        } else {
            if(nameAvailable) {
                selectedDepartment.setName(name);
                QueryManager.updateDepartment(selectedDepartment);
            } else {
                    JDBC.closeConnection();
                    Helpers.displayError("Department Exists", "Please choose a different department name.");
                    return;

            }
            JDBC.closeConnection();
            this.callback.callback();
        }

        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    /******************************************************************************
     * Populate department data                                                   *
     ******************************************************************************/
    public void populateData(BasicIdName department) {
        this.selectedDepartment = department;

        txtName.setText(department.getName());
    }
}
