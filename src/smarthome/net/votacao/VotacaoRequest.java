package smarthome.net.votacao;

import java.io.Serializable;

/**
 * Mensagem de requisição do sistema de votação.
 * Usa JSON para representação externa de dados.
 */
public class VotacaoRequest implements Serializable {
    
    public enum TipoOperacao {
        LOGIN(1),
        LISTAR_CANDIDATOS(2),
        VOTAR(3),
        ADICIONAR_CANDIDATO(4),
        REMOVER_CANDIDATO(5),
        ENVIAR_NOTA(6),
        OBTER_RESULTADOS(7),
        INICIAR_VOTACAO(8),
        ENCERRAR_VOTACAO(9);
        
        private final int codigo;
        
        TipoOperacao(int codigo) {
            this.codigo = codigo;
        }
        
        public int getCodigo() {
            return codigo;
        }
        
        public static TipoOperacao fromCodigo(int codigo) {
            for (TipoOperacao tipo : values()) {
                if (tipo.codigo == codigo) {
                    return tipo;
                }
            }
            throw new IllegalArgumentException("Código de operação inválido: " + codigo);
        }
    }
    
    private TipoOperacao tipoOperacao;
    private String dados; // JSON com os dados necessários
    
    public VotacaoRequest() {
    }
    
    public VotacaoRequest(TipoOperacao tipoOperacao) {
        this.tipoOperacao = tipoOperacao;
    }
    
    public VotacaoRequest(TipoOperacao tipoOperacao, String dados) {
        this.tipoOperacao = tipoOperacao;
        this.dados = dados;
    }
    
    public TipoOperacao getTipoOperacao() {
        return tipoOperacao;
    }
    
    public void setTipoOperacao(TipoOperacao tipoOperacao) {
        this.tipoOperacao = tipoOperacao;
    }
    
    public String getDados() {
        return dados;
    }
    
    public void setDados(String dados) {
        this.dados = dados;
    }
    
    @Override
    public String toString() {
        return "VotacaoRequest{" +
                "tipoOperacao=" + tipoOperacao +
                ", dados='" + dados + '\'' +
                '}';
    }
}
