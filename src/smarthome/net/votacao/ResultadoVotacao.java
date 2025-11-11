package smarthome.net.votacao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResultadoVotacao implements Serializable {
    
    private int totalVotos;
    private List<ResultadoCandidato> resultados;
    private Candidato vencedor;
    
    public ResultadoVotacao() {
        this.resultados = new ArrayList<>();
    }
    
    public int getTotalVotos() {
        return totalVotos;
    }
    
    public void setTotalVotos(int totalVotos) {
        this.totalVotos = totalVotos;
    }
    
    public List<ResultadoCandidato> getResultados() {
        return resultados;
    }
    
    public void setResultados(List<ResultadoCandidato> resultados) {
        this.resultados = resultados;
    }
    
    public Candidato getVencedor() {
        return vencedor;
    }
    
    public void setVencedor(Candidato vencedor) {
        this.vencedor = vencedor;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RESULTADOS DA VOTAÇÃO ===\n");
        sb.append("Total de votos: ").append(totalVotos).append("\n\n");
        
        for (ResultadoCandidato res : resultados) {
            sb.append(String.format("%s: %d votos (%.2f%%)\n", 
                res.getCandidato().getNome(), 
                res.getVotos(), 
                res.getPercentual()));
        }
        
        if (vencedor != null) {
            sb.append("\n*** VENCEDOR: ").append(vencedor.getNome());
        }
        
        return sb.toString();
    }
    
    public static class ResultadoCandidato implements Serializable {
        private Candidato candidato;
        private int votos;
        private double percentual;
        
        public ResultadoCandidato(Candidato candidato, int votos, double percentual) {
            this.candidato = candidato;
            this.votos = votos;
            this.percentual = percentual;
        }
        
        public Candidato getCandidato() {
            return candidato;
        }
        
        public int getVotos() {
            return votos;
        }
        
        public double getPercentual() {
            return percentual;
        }
    }
}
