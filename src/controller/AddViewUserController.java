package controller;

import application.Helpers;
import application.PermissionHelper;
import database.JDBC;
import database.QueryManager;
import interfaces.ICallback;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.BasicIdName;
import model.User;
import java.net.URL;
import java.util.ResourceBundle;

public class AddViewUserController implements Initializable {
    /******************************************************************************
     * FXML                                                                       *
     ******************************************************************************/
    @FXML
    public TextField txtUsername;
    @FXML
    public PasswordField txtPassword;
    @FXML
    public ChoiceBox<BasicIdName> choiceDepartment;
    @FXML
    public ChoiceBox<BasicIdName> choiceShift;
    @FXML
    public ChoiceBox<String> choicePermission;
    @FXML
    public CheckBox checkPasswordChange;
    @FXML
    public Button btnDelete;
    @FXML
    public Button btnCancel;
    @FXML
    public Button btnSave;

    /******************************************************************************
     * Members                                                                    *
     ******************************************************************************/
    private boolean newUser = true;
    private User selectedUser;

    /******************************************************************************
     * Observables                                                                *
     ******************************************************************************/
    private ObservableList<BasicIdName> departments = FXCollections.observableArrayList();
    private ObservableList<BasicIdName> shifts = FXCollections.observableArrayList();
    private ObservableList<String> permissions = FXCollections.observableArrayList(
            PermissionHelper.getStringFromPermissionLevel(PermissionHelper.LEVEL_ADMIN),
            PermissionHelper.getStringFromPermissionLevel(PermissionHelper.LEVEL_USER)
    );

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
        JDBC.openConnection();
        departments = QueryManager.getDepartments();
        shifts = QueryManager.getShifts();
        JDBC.closeConnection();

        choiceDepartment.setItems(departments);
        choiceShift.setItems(shifts);
        choicePermission.setItems(permissions);

        btnDelete.setOnAction(this::handleDelete);
        btnCancel.setOnAction(this::handleClose);
        btnSave.setOnAction(this::handleSave);
    }

    /******************************************************************************
     * On Event - Delete Button                                                   *
     ******************************************************************************/
    public void handleDelete(ActionEvent actionEvent) {
        JDBC.openConnection();
        QueryManager.deleteUser(selectedUser.getId());
        JDBC.closeConnection();

        this.callback.callback();
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
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
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        BasicIdName department = choiceDepartment.getValue();
        BasicIdName shift = choiceShift.getValue();
        String permissionString = choicePermission.getValue();
        boolean passwordChangeRequired = checkPasswordChange.isSelected();

        if(username.isEmpty()) {
            Helpers.displayError("Invalid Username", "Please enter a username.");
           return;
        }

        if(newUser) {
            if(password.isEmpty()) {
                Helpers.displayError("Invalid Password", "Please enter a new password.");
            }
        }

        if(department == null) {
            Helpers.displayError("Invalid Department", "Please choose a department.");
           return;
        }

        if(shift == null) {
            Helpers.displayError("Invalid Shift", "Please choose a shift.");
            return;
        }

        if(permissionString == null) {
            Helpers.displayError("Invalid Permission", "Please choose a permission.");
            return;
        }

        int permissionInt = PermissionHelper.getIntFromPermissionString(permissionString);

        if(newUser) {
            JDBC.openConnection();
            boolean usernameAvailable = QueryManager.checkUsernameAvailable(username);
            if(usernameAvailable) {
                QueryManager.createUser(
                        department.getId(),
                        shift.getId(),
                        permissionInt,
                        username,
                        password,
                        passwordChangeRequired
                );
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                Helpers.displayError("Username Taken", "Please choose a different username.");
                return;
            }
            JDBC.closeConnection();

            this.callback.callback();
        } else {
            JDBC.openConnection();
            QueryManager.updateUser(
                    selectedUser.getId(),
                    department.getId(),
                    shift.getId(),
                    permissionInt,
                    password,
                    passwordChangeRequired
            );
            JDBC.closeConnection();

            this.callback.callback();
        }

        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    /******************************************************************************
     * Populate user data                                                         *
     ******************************************************************************/
    public void populateData(User user) {
        this.newUser = false;
        this.selectedUser = user;
        this.btnDelete.setDisable(false);
        this.txtUsername.setDisable(true);

        txtUsername.setText(user.getUsername());
        if(departments.contains(user.getDepartment())) {
            choiceDepartment.getSelectionModel().select(user.getDepartment());
        }

        if(shifts.contains(user.getShift())) {
            choiceShift.getSelectionModel().select(user.getShift());
        }

        String permissionString = PermissionHelper.getStringFromPermissionLevel(user.getPermissionLevel());
        if(permissions.contains(permissionString)) {
            choicePermission.getSelectionModel().select(permissionString);
        }

        if(user.getPasswordChange()) {
            checkPasswordChange.setSelected(true);
        }
    }
}
