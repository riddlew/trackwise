package controller;

import database.JDBC;
import database.QueryManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.BasicIdName;
import model.Downtime;
import model.Report;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ReportController implements Initializable {
    /******************************************************************************
     * FXML                                                                       *
     ******************************************************************************/
    @FXML
    public Label lblGenerated;
    @FXML
    public TableView<Report> tblReport;
    @FXML
    public TableColumn<Report, LocalDate> tblReportColDate;
    @FXML
    public TableColumn<Report, BasicIdName> tblReportColShift;
    @FXML
    public TableColumn<Report, Integer> tblReportColIssues;
    @FXML
    public Button btnClose;

    /******************************************************************************
     * Members                                                                    *
     ******************************************************************************/
    private ObservableList<Report> reports = FXCollections.observableArrayList();

    /******************************************************************************
     * Initialize                                                                 *
     ******************************************************************************/
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LocalDateTime ldt = LocalDateTime.now();
        DateTimeFormatter dtd = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter dtt = DateTimeFormatter.ofPattern("HH:ss a");
        lblGenerated.setText(String.format("Report generated on %s at %s.", ldt.format(dtd), ldt.format(dtt)));

        tblReportColDate.setCellValueFactory(new PropertyValueFactory<>("Date"));
        tblReportColShift.setCellValueFactory(new PropertyValueFactory<>("Shift"));
        tblReportColIssues.setCellValueFactory(new PropertyValueFactory<>("Issues"));

        JDBC.openConnection();
        reports = QueryManager.generateShiftReports();
        JDBC.closeConnection();

        tblReport.setItems(reports);
        btnClose.setOnAction(this::handleClose);
    }

    /******************************************************************************
     * On Event - Close Button                                                    *
     ******************************************************************************/
    public void handleClose(ActionEvent actionEvent) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}
