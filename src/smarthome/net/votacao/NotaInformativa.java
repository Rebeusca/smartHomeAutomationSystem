package smarthome.net.votacao;

import java.io.Serializable;

/**
 * Representa uma nota informativa enviada pelos administradores.
 */
public class NotaInformativa implements Serializable {
    
    private String titulo;
    private String mensagem;
    private long timestamp;
    private String admin;
    
    public NotaInformativa() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public NotaInformativa(String titulo, String mensagem, String admin) {
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.admin = admin;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public String getMensagem() {
        return mensagem;
    }
    
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getAdmin() {
        return admin;
    }
    
    public void setAdmin(String admin) {
        this.admin = admin;
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s\n%s", 
            admin != null ? admin : "Administrador",
            titulo != null ? titulo : "Nota Informativa",
            mensagem != null ? mensagem : "");
    }
}
