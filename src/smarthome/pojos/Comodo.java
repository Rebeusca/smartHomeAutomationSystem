package smarthome.pojos;

import java.io.Serializable;
import java.util.List;

public class Comodo implements Serializable {
    private String id;
    private String nome;
    private List<DispositivoIoT> dispositivos;

    public Comodo() {}
    public Comodo(String nome) {
        this.nome = nome;
    }

    public String getNome() { return nome; }
    public List<DispositivoIoT> getDispositivos() { return dispositivos; }
    public void setNome(String nome) { this.nome = nome; }
    public void setDispositivos(List<DispositivoIoT> dispositivos) { this.dispositivos = dispositivos; }

    public void adicionarDispositivo(DispositivoIoT dispositivo) {
        if (dispositivo != null) {
            dispositivos.add(dispositivo);
        }
    }

    public void removerDispositivo(DispositivoIoT dispositivo) {
        if (dispositivo != null) {
            dispositivos.remove(dispositivo);
        }
    }
    
    @Override
    public String toString() {
        return "Comodo{" +
                "id='" + id + '\'' +
                ", nome='" + nome + '\'' +
                ", dispositivos=" + dispositivos +
                '}';
    }
}
