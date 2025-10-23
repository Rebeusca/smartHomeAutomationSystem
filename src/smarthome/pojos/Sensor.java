public class Sensor extends DispositivoIoT {
    private String tipo;
    private boolean unidadeMedida;
    private double valor;
    

    public Sensor() {
        super();
    }

    public Sensor(String nome, String comodo, boolean online, String tipo, boolean unidadeMedida, double valor) {
        super(nome, null, comodo);
        this.setOnline(online);
        this.tipo = tipo;
        this.unidadeMedida = unidadeMedida;
        this.valor = valor;
    }
    

    public String getTipo() { return tipo; }
    public boolean isUnidadeMedida() { return unidadeMedida; }
    public double getValor() { return valor; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setUnidadeMedida(boolean unidadeMedida) { this.unidadeMedida = unidadeMedida; }
    public void setValor(double valor) { this.valor = valor; }

    @Override
    public String toString() {
        return "Sensor{" + super.toString() + ", tipo=" + tipo + ", unidadeMedida=" + unidadeMedida + ", valor=" + valor + '}';
    }
}
