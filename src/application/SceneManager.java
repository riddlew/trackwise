package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class SceneManager {
    public static Stage getStageFromActionEvent(ActionEvent actionEvent) {
        return ((Stage) ((Node) actionEvent.getSource()).getScene().getWindow());
    }

    public static void createLoader(Object cl, String url) {
        FXMLLoader loader = new FXMLLoader(cl.getClass().getResource(url));
        loader.setController(cl);
    }

    public static Scene createScene(Object cl, String url) {
        try {
            FXMLLoader loader = new FXMLLoader(cl.getClass().getResource(url));
            loader.setController(cl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            return scene;
        } catch(IOException e) {
            Helpers.displayGenericError();
            Helpers.log("ERROR - SceneManager.createScene - " + e);
        }
        return null;
    }

    public static void showScene(Stage stage, Scene scene, String title) {
        if(scene == null) return;

        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }

    public static void showSceneInNewWindow(Scene scene, String title) {
        if(scene == null) return;

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }
}
