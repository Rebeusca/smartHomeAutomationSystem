package smarthome.pojos;

public class Fechadura extends DispositivoIoT {
    private boolean bloqueada;
    private int bateria;
    private boolean modoAutoClock;

    public Fechadura() {
        super();
    }

    public Fechadura(String nome, String comodo, boolean online, boolean bloqueada, int bateria, boolean modoAutoClock) {
        super(nome, null, comodo);
        this.setOnline(online);
        this.bloqueada = false;
        this.bateria = 0;
        this.modoAutoClock = false;
    }
    
    public boolean isBloqueada() { return bloqueada; }
    public int getBateria() { return bateria; }
    public boolean isModoAutoClock() { return modoAutoClock; }
    public void setBloqueada(boolean bloqueada) { this.bloqueada = bloqueada; }
    public void setBateria(int bateria) { this.bateria = bateria; }
    public void setModoAutoClock(boolean modoAutoClock) { this.modoAutoClock = modoAutoClock; }

    @Override
    public String toString() {
        return "Fechadura{" + super.toString() + ", bloqueada=" + bloqueada + ", bateria=" + bateria + ", modoAutoClock=" + modoAutoClock + '}';
    }
}
