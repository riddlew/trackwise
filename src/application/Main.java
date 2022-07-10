package application;

import controller.LoginController;
import database.JDBC;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
//        JDBC.openConnection();
//        JDBC.setup();
//        JDBC.closeConnection();
        LoginController controller = new LoginController();
        Scene scene = SceneManager.createScene(controller, Strings.FXML_LOGIN);
        SceneManager.showScene(stage, scene, Strings.TITLE_LOGIN);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
