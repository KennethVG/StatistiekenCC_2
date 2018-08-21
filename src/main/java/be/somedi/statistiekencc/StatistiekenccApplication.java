package be.somedi.statistiekencc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class StatistiekenccApplication extends Application {

    private ConfigurableApplicationContext context;
    private Parent root;

    public static void main(String[] args) {
        launch(StatistiekenccApplication.class, args);
    }

    @Override
    public void init() throws Exception {
        context = SpringApplication.run(StatistiekenccApplication.class);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Statistieken.fxml"));
        loader.setControllerFactory(context::getBean);
        root = loader.load();
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        primaryStage.setResizable(true);
        primaryStage.setTitle("Statistieken Applicatie");
        primaryStage.show();
    }

    @Override
    public void stop() {
        context.stop();
    }
}