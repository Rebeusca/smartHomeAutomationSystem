package smarthome.pojos;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

public class Acao implements Serializable {
    private String dispositivoId;
    private String comando;
    private Map<String, Object> parametros = new HashMap<>();

    public Acao() {}

    public Acao(String dispositivoId, String comando, Map<String, Object> parametros) {
        this.dispositivoId = dispositivoId;
        this.comando = comando;
        if (parametros != null) this.parametros.putAll(parametros);
    }

    public String getDispositivoId() { return dispositivoId; }
    public String getComando() { return comando; }
    public Map<String, Object> getParametros() { return parametros; }
    public void setDispositivoId(String dispositivoId) { this.dispositivoId = dispositivoId; }
    public void setComando(String comando) { this.comando = comando; }
    public void setParametros(Map<String, Object> parametros) { this.parametros = parametros; }

    @Override
    public String toString() {
        return "Acao{" +
                "dispositivoId='" + dispositivoId + '\'' +
                ", comando='" + comando + '\'' +
                ", parametros=" + parametros +
                '}';
    }
}