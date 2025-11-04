package smarthome.pojos;


public class Termostato extends DispositivoIoT {
    private double temperaturaAtual;
    private double temperaturaDesejada;
    private boolean status;

    public Termostato() {
        super();
    }

    public Termostato(String nome, String comodo, boolean status, double temperaturaAtual, double temperaturaDesejada) {
        // CORREÇÃO 3: Usando 'Termostato' como o tipo/modelo, assumindo que DispositivoIoT
        // recebe (String nome, String tipo/modelo, String comodo)
        super(nome, "Termostato", comodo); 
        this.status = status;
        this.temperaturaAtual = temperaturaAtual;
        this.temperaturaDesejada = temperaturaDesejada;
    }
    
    public double getTemperaturaAtual() { return temperaturaAtual; }
    public double getTemperaturaDesejada() { return temperaturaDesejada; }
    public boolean getStatus() { return status; }
    public void setTemperaturaAtual(double temperaturaAtual) { this.temperaturaAtual = temperaturaAtual; }
    public void setTemperaturaDesejada(double temperaturaDesejada) { this.temperaturaDesejada = temperaturaDesejada; }
    public void setStatus(boolean status) { this.status = status; }

    @Override
    public String toString() {
        return "Termostato{" + super.toString() + ", temperaturaAtual=" + temperaturaAtual + ", temperaturaDesejada=" + temperaturaDesejada + ", status=" + status + '}';
    }
}
