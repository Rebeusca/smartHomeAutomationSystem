import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public abstract class DispositivoIoT implements Serializable {
    private String id;
    private String nome;
    private String descricao;
    private String comodo;
    private boolean online;

    protected DispositivoIoT() {
        this.id = UUID.randomUUID().toString();
    }

    protected DispositivoIoT(String nome, String descricao, String comodo) {
        this();
        this.nome = nome;
        this.descricao = descricao;
        this.comodo = comodo;
        this.online = false;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public String getComodo() { return comodo; }
    public boolean getOnline() { return online; }
    public void setNome(String nome) { this.nome = nome; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setComodo(String comodo) { this.comodo = comodo; }
    public void setId(String id) { this.id = id; }
    public void setOnline(boolean online) { this.online = online; }

    @Override
    public String toString() {
        return "DispositivoIoT{" +
                "id='" + id + '\'' +
                ", nome='" + nome + '\'' +
                ", descricao='" + descricao + '\'' +
                ", comodo='" + comodo + '\'' +
                ", online=" + online +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DispositivoIoT that = (DispositivoIoT) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}