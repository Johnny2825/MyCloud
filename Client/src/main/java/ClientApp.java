import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/sample.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Cloud Client");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setResizable(false);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }
}
