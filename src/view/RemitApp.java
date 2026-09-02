package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RemitApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("remit.fxml"));
        primaryStage.setTitle("邮政电子汇兑系统‑JavaFX图形版");
        primaryStage.setScene(new Scene(root,1100,650));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
