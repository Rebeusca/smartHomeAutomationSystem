package smarthome.pojos;

public class Camera extends DispositivoIoT {
    private boolean gravando;
    private int qualidade;
    private boolean detectandoMovimento;

    public Camera() {
        super();
    }
    
    public Camera(String nome, String comodo, boolean online, boolean gravando, int qualidade, boolean detectandoMovimento) {
        super(nome, null, comodo);
        this.setOnline(online);
        this.gravando = false;
        this.qualidade = 0;
        this.detectandoMovimento = false;
    }
    
    public boolean isGravando() { return gravando; }
    public int getQualidade() { return qualidade; }
    public boolean isDetectandoMovimento() { return detectandoMovimento; }
    public void setGravando(boolean gravando) { this.gravando = gravando; }
    public void setQualidade(int qualidade) { this.qualidade = qualidade; }
    public void setDetectandoMovimento(boolean detectandoMovimento) { this.detectandoMovimento = detectandoMovimento; }

    @Override
    public String toString() {
        return "Camera{" + super.toString() + ", gravando=" + gravando + ", qualidade=" + qualidade + ", detectandoMovimento=" + detectandoMovimento + '}';
    }
}
