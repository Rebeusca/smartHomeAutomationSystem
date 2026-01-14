package smarthome.net;

import smarthome.interfaces.ISmartHomeService;
import smarthome.services.SmartHomeServiceImpl;
import smarthome.pojos.DispositivoIoT;
import smarthome.pojos.Rotina;
import smarthome.pojos.Alerta;
import smarthome.pojos.Comodo;
import smarthome.pojos.Lampada;
import smarthome.pojos.Termostato;
import smarthome.pojos.Sensor;
import smarthome.pojos.Acao;

import java.util.Map;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Servidor remoto que processa requisições de clientes usando API REST HTTP.
 * Implementa protocolo de requisição/resposta via HTTP.
 * Não utiliza sockets ou RMI diretamente.
 */
public class ServidorRemotoAPI {
    
    private static final int PORTA = 8080;
    private HttpServer server;
    private ISmartHomeService smartHomeService;
    
    public ServidorRemotoAPI() throws IOException {
        this.smartHomeService = new SmartHomeServiceImpl();
        this.server = HttpServer.create(new InetSocketAddress(PORTA), 0);
        configurarRotas();
    }
    
    /**
     * Configura as rotas da API REST.
     */
    private void configurarRotas() {
        // Endpoint para listar dispositivos
        server.createContext("/api/dispositivos", new DispositivosHandler());
        
        // Endpoint para obter dispositivo específico
        server.createContext("/api/dispositivos/obter", new ObterDispositivoHandler());
        
        // Endpoint para atualizar dispositivo
        server.createContext("/api/dispositivos/atualizar", new AtualizarDispositivoHandler());
        
        // Endpoint para executar ação
        server.createContext("/api/dispositivos/acao", new ExecutarAcaoHandler());
        
        // Endpoint para listar rotinas
        server.createContext("/api/rotinas", new RotinasHandler());
        
        // Endpoint para criar rotina
        server.createContext("/api/rotinas/criar", new CriarRotinaHandler());
        
        // Endpoint para listar alertas
        server.createContext("/api/alertas", new AlertasHandler());
        
        // Endpoint para obter cômodo
        server.createContext("/api/comodos/obter", new ObterComodoHandler());
        
        // Endpoint raiz para verificação
        server.createContext("/", new RootHandler());
    }
    
    public void iniciar() {
        server.setExecutor(null); // Usa executor padrão
        server.start();
        System.out.println("=== Servidor Remoto Smart Home (API REST HTTP) ===");
        System.out.println("Servidor iniciado na porta " + PORTA);
        System.out.println("Endpoints disponíveis:");
        System.out.println("  GET  /api/dispositivos");
        System.out.println("  POST /api/dispositivos/obter");
        System.out.println("  POST /api/dispositivos/atualizar");
        System.out.println("  POST /api/dispositivos/acao");
        System.out.println("  GET  /api/rotinas");
        System.out.println("  POST /api/rotinas/criar");
        System.out.println("  GET  /api/alertas");
        System.out.println("  POST /api/comodos/obter");
        System.out.println("Aguardando requisições de clientes...\n");
    }
    
    public void parar() {
        if (server != null) {
            server.stop(0);
            System.out.println("[SERVIDOR] Servidor parado");
        }
    }
    
    /**
     * Handler base para processar requisições HTTP.
     */
    private abstract class BaseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            
            System.out.println("[SERVIDOR] " + method + " " + path);
            
            try {
                // Valida método HTTP
                if (!isMetodoValido(method)) {
                    enviarErroJson(exchange, 405, "Método não permitido: " + method);
                    return;
                }
                
                Object responseObj = processarRequest(exchange);
                
                // API REST sempre retorna JSON
                byte[] response;
                if (responseObj != null) {
                    String jsonResponse = JsonConverter.toJson(responseObj);
                    response = jsonResponse.getBytes("UTF-8");
                } else {
                    response = "null".getBytes("UTF-8");
                }
                enviarRespostaJson(exchange, 200, response);
            } catch (NotFoundException e) {
                // 404 Not Found
                enviarErroJson(exchange, 404, e.getMessage());
            } catch (IllegalArgumentException e) {
                // Erro de validação - 400 Bad Request
                enviarErroJson(exchange, 400, e.getMessage());
            } catch (IOException e) {
                // Erro de I/O ou formato - 400 Bad Request
                if (e.getMessage() != null && 
                    (e.getMessage().contains("JSON") || 
                     e.getMessage().contains("Content-Type") ||
                     e.getMessage().contains("formato") ||
                     e.getMessage().contains("parsear"))) {
                    enviarErroJson(exchange, 400, e.getMessage());
                } else {
                    // Erro interno - 500
                    System.err.println("[ERRO] " + e.getMessage());
                    e.printStackTrace();
                    enviarErroJson(exchange, 500, "Erro interno do servidor");
                }
            } catch (ClassNotFoundException e) {
                // Não deveria acontecer em API REST pura, mas trata como 500
                System.err.println("[ERRO] " + e.getMessage());
                e.printStackTrace();
                enviarErroJson(exchange, 500, "Erro interno do servidor");
            } catch (Exception e) {
                // Erro não esperado - 500 Internal Server Error
                System.err.println("[ERRO] " + e.getMessage());
                e.printStackTrace();
                enviarErroJson(exchange, 500, "Erro interno do servidor");
            }
        }
        
        protected abstract Object processarRequest(HttpExchange exchange) throws IOException, ClassNotFoundException;
        
        protected boolean isMetodoValido(String method) {
            return "GET".equals(method) || "POST".equals(method);
        }
        
        protected void enviarResposta(HttpExchange exchange, int statusCode, byte[] response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
            exchange.sendResponseHeaders(statusCode, response != null ? response.length : 0);
            
            if (response != null && response.length > 0) {
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            }
            exchange.close();
        }
        
        protected void enviarRespostaJson(HttpExchange exchange, int statusCode, byte[] response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, response != null ? response.length : 0);
            
            if (response != null && response.length > 0) {
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            }
            exchange.close();
        }
        
        protected void enviarErroJson(HttpExchange exchange, int statusCode, String mensagem) throws IOException {
            // Cria objeto JSON de erro
            String jsonError = "{\"erro\":\"" + escapeJson(mensagem) + "\",\"codigo\":" + statusCode + "}";
            byte[] errorBytes = jsonError.getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, errorBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorBytes);
            }
            exchange.close();
        }
        
        private String escapeJson(String str) {
            if (str == null) return "";
            return str.replace("\\", "\\\\")
                     .replace("\"", "\\\"")
                     .replace("\n", "\\n")
                     .replace("\r", "\\r")
                     .replace("\t", "\\t");
        }
        
        protected byte[] lerCorpoRequisicao(HttpExchange exchange) throws IOException {
            try (InputStream is = exchange.getRequestBody()) {
                return lerTodosBytes(is);
            }
        }
        
        /**
         * Lê o corpo da requisição e deserializa como JSON.
         * API REST aceita apenas JSON.
         */
        protected Object lerCorpoRequisicaoJson(HttpExchange exchange) throws IOException {
            byte[] data = lerCorpoRequisicao(exchange);
            if (data == null || data.length == 0) {
                return null;
            }
            
            // Verifica Content-Type
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType != null) {
                String contentTypeLower = contentType.toLowerCase().split(";")[0].trim();
                if (!contentTypeLower.equals("application/json")) {
                    throw new IOException("Content-Type deve ser application/json. Recebido: " + contentType);
                }
            }
            
            // Parseia como JSON
            String jsonStr = new String(data, "UTF-8");
            try {
                return JsonParser.parse(jsonStr);
            } catch (Exception e) {
                throw new IOException("JSON inválido: " + e.getMessage(), e);
            }
        }
        
        /**
         * Lê todos os bytes de um InputStream (compatível com Java 8+).
         */
        private byte[] lerTodosBytes(InputStream is) throws IOException {
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int nRead;
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        }
    }
    
    /**
     * Handler para listar dispositivos (GET /api/dispositivos)
     */
    private class DispositivosHandler extends BaseHandler {
        @Override
        protected Object processarRequest(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                throw new IllegalArgumentException("Método deve ser GET");
            }
            return smartHomeService.listarDispositivos();
        }
    }
    
    /**
     * Handler para obter dispositivo (POST /api/dispositivos/obter)
     */
    private class ObterDispositivoHandler extends BaseHandler {
        @Override
        protected Object processarRequest(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                throw new IllegalArgumentException("Método deve ser POST");
            }
            
            Object data = lerCorpoRequisicaoJson(exchange);
            if (data == null) {
                throw new IllegalArgumentException("Corpo da requisição não pode ser vazio");
            }
            
            String dispositivoId;
            if (data instanceof String) {
                dispositivoId = (String) data;
            } else {
                dispositivoId = data.toString();
            }
            
            if (dispositivoId == null || dispositivoId.trim().isEmpty()) {
                throw new IllegalArgumentException("ID do dispositivo não pode ser vazio");
            }
            
            DispositivoIoT dispositivo = smartHomeService.obterDispositivo(dispositivoId);
            if (dispositivo == null) {
                // 404 Not Found
                throw new NotFoundException("Dispositivo não encontrado: " + dispositivoId);
            }
            
            return dispositivo;
        }
    }
    
    /**
     * Handler para atualizar dispositivo (POST /api/dispositivos/atualizar)
     */
    private class AtualizarDispositivoHandler extends BaseHandler {
        @Override
        protected Object processarRequest(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                throw new IllegalArgumentException("Método deve ser POST");
            }
            
            // JSON array: [id, dispositivo]
            Object data = lerCorpoRequisicaoJson(exchange);
            if (data == null) {
                throw new IllegalArgumentException("Corpo da requisição não pode ser vazio");
            }
            
            if (!(data instanceof List)) {
                throw new IllegalArgumentException("Esperado array JSON: [id, dispositivo]");
            }
            
            @SuppressWarnings("unchecked")
            List<Object> args = (List<Object>) data;
            if (args.size() < 2) {
                throw new IllegalArgumentException("Array JSON deve conter [id, dispositivo]");
            }
            
            String id = args.get(0).toString();
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("ID do dispositivo não pode ser vazio");
            }
            
            Object dispositivoJson = args.get(1);
            if (dispositivoJson == null) {
                throw new IllegalArgumentException("Dispositivo não pode ser null");
            }
            
            DispositivoIoT dispositivo = converterJsonParaDispositivo(dispositivoJson);
            DispositivoIoT resultado = smartHomeService.atualizarDispositivo(id, dispositivo);
            
            if (resultado == null) {
                // 404 Not Found
                throw new NotFoundException("Dispositivo não encontrado: " + id);
            }
            
            return resultado;
        }
    }
    
    /**
     * Converte um objeto JSON (Map) para Rotina.
     */
    @SuppressWarnings("unchecked")
    private Rotina converterJsonParaRotina(Object jsonObj) throws IOException {
        if (!(jsonObj instanceof Map)) {
            throw new IOException("Esperado objeto JSON (Map) para converter em Rotina");
        }
        
        Map<String, Object> map = (Map<String, Object>) jsonObj;
        
        String nome = getStringFromMap(map, "nome", "");
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da rotina não pode ser vazio");
        }
        
        // Converte ações
        Object acoesObj = map.get("acoes");
        List<Acao> acoes = new ArrayList<>();
        if (acoesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> acoesList = (List<Object>) acoesObj;
            for (Object acaoObj : acoesList) {
                if (acaoObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> acaoMap = (Map<String, Object>) acaoObj;
                    String dispositivoId = getStringFromMap(acaoMap, "dispositivoId", "");
                    String comando = getStringFromMap(acaoMap, "comando", "");
                    Map<String, Object> parametros = new HashMap<>();
                    Object paramsObj = acaoMap.get("parametros");
                    if (paramsObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> params = (Map<String, Object>) paramsObj;
                        parametros.putAll(params);
                    }
                    acoes.add(new Acao(dispositivoId, comando, parametros));
                }
            }
        }
        
        // Converte horário
        Object horarioObj = map.get("horarioInicio");
        LocalDateTime horarioInicio = null;
        if (horarioObj != null) {
            if (horarioObj instanceof String) {
                try {
                    horarioInicio = LocalDateTime.parse((String) horarioObj);
                } catch (Exception e) {
                    // Se falhar, usa horário atual
                    horarioInicio = LocalDateTime.now();
                }
            } else {
                horarioInicio = LocalDateTime.now();
            }
        } else {
            horarioInicio = LocalDateTime.now();
        }
        
        Rotina rotina = new Rotina(nome, acoes, horarioInicio);
        
        // ID é gerado automaticamente no construtor
        // Se necessário, pode ser definido via reflection ou método setId se existir
        
        return rotina;
    }
    
    /**
     * Converte um objeto JSON (Map) para DispositivoIoT.
     */
    @SuppressWarnings("unchecked")
    private DispositivoIoT converterJsonParaDispositivo(Object jsonObj) throws IOException {
        if (!(jsonObj instanceof Map)) {
            throw new IOException("Esperado objeto JSON (Map) para converter em DispositivoIoT");
        }
        
        Map<String, Object> map = (Map<String, Object>) jsonObj;
        
        // Extrai campos comuns
        String id = getStringFromMap(map, "id", null);
        String nome = getStringFromMap(map, "nome", "");
        String descricao = getStringFromMap(map, "descricao", null);
        String comodo = getStringFromMap(map, "comodo", "");
        boolean online = getBooleanFromMap(map, "online", false);
        String tipo = getStringFromMap(map, "tipo", "Lampada");
        
        DispositivoIoT dispositivo;
        
        // Cria instância baseada no tipo
        if ("Lampada".equals(tipo)) {
            boolean ligada = getBooleanFromMap(map, "ligada", false);
            int intensidade = getIntFromMap(map, "intensidade", 0);
            int temperatura = getIntFromMap(map, "temperatura", 0);
            dispositivo = new Lampada(nome, comodo, online, ligada, intensidade, temperatura);
        } else if ("Termostato".equals(tipo)) {
            double tempAtual = getDoubleFromMap(map, "temperaturaAtual", 0.0);
            double tempDesejada = getDoubleFromMap(map, "temperaturaDesejada", 0.0);
            boolean status = getBooleanFromMap(map, "status", false);
            dispositivo = new Termostato(nome, comodo, status, tempAtual, tempDesejada);
            dispositivo.setOnline(online);
        } else if ("Sensor".equals(tipo)) {
            String tipoSensor = getStringFromMap(map, "tipoSensor", "Desconhecido");
            boolean unidadeMedida = getBooleanFromMap(map, "unidadeMedida", false);
            double valor = getDoubleFromMap(map, "valor", 0.0);
            dispositivo = new Sensor(nome, comodo, online, tipoSensor, unidadeMedida, valor);
        } else {
            // Fallback: cria Lampada
            dispositivo = new Lampada(nome, comodo, online, false, 0, 0);
        }
        
        // Define ID e descrição se fornecidos
        if (id != null) {
            dispositivo.setId(id);
        }
        if (descricao != null) {
            dispositivo.setDescricao(descricao);
        }
        
        return dispositivo;
    }
    
    // Métodos auxiliares para extrair valores do Map
    private String getStringFromMap(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        return value.toString();
    }
    
    private boolean getBooleanFromMap(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) return Boolean.parseBoolean((String) value);
        return defaultValue;
    }
    
    private int getIntFromMap(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    private double getDoubleFromMap(Map<String, Object> map, String key, double defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * Handler para executar ação (POST /api/dispositivos/acao)
     */
    private class ExecutarAcaoHandler extends BaseHandler {
        @Override
        protected Object processarRequest(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                throw new IllegalArgumentException("Método deve ser POST");
            }
            
            // JSON array: [deviceId, comando]
            Object data = lerCorpoRequisicaoJson(exchange);
            if (data == null) {
                throw new IllegalArgumentException("Corpo da requisição não pode ser vazio");
            }
            
            if (!(data instanceof List)) {
                throw new IllegalArgumentException("Esperado array JSON: [deviceId, comando]");
            }
            
            @SuppressWarnings("unchecked")
            List<Object> args = (List<Object>) data;
            if (args.size() < 2) {
                throw new IllegalArgumentException("Array JSON deve conter [deviceId, comando]");
            }
            
            String deviceId = args.get(0).toString();
            String comando = args.get(1).toString();
            
            if (deviceId == null || deviceId.trim().isEmpty()) {
                throw new IllegalArgumentException("ID do dispositivo não pode ser vazio");
            }
            if (comando == null || comando.trim().isEmpty()) {
                throw new IllegalArgumentException("Comando não pode ser vazio");
            }
            
            DispositivoIoT resultado = smartHomeService.executarAcao(deviceId, comando);
            if (resultado == null) {
                throw new IOException("Dispositivo não encontrado: " + deviceId);
            }
            
            return resultado;
        }
    }
    
    /**
     * Handler para listar rotinas (GET /api/rotinas)
     */
    private class RotinasHandler extends BaseHandler {
        @Override
        protected Object processarRequest(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                throw new IllegalArgumentException("Método deve ser GET");
            }
            return smartHomeService.listarRotinas();
        }
    }
    
    /**
     * Handler para criar rotina (POST /api/rotinas/criar)
     */
    private class CriarRotinaHandler extends BaseHandler {
        @Override
        protected Object processarRequest(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                throw new IllegalArgumentException("Método deve ser POST");
            }
            
            // JSON: objeto Rotina
            Object data = lerCorpoRequisicaoJson(exchange);
            if (data == null) {
                throw new IllegalArgumentException("Corpo da requisição não pode ser vazio");
            }
            
            if (!(data instanceof Map)) {
                throw new IllegalArgumentException("Esperado objeto JSON representando uma Rotina");
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> rotinaJson = (Map<String, Object>) data;
            Rotina rotina = converterJsonParaRotina(rotinaJson);
            
            return smartHomeService.criarRotina(rotina);
        }
    }
    
    /**
     * Handler para listar alertas (GET /api/alertas)
     */
    private class AlertasHandler extends BaseHandler {
        @Override
        protected Object processarRequest(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                throw new IllegalArgumentException("Método deve ser GET");
            }
            return smartHomeService.listarAlertas();
        }
    }
    
    /**
     * Handler para obter cômodo (POST /api/comodos/obter)
     */
    private class ObterComodoHandler extends BaseHandler {
        @Override
        protected Object processarRequest(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                throw new IllegalArgumentException("Método deve ser POST");
            }
            
            Object data = lerCorpoRequisicaoJson(exchange);
            if (data == null) {
                throw new IllegalArgumentException("Corpo da requisição não pode ser vazio");
            }
            
            String nomeComodo;
            if (data instanceof String) {
                nomeComodo = (String) data;
            } else {
                nomeComodo = data.toString();
            }
            
            if (nomeComodo == null || nomeComodo.trim().isEmpty()) {
                throw new IllegalArgumentException("Nome do cômodo não pode ser vazio");
            }
            
            Comodo comodo = smartHomeService.obterComodo(nomeComodo);
            if (comodo == null) {
                // 404 Not Found
                throw new NotFoundException("Cômodo não encontrado: " + nomeComodo);
            }
            
            return comodo;
        }
    }
    
    /**
     * Exceção customizada para recursos não encontrados (404).
     */
    private static class NotFoundException extends IOException {
        public NotFoundException(String message) {
            super(message);
        }
    }
    
    /**
     * Handler para endpoint raiz
     */
    private class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Servidor Smart Home API - Serviço Remoto\n" +
                            "Use os endpoints /api/* para acessar os serviços.";
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes("UTF-8").length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes("UTF-8"));
            }
            exchange.close();
        }
    }
    
    public static void main(String[] args) {
        try {
            ServidorRemotoAPI servidor = new ServidorRemotoAPI();
            servidor.iniciar();
            
            // Mantém o servidor rodando
            System.out.println("Pressione Enter para parar o servidor...");
            System.in.read();
            
            servidor.parar();
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao iniciar servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
