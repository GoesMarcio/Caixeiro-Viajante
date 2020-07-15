public class Cidade{
    private double x, y;
    private String nome;

    public Cidade(double x, double y, String nome){
        this.x = x;
        this.y = y;
        this.nome = nome;
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public String getNome(){
        return nome;
    }

    public double calculaDistancia(Cidade c){
        double xA = x - c.getX();
        double yA = y - c.getY();
        if(xA < 0) xA = xA * (-1.0);
        if(yA < 0) yA = yA * (-1.0);

        return Math.sqrt((xA*xA) + (yA*yA));
    }
}