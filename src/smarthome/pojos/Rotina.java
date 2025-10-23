import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;
import java.util.UUID;
import java.util.Objects;

public class Rotina implements Serializable {
    private String id;
    private String nome;
    private List<Acao> acoes = new ArrayList<>();
    private LocalDateTime horarioInicio;

    public Rotina(String nome, List<Acao> acoes, LocalDateTime horarioInicio) {
        this.id = UUID.randomUUID().toString();
        this.nome = nome;
        if (acoes != null) this.acoes.addAll(acoes);
        this.horarioInicio = horarioInicio;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public List<Acao> getAcoes() { return acoes; }
    public LocalDateTime getHorarioInicio() { return horarioInicio; }
    public void setNome(String nome) { this.nome = nome; }
    public void setAcoes(List<Acao> acoes) {
        this.acoes = (acoes == null) ? new ArrayList<>() : new ArrayList<>(acoes);
    }
    public void setHorarioInicio(LocalDateTime horarioInicio) { this.horarioInicio = horarioInicio; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rotina)) return false;
        Rotina rotina = (Rotina) o;
        return Objects.equals(id, rotina.id);
    }
}
