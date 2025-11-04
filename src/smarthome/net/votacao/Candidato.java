package smarthome.net.votacao;

import java.io.Serializable;

/**
 * Representa um candidato na votação.
 */
public class Candidato implements Serializable {
    
    private String id;
    private String nome;
    private int votos;
    
    public Candidato() {
    }
    
    public Candidato(String id, String nome) {
        this.id = id;
        this.nome = nome;
        this.votos = 0;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public int getVotos() {
        return votos;
    }
    
    public void setVotos(int votos) {
        this.votos = votos;
    }
    
    public void adicionarVoto() {
        this.votos++;
    }
    
    @Override
    public String toString() {
        return "Candidato{" +
                "id='" + id + '\'' +
                ", nome='" + nome + '\'' +
                ", votos=" + votos +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Candidato candidato = (Candidato) o;
        return id != null && id.equals(candidato.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
