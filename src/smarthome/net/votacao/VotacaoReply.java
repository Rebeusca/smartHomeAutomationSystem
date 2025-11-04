package smarthome.net.votacao;

import java.io.Serializable;
import java.util.List;

/**
 * Mensagem de resposta do sistema de votação.
 * Usa JSON para representação externa de dados.
 */
public class VotacaoReply implements Serializable {
    
    public enum Status {
        SUCESSO(1),
        ERRO(2),
        LOGIN_INVALIDO(3),
        JA_VOTOU(4),
        VOTACAO_ENCERRADA(5),
        CANDIDATO_NAO_ENCONTRADO(6),
        NAO_AUTORIZADO(7),
        VOTACAO_NAO_INICIADA(8);
        
        private final int codigo;
        
        Status(int codigo) {
            this.codigo = codigo;
        }
        
        public int getCodigo() {
            return codigo;
        }
        
        public static Status fromCodigo(int codigo) {
            for (Status status : values()) {
                if (status.codigo == codigo) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Código de status inválido: " + codigo);
        }
    }
    
    private Status status;
    private String mensagem;
    private String dados; // JSON com dados adicionais (candidatos, resultados, etc.)
    
    public VotacaoReply() {
    }
    
    public VotacaoReply(Status status) {
        this.status = status;
    }
    
    public VotacaoReply(Status status, String mensagem) {
        this.status = status;
        this.mensagem = mensagem;
    }
    
    public VotacaoReply(Status status, String mensagem, String dados) {
        this.status = status;
        this.mensagem = mensagem;
        this.dados = dados;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getMensagem() {
        return mensagem;
    }
    
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
    
    public String getDados() {
        return dados;
    }
    
    public void setDados(String dados) {
        this.dados = dados;
    }
    
    @Override
    public String toString() {
        return "VotacaoReply{" +
                "status=" + status +
                ", mensagem='" + mensagem + '\'' +
                ", dados=" + (dados != null ? "presente" : "null") +
                '}';
    }
}
