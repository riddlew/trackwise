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
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class PasswordController implements Initializable {
    /******************************************************************************
     * FXML                                                                       *
     ******************************************************************************/
    @FXML
    public PasswordField txtCurrentPassword;
    @FXML
    public PasswordField txtNewPassword;
    @FXML
    public PasswordField txtConfirmPassword;
    @FXML
    public Button btnSave;

    /******************************************************************************
     * Members                                                                    *
     ******************************************************************************/
    private String username;

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
        btnSave.setOnAction(this::handleSave);
    }

    /******************************************************************************
     * On Event - Save Button                                                     *
     ******************************************************************************/
    private void handleSave(ActionEvent actionEvent) {
        String currentPassword = txtCurrentPassword.getText().trim();
        String newPassword = txtNewPassword.getText().trim();
        String confirmPassword = txtConfirmPassword.getText().trim();

        if(currentPassword.isEmpty()) {
            Helpers.displayError("Missing current password", "Please enter your current password.");
            return;
        }

        if(newPassword.isEmpty()) {
            Helpers.displayError("Missing new password", "Please enter the new password.");
            return;
        }

        if(confirmPassword.isEmpty()) {
            Helpers.displayError("Missing new password confirmation", "Please enter the confirmation for the new password.");
            return;
        }

        if(!newPassword.equals(confirmPassword)) {
            Helpers.displayError("New password and new password confirmation do not match", "Please enter the same password for the new password and confirm new password fields.");
            return;
        }

        if(currentPassword.equals(newPassword)) {
            Helpers.displayError("Current password and new password are the same", "Please enter a different password for your new password.");
            return;
        }

        JDBC.openConnection();
        boolean isValidLogin = QueryManager.isValidLogin(username, currentPassword);
        if(!isValidLogin) {
            Helpers.displayError("Current password is not correct", "Please enter the correct current password.");
            JDBC.closeConnection();
            return;
        }

        QueryManager.updatePassword(username, currentPassword, newPassword);
        JDBC.closeConnection();

        this.callback.callback();
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
