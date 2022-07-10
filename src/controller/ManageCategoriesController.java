package controller;

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
import model.Category;
import model.Subcategory;
import model.User;

import java.net.URL;
import java.util.ResourceBundle;

public class ManageCategoriesController implements Initializable {
    /******************************************************************************
     * FXML                                                                       *
     ******************************************************************************/
    @FXML
    public TableView<Category> tblCategories;
    @FXML
    public TableColumn<Category, Integer> tblCategoriesColId;
    @FXML
    public TableColumn<Category, String> tblCategoriesColCategory;
    @FXML
    public TableView<Subcategory> tblSubcategories;
    @FXML
    public TableColumn<Subcategory, Integer> tblSubcategoriesColId;
    @FXML
    public TableColumn<Subcategory, String> tblSubcategoriesColSubcategory;
    @FXML
    public Button btnNewCategory;
    @FXML
    public Button btnDeleteCategory;
    @FXML
    public Button btnNewSubcategory;
    @FXML
    public Button btnDeleteSubcategory;
    @FXML
    public Button btnClose;

    /******************************************************************************
     * Members                                                                   *
     ******************************************************************************/
    private Category selectedCategory;
    private Subcategory selectedSubcategory;

    /******************************************************************************
     * Observables                                                                *
     ******************************************************************************/
    ObservableList<Category> categories = FXCollections.observableArrayList();
    ObservableList<Subcategory> subcategories = FXCollections.observableArrayList();

    /******************************************************************************
     * Initialize                                                                 *
     ******************************************************************************/
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tblCategoriesColId.setCellValueFactory(new PropertyValueFactory<>("Id"));
        tblCategoriesColCategory.setCellValueFactory(new PropertyValueFactory<>("Name"));
        tblSubcategoriesColId.setCellValueFactory(new PropertyValueFactory<>("Id"));
        tblSubcategoriesColSubcategory.setCellValueFactory(new PropertyValueFactory<>("Name"));

        JDBC.openConnection();
        categories = QueryManager.getCategories();
        JDBC.closeConnection();

        tblCategories.setItems(categories);

        tblCategories.setOnMouseClicked(this::handleCategoryCellClicked);
        tblSubcategories.setOnMouseClicked(this::handleSubcategoryCellClicked);

        btnNewCategory.setOnAction(this::handleNewCategory);
        btnDeleteCategory.setOnAction(this::handleDeleteCategory);
        btnNewSubcategory.setOnAction(this::handleNewSubcategory);
        btnDeleteSubcategory.setOnAction(this::handleDeleteSubcategory);
        btnClose.setOnAction(this::handleClose);
    }

    /******************************************************************************
     * On Event - Cell Clicked in Categories Table                                *
     ******************************************************************************/
    public void handleCategoryCellClicked(MouseEvent mouseEvent) {
        selectedCategory = tblCategories.getSelectionModel().getSelectedItem();

        if(selectedCategory == null) {
            btnDeleteCategory.setDisable(true);
            btnDeleteSubcategory.setDisable(true);
            btnNewSubcategory.setDisable(true);
            subcategories.clear();
            return;
        }

        btnDeleteCategory.setDisable(false);
        btnNewSubcategory.setDisable(false);

        JDBC.openConnection();
        subcategories = QueryManager.getSubcategories(selectedCategory.getId());
        JDBC.closeConnection();

        tblSubcategories.setItems(subcategories);

        if(mouseEvent.getClickCount() == 2) {
            AddViewCategoryController controller = new AddViewCategoryController();
            Scene scene = SceneManager.createScene(controller, Strings.FXML_SETTINGS_ADD_VIEW_CATEGORY);
            controller.setType(AddViewCategoryController.CategoryType.CATEGORY);
            controller.setCategory(selectedCategory);
            controller.setCallback(() -> {
                JDBC.openConnection();
                categories.clear();
                categories.addAll(QueryManager.getCategories());
                JDBC.closeConnection();
                btnDeleteCategory.setDisable(true);
            });
            controller.populateCategoryData(selectedCategory);
            SceneManager.showSceneInNewWindow( scene, Strings.TITLE_SETTINGS_EDIT_CATEGORY);
        }
    }

    /******************************************************************************
     * On Event - Cell Clicked in Subcategories Table                             *
     ******************************************************************************/
    public void handleSubcategoryCellClicked(MouseEvent mouseEvent) {
        selectedCategory = tblCategories.getSelectionModel().getSelectedItem();
        selectedSubcategory = tblSubcategories.getSelectionModel().getSelectedItem();

        if(selectedSubcategory == null) {
            btnDeleteSubcategory.setDisable(true);
            btnNewSubcategory.setDisable(false);
            return;
        }

        btnDeleteSubcategory.setDisable(false);

        if(mouseEvent.getClickCount() == 2) {
            AddViewCategoryController controller = new AddViewCategoryController();
            Scene scene = SceneManager.createScene(controller, Strings.FXML_SETTINGS_ADD_VIEW_CATEGORY);
            controller.setType(AddViewCategoryController.CategoryType.SUBCATEGORY);
            controller.setCategory(selectedCategory);
            controller.setSubcategory(selectedSubcategory);
            controller.setCallback(() -> {
                JDBC.openConnection();
                subcategories.clear();
                subcategories.addAll(QueryManager.getSubcategories(selectedCategory.getId()));
                JDBC.closeConnection();
                btnDeleteSubcategory.setDisable(true);
            });
            controller.populateSubcategoryData(selectedSubcategory);
            SceneManager.showSceneInNewWindow( scene, Strings.TITLE_SETTINGS_EDIT_SUBCATEGORY);
        }
    }

    /******************************************************************************
     * On Event - New Category Button                                             *
     ******************************************************************************/
    public void handleNewCategory(ActionEvent actionEvent) {
        AddViewCategoryController controller = new AddViewCategoryController();
        Scene scene = SceneManager.createScene(controller, Strings.FXML_SETTINGS_ADD_VIEW_CATEGORY);
        controller.setType(AddViewCategoryController.CategoryType.CATEGORY);
        controller.setCallback(() -> {
            JDBC.openConnection();
            categories.clear();
            categories.addAll(QueryManager.getCategories());
            JDBC.closeConnection();
            btnDeleteCategory.setDisable(true);
        });
        SceneManager.showSceneInNewWindow( scene, Strings.TITLE_SETTINGS_ADD_CATEGORY);
    }

    /******************************************************************************
     * On Event - Delete Category Button                                          *
     ******************************************************************************/
    public void handleDeleteCategory(ActionEvent actionEvent) {
        Category selectedCategory = tblCategories.getSelectionModel().getSelectedItem();

        JDBC.openConnection();
        QueryManager.deleteCategory(selectedCategory.getId());
        categories.clear();
        categories.addAll(QueryManager.getCategories());
        subcategories.clear();
        JDBC.closeConnection();

        if(categories.size() < 1) {
            btnDeleteCategory.setDisable(true);
        }
    }

    /******************************************************************************
     * On Event - New Subcategory Button                                          *
     ******************************************************************************/
    public void handleNewSubcategory(ActionEvent actionEvent) {
        AddViewCategoryController controller = new AddViewCategoryController();
        Scene scene = SceneManager.createScene(controller, Strings.FXML_SETTINGS_ADD_VIEW_CATEGORY);
        controller.setType(AddViewCategoryController.CategoryType.SUBCATEGORY);
        controller.setCategory(selectedCategory);
        controller.setCallback(() -> {
            JDBC.openConnection();
            subcategories.clear();
            subcategories.addAll(QueryManager.getSubcategories(selectedCategory.getId()));
            JDBC.closeConnection();
            btnDeleteSubcategory.setDisable(true);
        });
        SceneManager.showSceneInNewWindow( scene, Strings.TITLE_SETTINGS_ADD_SUBCATEGORY);
    }

    /******************************************************************************
     * On Event - Delete Subcategory Button                                       *
     ******************************************************************************/
    public void handleDeleteSubcategory(ActionEvent actionEvent) {
        Category selectedCategory = tblCategories.getSelectionModel().getSelectedItem();
        selectedSubcategory = tblSubcategories.getSelectionModel().getSelectedItem();

        JDBC.openConnection();
        QueryManager.deleteSubcategory(selectedSubcategory.getId());
        subcategories.clear();
        subcategories.addAll(QueryManager.getSubcategories(selectedCategory.getId()));
        JDBC.closeConnection();
        tblSubcategories.setItems(subcategories);

        if(subcategories.size() < 1) {
            btnDeleteSubcategory.setDisable(true);
        }
    }

    /******************************************************************************
     * On Event - Close Button                                                    *
     ******************************************************************************/
    public void handleClose(ActionEvent actionEvent) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}
