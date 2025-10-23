public class Lampada extends DispositivoIoT {
    private boolean ligada;
    private int intensidade;
    private int temperatura;

    public Lampada() {
        super();
    }

    public Lampada(String nome, String comodo, boolean online, boolean ligada, int intensidade, int temperatura) {
        super(nome, null, comodo);
        this.setOnline(online);
        this.ligada = false;
        this.intensidade = 0;
        this.temperatura = 0;
    }
    
    public boolean isLigada() { return ligada; }
    public int getIntensidade() { return intensidade; }
    public int getTemperatura() { return temperatura; }
    public void setLigada(boolean ligada) { this.ligada = ligada; }
    public void setIntensidade(int intensidade) { this.intensidade = intensidade; }
    public void setTemperatura(int temperatura) { this.temperatura = temperatura; }

    @Override
    public String toString() {
        return "Lampada{" + super.toString() + ", ligada=" + ligada + ", intensidade=" + intensidade + ", temperatura=" + temperatura + '}';
    }
}
