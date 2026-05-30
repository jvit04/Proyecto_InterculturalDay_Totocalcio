package application;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.transform.Scale;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import utilities.Paths;
import javafx.scene.Group;
import javafx.scene.image.Image;
import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    public static void main(String[] args) {

        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        // 1. Cargamos el panel original desde el FXML
        StackPane loader = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(Paths.TotocalcioApp)));

        // 2. Envolvemos el loader en un Group para que recalcule los bordes con el zoom aplicado
        Group contenedorGrupo = new Group(loader);

        // 3. Creamos un StackPane como raíz de la escena que se encargará de centrar el grupo
        StackPane root = new StackPane(contenedorGrupo);
        root.setStyle("-fx-background-color: #2a5f90;"); // Mantenemos el color azul de fondo para los bordes sobrantes

        // 4. Inicializamos la escena con nuestro contenedor raíz
        Scene scene = new Scene(root);
        stage.setScene(scene);

        // Configuraciones de la ventana e íconos
        stage.setResizable(false);
        Image icono = new Image(Objects.requireNonNull(getClass().getResourceAsStream(Paths.ImagenIconoApp)));
        stage.getIcons().add(icono);
        stage.setTitle("Totocalcio App");

        // Configuraciones de pantalla completa
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        // 5. Configuramos la herramienta de escalado directamente sobre el 'loader'
        Scale scaleTransform = new Scale();
        scaleTransform.setPivotX(0);
        scaleTransform.setPivotY(0);
        loader.getTransforms().add(scaleTransform);

        // Listener para vigilar los cambios en el ancho de la pantalla
        scene.widthProperty().addListener((observable, oldVal, newVal) -> {
            double factorAncho = newVal.doubleValue() / 1280.0;
            double factorAlto = scene.getHeight() / 800.0;
            double factorFinal = Math.min(factorAncho, factorAlto);

            scaleTransform.setX(factorFinal);
            scaleTransform.setY(factorFinal);
        });

        // Listener para vigilar los cambios en el alto de la pantalla
        scene.heightProperty().addListener((observable, oldVal, newVal) -> {
            double factorAncho = scene.getWidth() / 1280.0;
            double factorAlto = newVal.doubleValue() / 800.0;
            double factorFinal = Math.min(factorAncho, factorAlto);

            scaleTransform.setX(factorFinal);
            scaleTransform.setY(factorFinal);
        });

        stage.show();
    }
}