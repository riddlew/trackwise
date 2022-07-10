package controller;

import application.Helpers;
import components.TimeSpinnerValueFactory;
import database.JDBC;
import database.QueryManager;
import interfaces.INewEditIssueCallback;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.BasicIdName;
import model.Category;
import model.Issue;
import model.Subcategory;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class IssueController implements Initializable {
    /******************************************************************************
     * FXML                                                                       *
     ******************************************************************************/
    @FXML
    public Spinner<LocalTime> spinnerStart;
    @FXML
    public Spinner<LocalTime> spinnerEnd;
    @FXML
    public ChoiceBox<Category> choiceCategory;
    @FXML
    public ChoiceBox<Subcategory> choiceSubcategory;
    @FXML
    public TextArea txtNotes;
    @FXML
    public Button btnCancel;
    @FXML
    public Button btnSave;

    /******************************************************************************
     * Members                                                                    *
     ******************************************************************************/
    private int selectedIndex;
    private int issueId;
    private boolean newIssue = true;

    /******************************************************************************
     * Observables                                                                *
     ******************************************************************************/
    private ObservableList<Category> categories = FXCollections.observableArrayList();
    private ObservableList<Subcategory> subCategories = FXCollections.observableArrayList();

    /******************************************************************************
     * Interface Implementations                                                  *
     ******************************************************************************/
    private INewEditIssueCallback callback;

    public void setSaveEditCallback(
            Function<Issue, Boolean> newCallback,
            BiFunction<Issue, Integer, Boolean> editCallback
    ) {
        this.callback = new INewEditIssueCallback(){
            @Override
            public boolean saveNew(Issue issue) {
                return newCallback.apply(issue);
            }

            @Override
            public boolean saveChanges(Issue issue, int index) {
                return editCallback.apply(issue, index);
            }
        };
    }

    /******************************************************************************
     * Initialize                                                                 *
     ******************************************************************************/
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        spinnerStart.setValueFactory(new TimeSpinnerValueFactory());
        spinnerStart.setEditable(true);
        spinnerEnd.setValueFactory(new TimeSpinnerValueFactory());
        spinnerEnd.setEditable(true);

        if(newIssue) {
            JDBC.openConnection();
            categories = QueryManager.getCategories();
            JDBC.closeConnection();
        } else {
            choiceSubcategory.setItems(subCategories);
        }
        choiceCategory.setItems(categories);
        choiceCategory.setOnAction(this::onHandleChooseCategory);

        btnCancel.setOnAction(this::onHandleCancel);
        btnSave.setOnAction(this::onHandleSave);
    }

    /******************************************************************************
     * On Event - Select Category                                                 *
     ******************************************************************************/
    public void onHandleChooseCategory(ActionEvent actionEvent) {
        JDBC.openConnection();
        Category category = choiceCategory.getValue();
        subCategories = QueryManager.getSubcategories(category.getId());
        choiceSubcategory.setItems(subCategories);
        JDBC.closeConnection();
    }

    /******************************************************************************
     * On Event - Cancel Button                                                   *
     ******************************************************************************/
    public void onHandleCancel(ActionEvent actionEvent) {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    /******************************************************************************
     * On Event - Save Button                                                     *
     ******************************************************************************/
    public void onHandleSave(ActionEvent actionEvent) {
        LocalTime start = spinnerStart.getValue();
        LocalTime end = spinnerEnd.getValue();
        Category category = choiceCategory.getValue();
        Subcategory subcategory = choiceSubcategory.getValue();
        String notes = txtNotes.getText().trim();

        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

        if(start == null || end == null) {
            Helpers.displayError("Invalid Start or End time", "Please choose a valid start and end time with the 24-hour format HH:mm, such as 07:30 or 15:00.");
            return;
        }

        if(category == null || subcategory == null) {
            Helpers.displayError("Invalid category or subcategory", "Please choose a valid category and subcategory");
            return;
        }

        Issue issue = new Issue(issueId, LocalTime.parse(start.format(dtf)), LocalTime.parse(end.format(dtf)), category, subcategory, notes);
        Stage stage = (Stage) btnCancel.getScene().getWindow();

        if(newIssue) {
            if(!this.callback.saveNew(issue)) {
                Helpers.displayError("Time overlaps with existing issue", "The selected start and end time overlaps with an existing issue.");
                return;
            }
        } else {
            if(!this.callback.saveChanges(issue, this.selectedIndex)) {
                Helpers.displayError("Time overlaps with existing issue", "The selected start and end time overlaps with an existing issue.");
                return;
            }
        }
        stage.close();
    }

    /******************************************************************************
     * Populate user data                                                         *
     ******************************************************************************/
    public void populateData(int index, Issue issue) {
        newIssue = false;
        this.selectedIndex = index;

        JDBC.openConnection();
        categories = QueryManager.getCategories();
        subCategories = QueryManager.getSubcategories(issue.getSubcategory().getCategoryId());
        JDBC.closeConnection();

        spinnerStart.getValueFactory().setValue(issue.getStartTime());
        spinnerEnd.getValueFactory().setValue(issue.getEndTime());

        if(categories.contains(issue.getCategory())) {
            choiceCategory.getSelectionModel().select(issue.getCategory());
        }

        if(subCategories.contains(issue.getSubcategory())) {
            choiceSubcategory.getSelectionModel().select(issue.getSubcategory());
        }
        txtNotes.setText(issue.getNotes());
    }

    /******************************************************************************
     * Set the issue id                                                           *
     ******************************************************************************/
    public void setId(int id) {
        this.issueId = id;
    }
}
