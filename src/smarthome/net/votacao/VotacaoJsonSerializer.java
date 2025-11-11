package smarthome.net.votacao;

import java.util.ArrayList;
import java.util.List;

public class VotacaoJsonSerializer {
    
    public static String candidatosToJson(List<Candidato> candidatos) {
        if (candidatos == null || candidatos.isEmpty()) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < candidatos.size(); i++) {
            Candidato c = candidatos.get(i);
            sb.append("{");
            sb.append("\"id\":\"").append(escapeJson(c.getId())).append("\",");
            sb.append("\"nome\":\"").append(escapeJson(c.getNome())).append("\",");
            sb.append("\"votos\":").append(c.getVotos());
            sb.append("}");
            if (i < candidatos.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    public static List<Candidato> jsonToCandidatos(String json) {
        List<Candidato> candidatos = new ArrayList<>();
        if (json == null || json.trim().isEmpty() || json.equals("[]")) {
            return candidatos;
        }
        
        json = json.trim();
        if (json.startsWith("[")) {
            json = json.substring(1, json.length() - 1);
        }
        
        String[] objetos = json.split("\\},\\{");
        for (String obj : objetos) {
            obj = obj.replace("{", "").replace("}", "");
            String[] campos = obj.split(",");
            String id = null, nome = null;
            int votos = 0;
            
            for (String campo : campos) {
                String[] kv = campo.split(":");
                if (kv.length == 2) {
                    String key = kv[0].trim().replace("\"", "");
                    String value = kv[1].trim().replace("\"", "");
                    
                    if (key.equals("id")) {
                        id = value;
                    } else if (key.equals("nome")) {
                        nome = value;
                    } else if (key.equals("votos")) {
                        try {
                            votos = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            votos = 0;
                        }
                    }
                }
            }
            
            if (id != null && nome != null) {
                Candidato c = new Candidato(id, nome);
                c.setVotos(votos);
                candidatos.add(c);
            }
        }
        
        return candidatos;
    }
    
    public static String resultadoToJson(ResultadoVotacao resultado) {
        if (resultado == null) {
            return "{}";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"totalVotos\":").append(resultado.getTotalVotos()).append(",");
        
        if (resultado.getVencedor() != null) {
            sb.append("\"vencedor\":{");
            sb.append("\"id\":\"").append(escapeJson(resultado.getVencedor().getId())).append("\",");
            sb.append("\"nome\":\"").append(escapeJson(resultado.getVencedor().getNome())).append("\",");
            sb.append("\"votos\":").append(resultado.getVencedor().getVotos());
            sb.append("},");
        }
        
        sb.append("\"resultados\":[");
        List<ResultadoVotacao.ResultadoCandidato> resultados = resultado.getResultados();
        for (int i = 0; i < resultados.size(); i++) {
            ResultadoVotacao.ResultadoCandidato res = resultados.get(i);
            sb.append("{");
            sb.append("\"candidato\":{");
            sb.append("\"id\":\"").append(escapeJson(res.getCandidato().getId())).append("\",");
            sb.append("\"nome\":\"").append(escapeJson(res.getCandidato().getNome())).append("\",");
            sb.append("\"votos\":").append(res.getCandidato().getVotos());
            sb.append("},");
            sb.append("\"votos\":").append(res.getVotos()).append(",");
            sb.append("\"percentual\":").append(res.getPercentual());
            sb.append("}");
            if (i < resultados.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        sb.append("}");
        
        return sb.toString();
    }
    
    public static String[] parseLoginJson(String json) {
        String username = null, senha = null;
        
        if (json != null && !json.trim().isEmpty()) {
            json = json.replace("{", "").replace("}", "").trim();
            String[] campos = json.split(",");
            
            for (String campo : campos) {
                String[] kv = campo.split(":");
                if (kv.length == 2) {
                    String key = kv[0].trim().replace("\"", "");
                    String value = kv[1].trim().replace("\"", "");
                    
                    if (key.equals("username")) {
                        username = value;
                    } else if (key.equals("senha")) {
                        senha = value;
                    }
                }
            }
        }
        
        return new String[]{username, senha};
    }
    
    public static String parseUsernameJson(String json) {
        if (json != null && !json.trim().isEmpty()) {
            json = json.replace("{", "").replace("}", "").trim();
            String[] campos = json.split(",");
            
            for (String campo : campos) {
                String[] kv = campo.split(":");
                if (kv.length == 2) {
                    String key = kv[0].trim().replace("\"", "");
                    String value = kv[1].trim().replace("\"", "");
                    
                    if (key.equals("username")) {
                        return value;
                    }
                }
            }
        }
        return null;
    }
    
    public static String parseVotoJson(String json) {
        if (json != null && !json.trim().isEmpty()) {
            json = json.replace("{", "").replace("}", "").trim();
            String[] campos = json.split(",");
            
            for (String campo : campos) {
                String[] kv = campo.split(":");
                if (kv.length == 2) {
                    String key = kv[0].trim().replace("\"", "");
                    String value = kv[1].trim().replace("\"", "");
                    
                    if (key.equals("candidatoId")) {
                        return value;
                    }
                }
            }
        }
        return null;
    }
    
    public static String[] parseCandidatoJson(String json) {
        String id = null, nome = null;
        
        if (json != null && !json.trim().isEmpty()) {
            json = json.replace("{", "").replace("}", "").trim();
            String[] campos = json.split(",");
            
            for (String campo : campos) {
                String[] kv = campo.split(":");
                if (kv.length == 2) {
                    String key = kv[0].trim().replace("\"", "");
                    String value = kv[1].trim().replace("\"", "");
                    
                    if (key.equals("id")) {
                        id = value;
                    } else if (key.equals("nome")) {
                        nome = value;
                    }
                }
            }
        }
        
        return new String[]{id, nome};
    }
    
    public static int parseDuracaoJson(String json) {
        if (json != null && !json.trim().isEmpty()) {
            json = json.replace("{", "").replace("}", "").trim();
            String[] campos = json.split(",");
            
            for (String campo : campos) {
                String[] kv = campo.split(":");
                if (kv.length == 2) {
                    String key = kv[0].trim().replace("\"", "");
                    String value = kv[1].trim().replace("\"", "");
                    
                    if (key.equals("duracaoMinutos")) {
                        try {
                            return Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            return 5;
                        }
                    }
                }
            }
        }
        return 5;
    }
    
    public static String[] parseNotaJson(String json) {
        String titulo = null, mensagem = null;
        
        if (json != null && !json.trim().isEmpty()) {
            json = json.replace("{", "").replace("}", "").trim();
            String[] campos = json.split(",");
            
            for (String campo : campos) {
                String[] kv = campo.split(":");
                if (kv.length == 2) {
                    String key = kv[0].trim().replace("\"", "");
                    String value = kv[1].trim().replace("\"", "");
                    
                    if (key.equals("titulo")) {
                        titulo = value;
                    } else if (key.equals("mensagem")) {
                        mensagem = value;
                    }
                }
            }
        }
        
        return new String[]{titulo, mensagem};
    }
    
    public static ResultadoVotacao jsonToResultado(String json) {
        ResultadoVotacao resultado = new ResultadoVotacao();
        
        if (json == null || json.trim().isEmpty() || json.equals("{}")) {
            return resultado;
        }
        
        try {
            if (json.contains("\"totalVotos\":")) {
                String[] partes = json.split("\"totalVotos\":");
                if (partes.length > 1) {
                    String valor = partes[1].split(",")[0].trim();
                    resultado.setTotalVotos(Integer.parseInt(valor));
                }
            }
            
            if (json.contains("\"resultados\":")) {
                String resultadosStr = json.substring(json.indexOf("\"resultados\":") + 13);
                resultadosStr = resultadosStr.substring(resultadosStr.indexOf("[") + 1, resultadosStr.lastIndexOf("]"));
                
                String[] resultadosArray = resultadosStr.split("\\},\\{");
                for (String resStr : resultadosArray) {
                    resStr = resStr.replace("{", "").replace("}", "").trim();
                    if (resStr.isEmpty()) continue;
                    
                    String candidatoJson = resStr.substring(resStr.indexOf("\"candidato\":") + 12);
                    candidatoJson = candidatoJson.substring(0, candidatoJson.indexOf("},") + 1);
                    
                    List<Candidato> cands = jsonToCandidatos("[" + candidatoJson + "]");
                    if (!cands.isEmpty()) {
                        Candidato c = cands.get(0);
                        
                        int votos = 0;
                        double percentual = 0.0;
                        
                        String[] campos = resStr.split(",");
                        for (String campo : campos) {
                            if (campo.contains("\"votos\":")) {
                                String v = campo.split(":")[1].trim();
                                votos = Integer.parseInt(v);
                            } else if (campo.contains("\"percentual\":")) {
                                String p = campo.split(":")[1].trim().replace("}", "");
                                percentual = Double.parseDouble(p);
                            }
                        }
                        
                        resultado.getResultados().add(
                            new ResultadoVotacao.ResultadoCandidato(c, votos, percentual));
                    }
                }
            }
            
            if (json.contains("\"vencedor\":")) {
                String vencedorStr = json.substring(json.indexOf("\"vencedor\":") + 11);
                vencedorStr = vencedorStr.substring(0, vencedorStr.indexOf("},") + 1);
                List<Candidato> vencedorList = jsonToCandidatos("[" + vencedorStr + "]");
                if (!vencedorList.isEmpty()) {
                    resultado.setVencedor(vencedorList.get(0));
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao parsear resultados: " + e.getMessage());
        }
        
        return resultado;
    }
    
    private static String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
