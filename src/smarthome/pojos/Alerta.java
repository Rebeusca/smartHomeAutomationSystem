import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Alerta implements Serializable {
    private String id;
    private String titulo;
    private String mensagem;
    private Instant timestamp;
    private String comodo;

    public Alerta() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
    }

    public Alerta(String titulo, String mensagem, String comodo) {
        this();
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.comodo = comodo;
    }

    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getMensagem() { return mensagem; }
    public Instant getTimestamp() { return timestamp; }
    public String getComodo() { return comodo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public void setComodo(String comodo) { this.comodo = comodo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Alerta)) return false;
        Alerta that = (Alerta) o;
        return Objects.equals(id, that.id);
    }
}
