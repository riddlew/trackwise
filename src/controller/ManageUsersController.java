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
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.BasicIdName;
import model.User;
import java.net.URL;
import java.util.ResourceBundle;

public class ManageUsersController implements Initializable {
    /******************************************************************************
     * FXML                                                                       *
     ******************************************************************************/
    @FXML
    public TextField txtUsernameSearch;
    @FXML
    public Button btnNewUser;
    @FXML
    public TableView<User> tblUsers;
    @FXML
    public TableColumn<User, Integer> tblUsersColId;
    @FXML
    public TableColumn<User, String> tblUsersColUsername;
    @FXML
    public TableColumn<User, BasicIdName> tblUsersColShift;
    @FXML
    public TableColumn<User, BasicIdName> tblUsersColDepartment;
    @FXML
    public TableColumn<User, String> tblUsersColPermissionLevel;
    @FXML
    public TableColumn<User, Boolean> tblUsersColPasswordChange;
    @FXML
    public Button btnClose;

    /******************************************************************************
     * Observables                                                                *
     ******************************************************************************/
    ObservableList<User> users = FXCollections.observableArrayList();

    /******************************************************************************
     * Initialize                                                                 *
     ******************************************************************************/
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tblUsersColId.setCellValueFactory(new PropertyValueFactory<>("Id"));
        tblUsersColUsername.setCellValueFactory(new PropertyValueFactory<>("Username"));
        tblUsersColShift.setCellValueFactory(new PropertyValueFactory<>("Shift"));
        tblUsersColDepartment.setCellValueFactory(new PropertyValueFactory<>("Department"));
        tblUsersColPermissionLevel.setCellValueFactory(new PropertyValueFactory<>("PermissionLevel"));
        tblUsersColPermissionLevel.setCellValueFactory(f -> {
            if(f.getValue() != null) {
                return new SimpleStringProperty(PermissionHelper.getStringFromPermissionLevel(f.getValue().getPermissionLevel()));
            }

            return new SimpleStringProperty("");
        });
        tblUsersColPasswordChange.setCellValueFactory(new PropertyValueFactory<>("PasswordChange"));

        JDBC.openConnection();
        users = QueryManager.getUsers();
        JDBC.closeConnection();

        tblUsers.setItems(users);

        // When text changes in username search textfield, filter the Users and return any partion username matches.
        FilteredList<User> filteredUsers = new FilteredList<>(users, p -> true);
        txtUsernameSearch.textProperty().addListener((observable, oldValue, newValue) -> filteredUsers.setPredicate(
                user -> {
                   if(newValue == null || newValue.isEmpty()) {
                       return true;
                   }

                   String lowerCaseFilter = newValue.toLowerCase();
                   String lowerCaseUsername = user.getUsername().toLowerCase();

                    return lowerCaseUsername.contains(lowerCaseFilter);
                }
        ));
        SortedList<User> sortedUsers = new SortedList<>(filteredUsers);
        sortedUsers.comparatorProperty().bind(tblUsers.comparatorProperty());
        tblUsers.setItems(sortedUsers);

        tblUsers.setOnMouseClicked(this::handleCellClicked);
        btnNewUser.setOnAction(this::handleNewUsers);
        btnClose.setOnAction(this::handleClose);
    }

    /******************************************************************************
     * On Event - Cell Clicked in Users Table                                     *
     ******************************************************************************/
    public void handleCellClicked(MouseEvent mouseEvent) {
        User selectedUser = tblUsers.getSelectionModel().getSelectedItem();

        if(selectedUser == null) {
            return;
        }

        if(mouseEvent.getClickCount() == 2) {
            AddViewUserController controller = new AddViewUserController();
            Scene scene = SceneManager.createScene(controller, Strings.FXML_SETTINGS_ADD_VIEW_USER);
            setControllerCallback(controller);
            controller.populateData(selectedUser);
            SceneManager.showSceneInNewWindow( scene, Strings.TITLE_SETTINGS_EDIT_USER);
        }
    }

    /******************************************************************************
     * On Event - New User Button                                                 *
     ******************************************************************************/
    public void handleNewUsers(ActionEvent actionEvent) {
        AddViewUserController controller = new AddViewUserController();
        Scene scene = SceneManager.createScene(controller, Strings.FXML_SETTINGS_ADD_VIEW_USER);
        setControllerCallback(controller);
        SceneManager.showSceneInNewWindow( scene, Strings.TITLE_SETTINGS_ADD_USER);
    }

    /******************************************************************************
     * On Event - Close Button                                                    *
     ******************************************************************************/
    public void handleClose(ActionEvent actionEvent) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    /******************************************************************************
     * Set controller callback.                                                   *
     ******************************************************************************/
    private void setControllerCallback(AddViewUserController controller) {
        controller.setCallback(() -> {
            JDBC.openConnection();
            users = QueryManager.getUsers();
            JDBC.closeConnection();
            tblUsers.setItems(users);
        });
    }
}
