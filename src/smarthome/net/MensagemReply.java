package smarthome.net;

import smarthome.pojos.DispositivoIoT;
import java.io.Serializable;

/**
 * Mensagem de resposta enviada pelo servidor para o cliente.
 * Contém o status da operação e os dados resultantes.
 */
public class MensagemReply implements Serializable {
    
    public enum Status {
        SUCESSO(1),
        ERRO(2),
        DISPOSITIVO_NAO_ENCONTRADO(3);
        
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
    private String mensagem;              // Mensagem de erro ou sucesso
    private DispositivoIoT[] dispositivos; // Array de dispositivos (para listagem)
    private DispositivoIoT dispositivo;     // Dispositivo único (para operações específicas)
    
    public MensagemReply() {
    }
    
    public MensagemReply(Status status) {
        this.status = status;
    }
    
    public MensagemReply(Status status, String mensagem) {
        this.status = status;
        this.mensagem = mensagem;
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
    
    public DispositivoIoT[] getDispositivos() {
        return dispositivos;
    }
    
    public void setDispositivos(DispositivoIoT[] dispositivos) {
        this.dispositivos = dispositivos;
    }
    
    public DispositivoIoT getDispositivo() {
        return dispositivo;
    }
    
    public void setDispositivo(DispositivoIoT dispositivo) {
        this.dispositivo = dispositivo;
    }
    
    @Override
    public String toString() {
        return "MensagemReply{" +
                "status=" + status +
                ", mensagem='" + mensagem + '\'' +
                ", dispositivos=" + (dispositivos != null ? dispositivos.length : 0) +
                ", dispositivo=" + (dispositivo != null ? dispositivo.getId() : "null") +
                '}';
    }
}

