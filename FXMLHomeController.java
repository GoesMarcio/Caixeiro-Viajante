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
    private Canvas canvasPoints;

    @FXML
    private MenuItem menuClose;

    @FXML
    private MenuItem menuLoad;

    @FXML
    private Button initStop;

    @FXML
    private Label distance;

    private GraphicsContext gc;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        it = 0;
        itMAX = 100000;
        executePath = false;
        gc = canvas.getGraphicsContext2D();
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
                draw();
                drawPoints();

            }
        }catch(IOException e){
            
        }
    }

    @FXML
    void initStop(ActionEvent event) {
        if(executePath == false){
            executePath = true;
            System.out.println("aaaa");
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
            System.out.println("bbbb");
        }
    }

    @FXML
    void close(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    public void draw(){
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setLineWidth(1.0);
        gc.beginPath();
        gc.moveTo(cidades[0].getX()/maxX * canvas.getWidth(), cidades[0].getY()/maxY * canvas.getHeight());
        for(int i = 1; i<cidades.length; i++){
            gc.lineTo(cidades[i].getX()/maxX * canvas.getWidth(), cidades[i].getY()/maxY * canvas.getHeight());
        }
        gc.lineTo(cidades[0].getX()/maxX * canvas.getWidth(), cidades[0].getY()/maxY * canvas.getHeight());
        gc.stroke();

        DecimalFormat df = new DecimalFormat("#.00");
        distance.setText(df.format(bestCase) + " km");
        System.out.println(bestCase);
    }

    public void drawPoints(){
        GraphicsContext gc2 = canvasPoints.getGraphicsContext2D();
        gc2.clearRect(0, 0, canvasPoints.getWidth(), canvasPoints.getHeight());
        gc2.setFill(Color.GREEN);
        for(int i = 0; i<cidades.length; i++){
            gc2.fillOval(cidades[i].getX()/maxX * canvas.getWidth(), cidades[i].getY()/maxY * canvas.getHeight(), 2, 2);
        }
    }


    //Algoritmos de troca de cidade

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

