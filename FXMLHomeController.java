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
import javafx.scene.control.CheckBox;
import javafx.scene.transform.Affine;
import javafx.geometry.Point2D;

public class FXMLHomeController implements Initializable {

    private Cidade[] cidades;
    private double melhorCaso;
    private Random rand = new Random();

    private double maxX, maxY = 0.0;
    private double factorZoom;
    private double bestCase = 0;
    private int it, itMAX;
    private boolean executePath;
    private Thread th;

    private double orgX, orgY;

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
        factorZoom = 1.0;
        
        gc = canvas.getGraphicsContext2D();
        gc2 = canvasPoints.getGraphicsContext2D();
        gc.setStroke(Color.web("#000000",0.8));
        gc.setLineWidth(0.8);

        zoomId.setOnScroll(event -> {
            if(cidades == null) return;

            Affine transform = gc.getTransform();

            double delta = 1.1;
            double scale = canvas.getScaleX(); // currently we only use Y, same value is used for X

            if (event.getDeltaY() < 0){
                if(transform.getMxx() < 0.5) return;
                scale /= delta;
                factorZoom -= 0.1;
            }else{
                if(transform.getMxx() > 4) return;
                scale *= delta;
                factorZoom += 0.1;
            }
            
            clearCanvas();

            transform.appendScale(scale, scale, new Point2D(event.getX(), event.getY()));
            clearCanvas();
            gc.setTransform(transform);
            gc2.setTransform(transform);
            
            draw();
            drawPoints();

            event.consume();
        });

        zoomId.setOnMousePressed((event) -> {
            orgX = event.getSceneX();
            orgY = event.getSceneY();
        });
        zoomId.setOnMouseDragged(event -> {
            if(cidades == null) return;
            double changeInX = event.getSceneX() - orgX;
            double changeInY = event.getSceneY() - orgY;

            Affine transform = gc.getTransform();
            transform.appendTranslation(changeInX, changeInY);
            clearCanvas();
            gc.setTransform(transform);
            gc2.setTransform(transform);

            draw();
            drawPoints();

            orgX = event.getSceneX();
            orgY = event.getSceneY();
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
                clearCanvas();
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
    void close(ActionEvent event){
        Platform.exit();
        System.exit(0);
    }

    @FXML
    void checkPoints(ActionEvent event){
        gc2.clearRect(-10, -10, canvas.getWidth() + 20, canvas.getHeight() + 20);
        drawPoints();
    }

    @FXML
    void checkPath(ActionEvent event){
        gc.clearRect(-10, -10, canvas.getWidth() + 20, canvas.getHeight() + 20);
        draw();
    }

    public void clearCanvas(){
        gc.clearRect(-20, -20, canvas.getWidth() + 20, canvas.getHeight() + 20);
        gc2.clearRect(-10, -10, canvas.getWidth() + 20, canvas.getHeight() + 20);
    }

    public void draw(){
        Platform.runLater(()->{
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
        if(!checkPointsId.isSelected()) return;

        gc2.setFill(Color.GREEN);
        for(int i = 0; i<cidades.length; i++){
            gc2.fillOval((cidades[i].getX()/maxX * canvas.getWidth()) - 1, (cidades[i].getY()/maxY * canvas.getHeight()) - 1, 2, 2);
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
                gc.clearRect(-20, -20, canvas.getWidth() + 20, canvas.getHeight() + 20);
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

