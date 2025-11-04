package smarthome.net;

import java.io.Serializable;

/**
 * Mensagem de requisição enviada pelo cliente para o servidor.
 * Contém o tipo de operação e os dados necessários.
 */
public class MensagemRequest implements Serializable {
    
    public enum TipoOperacao {
        LISTAR_DISPOSITIVOS(1),
        OBTER_DISPOSITIVO(2),
        ATUALIZAR_DISPOSITIVO(3),
        EXECUTAR_ACAO(4);
        
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
    private String dispositivoId;  // Para operações que precisam de ID
    private String dados;           // Dados adicionais (JSON ou string simples)
    
    public MensagemRequest() {
    }
    
    public MensagemRequest(TipoOperacao tipoOperacao) {
        this.tipoOperacao = tipoOperacao;
    }
    
    public MensagemRequest(TipoOperacao tipoOperacao, String dispositivoId) {
        this.tipoOperacao = tipoOperacao;
        this.dispositivoId = dispositivoId;
    }
    
    public MensagemRequest(TipoOperacao tipoOperacao, String dispositivoId, String dados) {
        this.tipoOperacao = tipoOperacao;
        this.dispositivoId = dispositivoId;
        this.dados = dados;
    }
    
    public TipoOperacao getTipoOperacao() {
        return tipoOperacao;
    }
    
    public void setTipoOperacao(TipoOperacao tipoOperacao) {
        this.tipoOperacao = tipoOperacao;
    }
    
    public String getDispositivoId() {
        return dispositivoId;
    }
    
    public void setDispositivoId(String dispositivoId) {
        this.dispositivoId = dispositivoId;
    }
    
    public String getDados() {
        return dados;
    }
    
    public void setDados(String dados) {
        this.dados = dados;
    }
    
    @Override
    public String toString() {
        return "MensagemRequest{" +
                "tipoOperacao=" + tipoOperacao +
                ", dispositivoId='" + dispositivoId + '\'' +
                ", dados='" + dados + '\'' +
                '}';
    }
}

