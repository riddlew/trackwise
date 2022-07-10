package controller;

import application.*;
import database.JDBC;
import database.QueryManager;
import interfaces.ICallback;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    /******************************************************************************
     * FXML                                                                       *
     ******************************************************************************/
    @FXML
    public TextField txtUsername;
    @FXML
    public PasswordField txtPassword;
    @FXML
    public Button btnLogin;

    /******************************************************************************
     * Initialize                                                                 *
     ******************************************************************************/
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnLogin.setOnAction(this::handleLogin);
    }

    /******************************************************************************
     * On Event - Login Button                                                    *
     ******************************************************************************/
    public void handleLogin(ActionEvent actionEvent) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if(username.isBlank() || password.isBlank()) {
            Helpers.displayError("Missing Input", "Username and Password must not be blank.");
            return;
        }

        JDBC.openConnection();
        boolean isValidLogin = QueryManager.isValidLogin(username, password);
        int permissionLevel = QueryManager.getPermissionLevel(username, password);
        boolean requiresPasswordChange = QueryManager.checkPasswordChange(username, password);
        JDBC.closeConnection();

        if (isValidLogin) {
            if(requiresPasswordChange) {
                PasswordController controller = new PasswordController();
                Scene scene = SceneManager.createScene(controller, Strings.FXML_CHANGE_PASSWORD);
                controller.setUsername(username);
                controller.setCallback(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Password Changed");
                    alert.setHeaderText("Password Changed");
                    alert.setContentText("Your password has been successfully changed.");
                    alert.showAndWait();
                });
                SceneManager.showSceneInNewWindow(scene, Strings.TITLE_CHANGE_PASSWORD);
            } else {
                PermissionHelper.setPermissionLevel(permissionLevel);
                MainController controller = new MainController();
                Stage stage = SceneManager.getStageFromActionEvent(actionEvent);
                Scene scene = SceneManager.createScene(controller, Strings.FXML_MAIN);
                SceneManager.showScene(stage, scene, Strings.TITLE_MAIN);
            }
        } else {
            Helpers.displayError("Invalid Username or Password", "Verify both the username and the password are correct.");
            Helpers.log("Failed user login attempt -  username: " + username + ".");
        }
    }

}
