import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.lang.Exception;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.image.Image;

public class CaixeiroViajante extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        try{
            Parent root = FXMLLoader.load(getClass().getResource("interface.fxml"));
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            //stage.initStyle(StageStyle.UNDECORATED);
            stage.getIcons().add(new Image(this.getClass().getResourceAsStream("assets/icon.png")));
            stage.setTitle("Caixeiro Viajante");
            //stage.setResizable(false);
            stage.show();
            stage.setOnCloseRequest(e -> System.exit(0));
        }catch(Exception e){
            System.out.println("Erro ao iniciar programa");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
