package smarthome.net.votacao;

import java.io.Serializable;

/**
 * Representa um eleitor no sistema de votação.
 */
public class Eleitor implements Serializable {
    
    private String username;
    private String senha;
    private boolean jaVotou;
    private boolean isAdmin;
    
    public Eleitor() {
    }
    
    public Eleitor(String username, String senha, boolean isAdmin) {
        this.username = username;
        this.senha = senha;
        this.jaVotou = false;
        this.isAdmin = isAdmin;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getSenha() {
        return senha;
    }
    
    public void setSenha(String senha) {
        this.senha = senha;
    }
    
    public boolean isJaVotou() {
        return jaVotou;
    }
    
    public void setJaVotou(boolean jaVotou) {
        this.jaVotou = jaVotou;
    }
    
    public boolean isAdmin() {
        return isAdmin;
    }
    
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
    
    public boolean validarSenha(String senha) {
        return this.senha.equals(senha);
    }
    
    @Override
    public String toString() {
        return "Eleitor{" +
                "username='" + username + '\'' +
                ", jaVotou=" + jaVotou +
                ", isAdmin=" + isAdmin +
                '}';
    }
}
