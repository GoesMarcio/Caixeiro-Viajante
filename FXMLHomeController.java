import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import java.util.Random;
import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
import javafx.stage.FileChooser;
import java.io.IOException;
import javafx.scene.control.MenuItem;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.text.DecimalFormat;
import javafx.scene.paint.Color;
import javafx.scene.control.SplitPane;
import javafx.util.Duration;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.input.ScrollEvent;
import javafx.event.EventHandler;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import javafx.scene.control.CheckBox;

public class FXMLHomeController implements Initializable {

    private Cidade[] cidades;
    private double melhorCaso;
    private Random rand = new Random();

    private double maxX, maxY = 0.0;
    private double bestCase = 0;
    private int it, itMAX;
    private boolean executePath;
    private Thread th;

    @FXML
    private Canvas canvas;

    @FXML
    private Pane zoomId;

    @FXML
    private Canvas canvasPoints;

    @FXML
    private SplitPane split;

    @FXML
    private MenuItem menuClose;

    @FXML
    private MenuItem menuLoad;

    @FXML
    private Button initStop;

    @FXML
    private Label distance;

    @FXML
    private Label status;

    @FXML
    private Label loading;

    @FXML
    private CheckBox checkPointsId;

    @FXML
    private CheckBox checkPathId;

    private GraphicsContext gc, gc2;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        it = 0;
        itMAX = 10000;
        executePath = false;
        
        gc = canvas.getGraphicsContext2D();
        gc2 = canvasPoints.getGraphicsContext2D();
        gc.setStroke(Color.web("#000000",0.8));
        
        // zoomId.setOnScroll(event -> {
        //     if(cidades != null){
                
                
        //             double facZoom = event.getDeltaY()/1000;
        //             gc.scale(1 + facZoom, 1 + facZoom);
        //             gc2.scale(1 + facZoom, 1 + facZoom);
                
        //             gc.clearRect(0, 0, 586, 457);
        //             gc2.clearRect(0, 0, 586, 457);
                
        //         //System.out.println(event.getY());
        //         draw();
        //         drawPoints();
        //     }
        // });

        zoomId.setOnScroll(event -> {       
            double zoom_fac = 1.05;
            double delta_y = event.getDeltaY();

            if(delta_y < 0) {
                zoom_fac = 2.0 - zoom_fac;
            }

            Scale newScale = new Scale();
            newScale.setPivotX(event.getX());
            newScale.setPivotY(event.getY());
            newScale.setX(canvas.getScaleX() * zoom_fac );
            newScale.setY(canvas.getScaleY() * zoom_fac );

            gc.scale(newScale.getX(), newScale.getY());
            draw();
            drawPoints();

            event.consume();
        });
        
    }

    @FXML
    void loadCities(ActionEvent event) throws IOException{
        try{
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Documentos texto (*.txt)", "*.txt");

            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(null);

            if (file != null) {
                
                Scanner sc = new Scanner(file);
                int n  = sc.nextInt();
                cidades = new Cidade[n];

                for(int i = 0; i<n; i++){
                    double x = Double.parseDouble(sc.next());
                    double y = Double.parseDouble(sc.next());
                    cidades[i] = new Cidade(x, y, sc.next());
                    if(x > maxX) maxX = x;
                    if(y > maxY) maxY = y;
                }
                
                //melhorCaso = distanciaTotal(cidades);
                sc.close();
                bestCase = distanciaTotal(cidades);
                loading.setText("");
                draw();
                drawPoints();

            }
        }catch(IOException e){
            
        }
    }

    // Bug com as threads e atualização da exibição do número da distância total
    @FXML
    void initStop(ActionEvent event) {
        if(cidades == null) return;
        if(executePath == false){
            executePath = true;
            status.setText("Status: Executando...");

            Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), new KeyValue(status.textFillProperty(), Color.web("#9f9f9f"))),
                new KeyFrame(Duration.seconds(1), new KeyValue(status.textFillProperty(), Color.GREEN))
            );
            timeline.setAutoReverse(true);
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();

            th = new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            execute();   
                        }
                    }
                    
                );
            th.start();
        }else{
            executePath = false;
            status.setText("Status: Parado");
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), new KeyValue(status.textFillProperty(), Color.web("#9f9f9f"))),
                new KeyFrame(Duration.seconds(0), new KeyValue(status.textFillProperty(), Color.web("#9f9f9f")))
            );
            timeline.setAutoReverse(true);
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
            th.interrupt();
        }
    }

    @FXML
    void close(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    void checkPoints(ActionEvent event) {
            drawPoints();
    }

    @FXML
    void checkPath(ActionEvent event) {
            draw();
    }

    public void draw(){
        Platform.runLater(()->{
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            System.out.println(canvas.getWidth());
            gc.setLineWidth(0.8);

            if(checkPathId.isSelected()){
                gc.beginPath();
                gc.moveTo(cidades[0].getX()/maxX * canvas.getWidth(), cidades[0].getY()/maxY * canvas.getHeight());
                for(int i = 1; i<cidades.length; i++){
                    gc.lineTo(cidades[i].getX()/maxX * canvas.getWidth(), cidades[i].getY()/maxY * canvas.getHeight());
                }
                gc.lineTo(cidades[0].getX()/maxX * canvas.getWidth(), cidades[0].getY()/maxY * canvas.getHeight());
                gc.stroke();
            }
            DecimalFormat df = new DecimalFormat("#.00");
            distance.setText(df.format(bestCase) + " km");
        });

        //System.out.println(bestCase);
    }

    public void drawPoints(){
        gc2.clearRect(0, 0, canvasPoints.getWidth(), canvasPoints.getHeight());

        if(!checkPointsId.isSelected()) return;

        gc2.setFill(Color.GREEN);
        for(int i = 0; i<cidades.length; i++){
            gc2.fillOval(cidades[i].getX()/maxX * canvas.getWidth(), cidades[i].getY()/maxY * canvas.getHeight(), 2, 2);
        }
    }


    //Algoritmos genéticos de troca de cidade 

    public void execute(){
        while(executePath){
            it++;
            int selecionaAlgoritmo = rand.nextInt(2);
            switch(selecionaAlgoritmo){
                case 0: swap(); 
                        break;
                case 1: moveFinal(); 
                        break;
                default: moveFinal();
            }
            if(it > itMAX){
                it = 0;
                draw();
            }
        }
    }

    public void swap(){
        Cidade[] aux = Arrays.copyOf(cidades, cidades.length);

        int i = rand.nextInt(cidades.length);
        int j = rand.nextInt(cidades.length);

        Cidade auxCidade = aux[i];
        aux[i] = aux[j];
        aux[j] = auxCidade;

        double d = distanciaTotal(aux);

        if(d < bestCase){
            cidades = Arrays.copyOf(aux, cidades.length);
            bestCase = d;
            //System.out.print(bestCase + "            \r");
        }
    }

    public void moveFinal(){
        Cidade[] aux = Arrays.copyOf(cidades, cidades.length);
        int i = rand.nextInt(cidades.length);
        Cidade cid = aux[i];

        for(int j = i; j < aux.length-1; j++){
            aux[j] = aux[j+1];
        }

        aux[aux.length-1] = cid;

        double d = distanciaTotal(aux);

        if(d < bestCase){
            cidades = Arrays.copyOf(aux, cidades.length);
            bestCase = d;
            //System.out.print(bestCase + "            \r");
        }
    }

    public double distanciaTotal(Cidade[] auxCidades){
        double aux = 0.0;
        for(int i = 0; i<auxCidades.length-1; i++){
            aux += auxCidades[i].calculaDistancia(auxCidades[i+1]);
        }
        aux += auxCidades[auxCidades.length-1].calculaDistancia(auxCidades[0]);

        return aux;
    }

}

