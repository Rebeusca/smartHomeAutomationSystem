package smarthome.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser JSON simples para deserializar requisições JSON.
 * Implementação básica para casos simples (strings, arrays, objetos básicos).
 */
public class JsonParser {
    
    /**
     * Parseia uma string JSON e retorna um objeto Java.
     */
    public static Object parse(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        json = json.trim();
        
        // null
        if (json.equals("null")) {
            return null;
        }
        
        // String (entre aspas)
        if (json.startsWith("\"") && json.endsWith("\"")) {
            return json.substring(1, json.length() - 1)
                      .replace("\\\"", "\"")
                      .replace("\\n", "\n")
                      .replace("\\r", "\r")
                      .replace("\\t", "\t");
        }
        
        // Array (começa com [)
        if (json.startsWith("[")) {
            return parseArray(json);
        }
        
        // Objeto (começa com {)
        if (json.startsWith("{")) {
            return parseObject(json);
        }
        
        // Número ou boolean
        if (json.equals("true")) return true;
        if (json.equals("false")) return false;
        if (json.equals("null")) return null;
        
        try {
            // Tenta parsear como número
            if (json.contains(".")) {
                return Double.parseDouble(json);
            } else {
                return Integer.parseInt(json);
            }
        } catch (NumberFormatException e) {
            // Se não for número, retorna como string
            return json;
        }
    }
    
    /**
     * Parseia um array JSON.
     */
    private static List<Object> parseArray(String json) {
        List<Object> result = new ArrayList<>();
        json = json.trim();
        
        if (json.equals("[]")) {
            return result;
        }
        
        // Remove [ e ]
        json = json.substring(1, json.length() - 1).trim();
        
        if (json.isEmpty()) {
            return result;
        }
        
        // Divide por vírgulas (respeitando strings e objetos aninhados)
        List<String> elementos = splitJson(json);
        
        for (String elemento : elementos) {
            result.add(parse(elemento.trim()));
        }
        
        return result;
    }
    
    /**
     * Parseia um objeto JSON.
     */
    private static Map<String, Object> parseObject(String json) {
        Map<String, Object> result = new HashMap<>();
        json = json.trim();
        
        if (json.equals("{}")) {
            return result;
        }
        
        // Remove { e }
        json = json.substring(1, json.length() - 1).trim();
        
        if (json.isEmpty()) {
            return result;
        }
        
        // Divide por vírgulas (respeitando strings e objetos aninhados)
        List<String> pares = splitJson(json);
        
        for (String par : pares) {
            int doisPontos = par.indexOf(':');
            if (doisPontos > 0) {
                String chave = par.substring(0, doisPontos).trim();
                String valor = par.substring(doisPontos + 1).trim();
                
                // Remove aspas da chave
                if (chave.startsWith("\"") && chave.endsWith("\"")) {
                    chave = chave.substring(1, chave.length() - 1);
                }
                
                result.put(chave, parse(valor));
            }
        }
        
        return result;
    }
    
    /**
     * Divide uma string JSON por vírgulas, respeitando strings e objetos aninhados.
     */
    private static List<String> splitJson(String json) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        boolean inString = false;
        boolean escapeNext = false;
        
        for (char c : json.toCharArray()) {
            if (escapeNext) {
                current.append(c);
                escapeNext = false;
                continue;
            }
            
            if (c == '\\') {
                escapeNext = true;
                current.append(c);
                continue;
            }
            
            if (c == '"' && !escapeNext) {
                inString = !inString;
                current.append(c);
                continue;
            }
            
            if (inString) {
                current.append(c);
                continue;
            }
            
            if (c == '{' || c == '[') {
                depth++;
                current.append(c);
            } else if (c == '}' || c == ']') {
                depth--;
                current.append(c);
            } else if (c == ',' && depth == 0) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            result.add(current.toString());
        }
        
        return result;
    }
}
