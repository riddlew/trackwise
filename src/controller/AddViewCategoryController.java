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

public class AddViewCategoryController implements Initializable {
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
    private boolean newCategory = true;
    private BasicIdName selectedCategory;
    private BasicIdName selectedSubcategory;
    private CategoryType categoryType;

    /******************************************************************************
     * Interface Implementations                                                  *
     ******************************************************************************/
    private ICallback callback;

    public void setCallback(ICallback callback) {
        this.callback = callback;
    }

    /******************************************************************************
     * Enums                                                                      *
     ******************************************************************************/
    public enum CategoryType {
        CATEGORY,
        SUBCATEGORY
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

        if(categoryType == CategoryType.CATEGORY) {
            JDBC.openConnection();
            boolean categoryAvailable = QueryManager.checkCategoryNameAvailable(name);

            if(newCategory) {
                if(categoryAvailable) {
                    QueryManager.createCategory(name);
                } else {
                    JDBC.closeConnection();
                    Helpers.displayError("Category Exists", "Please choose a different category name.");
                    return;

                }
                JDBC.closeConnection();
                this.callback.callback();
            } else {
                if(categoryAvailable) {
                    selectedCategory.setName(name);
                    QueryManager.updateCategory(selectedCategory);
                } else {
                    JDBC.closeConnection();
                    Helpers.displayError("Category Exists", "Please choose a different category name.");
                    return;

                }
                JDBC.closeConnection();
                this.callback.callback();
            }
        } else {
            JDBC.openConnection();
            boolean subcategoryAvailable = QueryManager.checkSubcategoryNameAvailable(name, selectedCategory.getId());

            if(newCategory) {
                if(subcategoryAvailable) {
                    QueryManager.createSubcategory(name, selectedCategory.getId());
                } else {
                    JDBC.closeConnection();
                    Helpers.displayError("Subcategory Exists", "Please choose a different subcategory name.");
                    return;
                }
                JDBC.closeConnection();
                this.callback.callback();
            } else if(this.selectedCategory != null) {
                if(subcategoryAvailable) {
                    selectedSubcategory.setName(name);
                    QueryManager.updateSubcategory(selectedSubcategory, selectedCategory.getId());
                } else {
                    JDBC.closeConnection();
                    Helpers.displayError("Subcategory Exists", "Please choose a different subcategory name.");
                    return;
                }
                JDBC.closeConnection();
                this.callback.callback();
            }
        }

        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    /******************************************************************************
     * Populate category data                                                     *
     ******************************************************************************/
    public void populateCategoryData(BasicIdName category) {
        this.newCategory = false;
        this.selectedCategory = category;

        txtName.setText(category.getName());
    }

    /******************************************************************************
     * Populate subcategory data                                                  *
     ******************************************************************************/
    public void populateSubcategoryData(BasicIdName category) {
        this.newCategory = false;
        this.selectedSubcategory = category;

        txtName.setText(category.getName());
    }

    /******************************************************************************
     * Set type of field.                                                        *
     ******************************************************************************/
    public void setType(CategoryType type) {
        this.categoryType = type;
    }

    /******************************************************************************
     * Set selected category                                                     *
     ******************************************************************************/
    public void setCategory(BasicIdName category) {
        this.selectedCategory = category;
    }

    /******************************************************************************
     * Set selected subcategory                                                  *
     ******************************************************************************/
    public void setSubcategory(BasicIdName subcategory) {
        this.selectedSubcategory = subcategory;
    }
}
