package smarthome.net;

import smarthome.pojos.*;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Utilitário para converter objetos Java para JSON (implementação simples).
 * Usado para comunicação com clientes em outras linguagens.
 */
public class JsonConverter {
    
    /**
     * Converte um objeto para JSON string.
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        if (obj instanceof String) {
            return "\"" + escapeJson((String) obj) + "\"";
        }
        
        if (obj instanceof Boolean) {
            return obj.toString();
        }
        
        if (obj instanceof Number) {
            return obj.toString();
        }
        
        if (obj instanceof List) {
            return listToJson((List<?>) obj);
        }
        
        if (obj instanceof DispositivoIoT) {
            return dispositivoToJson((DispositivoIoT) obj);
        }
        
        if (obj instanceof Rotina) {
            return rotinaToJson((Rotina) obj);
        }
        
        if (obj instanceof Alerta) {
            return alertaToJson((Alerta) obj);
        }
        
        if (obj instanceof Comodo) {
            return comodoToJson((Comodo) obj);
        }
        
        if (obj instanceof Acao) {
            return acaoToJson((Acao) obj);
        }
        
        if (obj instanceof Object[]) {
            return arrayToJson((Object[]) obj);
        }
        
        if (obj instanceof Map) {
            return mapToJson((Map<?, ?>) obj);
        }
        
        return "\"" + obj.toString() + "\"";
    }
    
    private static String dispositivoToJson(DispositivoIoT d) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(toJson(d.getId())).append(",");
        sb.append("\"nome\":").append(toJson(d.getNome())).append(",");
        sb.append("\"descricao\":").append(toJson(d.getDescricao())).append(",");
        sb.append("\"comodo\":").append(toJson(d.getComodo())).append(",");
        sb.append("\"online\":").append(d.getOnline());
        
        // Adiciona campos específicos do tipo
        String tipo = d.getClass().getSimpleName();
        sb.append(",\"tipo\":").append(toJson(tipo));
        
        if (d instanceof Lampada) {
            Lampada l = (Lampada) d;
            sb.append(",\"ligada\":").append(l.isLigada());
            sb.append(",\"intensidade\":").append(l.getIntensidade());
            sb.append(",\"temperatura\":").append(l.getTemperatura());
        } else if (d instanceof Termostato) {
            Termostato t = (Termostato) d;
            sb.append(",\"temperaturaAtual\":").append(t.getTemperaturaAtual());
            sb.append(",\"temperaturaDesejada\":").append(t.getTemperaturaDesejada());
        } else if (d instanceof Sensor) {
            Sensor s = (Sensor) d;
            sb.append(",\"tipoSensor\":").append(toJson(s.getTipo()));
            sb.append(",\"unidadeMedida\":").append(s.isUnidadeMedida());
            sb.append(",\"valor\":").append(s.getValor());
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    private static String rotinaToJson(Rotina r) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(toJson(r.getId())).append(",");
        sb.append("\"nome\":").append(toJson(r.getNome())).append(",");
        sb.append("\"acoes\":").append(listToJson(r.getAcoes())).append(",");
        if (r.getHorarioInicio() != null) {
            sb.append("\"horarioInicio\":").append(toJson(r.getHorarioInicio().toString()));
        } else {
            sb.append("\"horarioInicio\":null");
        }
        sb.append("}");
        return sb.toString();
    }
    
    private static String alertaToJson(Alerta a) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(toJson(a.getId())).append(",");
        sb.append("\"titulo\":").append(toJson(a.getTitulo())).append(",");
        sb.append("\"mensagem\":").append(toJson(a.getMensagem())).append(",");
        sb.append("\"comodo\":").append(toJson(a.getComodo()));
        if (a.getTimestamp() != null) {
            sb.append(",\"timestamp\":").append(toJson(a.getTimestamp().toString()));
        }
        sb.append("}");
        return sb.toString();
    }
    
    private static String comodoToJson(Comodo c) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"nome\":").append(toJson(c.getNome())).append(",");
        sb.append("\"dispositivos\":").append(listToJson(c.getDispositivos()));
        sb.append("}");
        return sb.toString();
    }
    
    private static String acaoToJson(Acao a) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"dispositivoId\":").append(toJson(a.getDispositivoId())).append(",");
        sb.append("\"comando\":").append(toJson(a.getComando())).append(",");
        sb.append("\"parametros\":").append(mapToJson(a.getParametros()));
        sb.append("}");
        return sb.toString();
    }
    
    private static String mapToJson(Map<?, ?> map) {
        if (map == null) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append(toJson(entry.getKey().toString())).append(":");
            sb.append(toJson(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }
    
    private static String listToJson(List<?> list) {
        if (list == null) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }
    
    private static String arrayToJson(Object[] array) {
        if (array == null) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson(array[i]));
        }
        sb.append("]");
        return sb.toString();
    }
    
    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
