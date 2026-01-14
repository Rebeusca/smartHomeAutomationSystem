package smarthome.net;

import smarthome.interfaces.ISmartHomeService;
import smarthome.pojos.DispositivoIoT;
import smarthome.pojos.Rotina;
import smarthome.pojos.Alerta;
import smarthome.pojos.Comodo;
import smarthome.pojos.Acao;
import smarthome.net.JsonParser;
import smarthome.net.JsonConverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Cliente remoto que se comunica com o servidor usando API REST HTTP.
 * Implementa protocolo de requisição/resposta via HTTP.
 * Não utiliza sockets ou RMI diretamente.
 */
public class ClienteRemotoAPI {
    
    private final String baseUrl;
    
    public ClienteRemotoAPI(String host, int porta) {
        this.baseUrl = "http://" + host + ":" + porta;
    }
    
    /**
     * Executa uma requisição HTTP e retorna a resposta deserializada.
     * API REST sempre usa JSON (envio e recebimento).
     */
    private Object executarRequisicao(String endpoint, String method, Object requestBody) 
            throws IOException, ClassNotFoundException {
        
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod(method);
            connection.setDoOutput(requestBody != null);
            connection.setDoInput(true);
            
            // API REST sempre solicita JSON na resposta
            connection.setRequestProperty("Accept", "application/json");
            
            // Envia corpo da requisição se houver (sempre JSON)
            if (requestBody != null) {
                String jsonBody;
                
                if (requestBody instanceof String) {
                    // String JSON ou string simples
                    String str = (String) requestBody;
                    if (str.startsWith("\"") || str.startsWith("[") || str.startsWith("{")) {
                        // Já é JSON
                        jsonBody = str;
                    } else {
                        // String simples, converte para JSON
                        jsonBody = "\"" + str.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
                    }
                } else {
                    // Objeto complexo - converte para JSON
                    jsonBody = JsonConverter.toJson(requestBody);
                }
                
                byte[] bodyBytes = jsonBody.getBytes("UTF-8");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Content-Length", String.valueOf(bodyBytes.length));
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(bodyBytes);
                }
            }
            
            // Verifica código de resposta
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                String errorMsg = lerErro(connection);
                throw new IOException("Erro HTTP " + responseCode + ": " + errorMsg);
            }
            
            // Lê resposta JSON (servidor sempre retorna JSON)
            try (InputStream is = connection.getInputStream()) {
                byte[] responseData = lerTodosBytes(is);
                String jsonResponse = new String(responseData, "UTF-8");
                
                // Parseia JSON
                return JsonParser.parse(jsonResponse);
            }
            
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * Lê mensagem de erro da resposta HTTP.
     */
    private String lerErro(HttpURLConnection connection) {
        try (InputStream is = connection.getErrorStream()) {
            if (is != null) {
                byte[] errorData = lerTodosBytes(is);
                return new String(errorData, "UTF-8");
            }
        } catch (IOException e) {
            // Ignora erro ao ler mensagem de erro
        }
        return "Erro desconhecido";
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
    
    /**
     * Converte resposta JSON (Map/List) para objetos Java.
     * Para simplificar, mantém como Map/List e converte apenas quando necessário.
     */
    @SuppressWarnings("unchecked")
    private List<DispositivoIoT> converterJsonParaListaDispositivos(Object json) {
        if (json instanceof List) {
            List<Object> lista = (List<Object>) json;
            List<DispositivoIoT> dispositivos = new ArrayList<>();
            // Por enquanto, retorna lista vazia pois conversão completa requer mais implementação
            // Em produção, usar biblioteca JSON ou implementar conversor completo
            return dispositivos;
        }
        return new ArrayList<>();
    }
    
    /**
     * Lista todos os dispositivos usando API REST.
     */
    @SuppressWarnings("unchecked")
    public List<DispositivoIoT> listarDispositivos() throws IOException, ClassNotFoundException {
        System.out.println("[CLIENTE] GET /api/dispositivos");
        Object response = executarRequisicao("/api/dispositivos", "GET", null);
        // Por enquanto, retorna lista vazia - conversão JSON->DispositivoIoT requer implementação completa
        // Em produção, usar biblioteca JSON ou implementar conversor
        return new ArrayList<>();
    }
    
    /**
     * Obtém um dispositivo por ID usando API REST.
     */
    public DispositivoIoT obterDispositivo(String dispositivoId) throws IOException, ClassNotFoundException {
        System.out.println("[CLIENTE] POST /api/dispositivos/obter (id=" + dispositivoId + ")");
        // Envia como JSON string
        String jsonRequest = "\"" + dispositivoId.replace("\"", "\\\"") + "\"";
        Object response = executarRequisicao("/api/dispositivos/obter", "POST", jsonRequest);
        // Por enquanto, retorna null - conversão requer implementação completa
        return null;
    }
    
    /**
     * Atualiza um dispositivo usando API REST.
     * Usa serialização Java para enviar (compatibilidade).
     */
    public DispositivoIoT atualizarDispositivo(String dispositivoId, DispositivoIoT dispositivo) 
            throws IOException, ClassNotFoundException {
        System.out.println("[CLIENTE] POST /api/dispositivos/atualizar (id=" + dispositivoId + ")");
        Object[] args = {dispositivoId, dispositivo};
        Object response = executarRequisicao("/api/dispositivos/atualizar", "POST", args);
        // Por enquanto, retorna null - conversão requer implementação completa
        return null;
    }
    
    /**
     * Executa uma ação em um dispositivo usando API REST.
     */
    public DispositivoIoT executarAcao(String dispositivoId, String comando) 
            throws IOException, ClassNotFoundException {
        System.out.println("[CLIENTE] POST /api/dispositivos/acao (id=" + dispositivoId + ", comando=" + comando + ")");
        // Envia como array JSON: [deviceId, comando]
        String jsonRequest = "[\"" + dispositivoId.replace("\"", "\\\"") + "\",\"" + 
                           comando.replace("\"", "\\\"") + "\"]";
        Object response = executarRequisicao("/api/dispositivos/acao", "POST", jsonRequest);
        // Por enquanto, retorna null - conversão requer implementação completa
        return null;
    }
    
    /**
     * Lista todas as rotinas usando API REST.
     */
    @SuppressWarnings("unchecked")
    public List<Rotina> listarRotinas() throws IOException, ClassNotFoundException {
        System.out.println("[CLIENTE] GET /api/rotinas");
        Object response = executarRequisicao("/api/rotinas", "GET", null);
        // Por enquanto, retorna lista vazia
        return new ArrayList<>();
    }
    
    /**
     * Cria uma nova rotina usando API REST.
     * Usa serialização Java para enviar (compatibilidade).
     */
    public Rotina criarRotina(Rotina rotina) throws IOException, ClassNotFoundException {
        System.out.println("[CLIENTE] POST /api/rotinas/criar");
        Object response = executarRequisicao("/api/rotinas/criar", "POST", rotina);
        // Por enquanto, retorna null
        return null;
    }
    
    /**
     * Lista todos os alertas usando API REST.
     */
    @SuppressWarnings("unchecked")
    public List<Alerta> listarAlertas() throws IOException, ClassNotFoundException {
        System.out.println("[CLIENTE] GET /api/alertas");
        Object response = executarRequisicao("/api/alertas", "GET", null);
        // Por enquanto, retorna lista vazia
        return new ArrayList<>();
    }
    
    /**
     * Obtém um cômodo por nome usando API REST.
     */
    public Comodo obterComodo(String nomeComodo) throws IOException, ClassNotFoundException {
        System.out.println("[CLIENTE] POST /api/comodos/obter (nome=" + nomeComodo + ")");
        // Envia como JSON string
        String jsonRequest = "\"" + nomeComodo.replace("\"", "\\\"") + "\"";
        Object response = executarRequisicao("/api/comodos/obter", "POST", jsonRequest);
        // Por enquanto, retorna null
        return null;
    }
    
    public static void main(String[] args) {
        try {
            String host = "localhost";
            int porta = 8080;
            
            // Permite passar host e porta como argumentos
            if (args.length >= 1) {
                host = args[0];
            }
            if (args.length >= 2) {
                porta = Integer.parseInt(args[1]);
            }
            
            ClienteRemotoAPI cliente = new ClienteRemotoAPI(host, porta);
            
            System.out.println("========================================");
            System.out.println("Cliente Remoto Smart Home (API REST)");
            System.out.println("========================================");
            System.out.println("Conectado a: " + host + ":" + porta);
            System.out.println();
            
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            boolean continuar = true;
            
            while (continuar) {
                exibirMenu();
                System.out.print("Escolha uma opcao: ");
                String opcao = scanner.nextLine().trim();
                
                try {
                    switch (opcao) {
                        case "1":
                            listarDispositivos(cliente);
                            break;
                        case "2":
                            obterDispositivo(cliente, scanner);
                            break;
                        case "3":
                            atualizarDispositivo(cliente, scanner);
                            break;
                        case "4":
                            executarAcao(cliente, scanner);
                            break;
                        case "5":
                            listarRotinas(cliente);
                            break;
                        case "6":
                            criarRotina(cliente, scanner);
                            break;
                        case "7":
                            listarAlertas(cliente);
                            break;
                        case "8":
                            obterComodo(cliente, scanner);
                            break;
                        case "0":
                            continuar = false;
                            System.out.println("\nEncerrando cliente...");
                            break;
                        default:
                            System.out.println("\n[ERRO] Opcao invalida! Tente novamente.");
                    }
                } catch (Exception e) {
                    System.err.println("\n[ERRO] " + e.getMessage());
                    if (e.getCause() != null) {
                        System.err.println("Causa: " + e.getCause().getMessage());
                    }
                }
                
                if (continuar) {
                    System.out.println("\nPressione Enter para continuar...");
                    scanner.nextLine();
                }
            }
            
            scanner.close();
            
        } catch (Exception e) {
            System.err.println("[ERRO] " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void exibirMenu() {
        System.out.println("\n========================================");
        System.out.println("MENU PRINCIPAL");
        System.out.println("========================================");
        System.out.println("1. Listar dispositivos");
        System.out.println("2. Obter dispositivo por ID");
        System.out.println("3. Atualizar dispositivo");
        System.out.println("4. Executar acao em dispositivo");
        System.out.println("5. Listar rotinas");
        System.out.println("6. Criar rotina");
        System.out.println("7. Listar alertas");
        System.out.println("8. Obter comodo por nome");
        System.out.println("0. Sair");
        System.out.println("========================================");
    }
    
    private static void listarDispositivos(ClienteRemotoAPI cliente) throws Exception {
        System.out.println("\n--- Listando Dispositivos ---");
        List<DispositivoIoT> dispositivos = cliente.listarDispositivos();
        System.out.println("Total: " + dispositivos.size() + " dispositivos\n");
        
        if (dispositivos.isEmpty()) {
            System.out.println("Nenhum dispositivo encontrado.");
        } else {
            for (int i = 0; i < dispositivos.size(); i++) {
                DispositivoIoT d = dispositivos.get(i);
                System.out.println((i + 1) + ". " + d.getNome());
                System.out.println("   ID: " + d.getId());
                System.out.println("   Tipo: " + d.getClass().getSimpleName());
                System.out.println("   Online: " + (d.getOnline() ? "Sim" : "Nao"));
                System.out.println("   Comodo: " + d.getComodo());
                System.out.println();
            }
        }
    }
    
    private static void obterDispositivo(ClienteRemotoAPI cliente, java.util.Scanner scanner) throws Exception {
        System.out.println("\n--- Obter Dispositivo ---");
        System.out.print("Digite o ID do dispositivo: ");
        String id = scanner.nextLine().trim();
        
        if (id.isEmpty()) {
            System.out.println("[ERRO] ID nao pode ser vazio!");
            return;
        }
        
        DispositivoIoT dispositivo = cliente.obterDispositivo(id);
        if (dispositivo != null) {
            System.out.println("\nDispositivo encontrado:");
            System.out.println("  Nome: " + dispositivo.getNome());
            System.out.println("  ID: " + dispositivo.getId());
            System.out.println("  Tipo: " + dispositivo.getClass().getSimpleName());
            System.out.println("  Online: " + (dispositivo.getOnline() ? "Sim" : "Nao"));
            System.out.println("  Comodo: " + dispositivo.getComodo());
            System.out.println("  Descricao: " + dispositivo.getDescricao());
        } else {
            System.out.println("\n[ERRO] Dispositivo nao encontrado!");
        }
    }
    
    private static void atualizarDispositivo(ClienteRemotoAPI cliente, java.util.Scanner scanner) throws Exception {
        System.out.println("\n--- Atualizar Dispositivo ---");
        System.out.print("Digite o ID do dispositivo: ");
        String id = scanner.nextLine().trim();
        
        if (id.isEmpty()) {
            System.out.println("[ERRO] ID nao pode ser vazio!");
            return;
        }
        
        // Obtém o dispositivo atual
        DispositivoIoT dispositivo = cliente.obterDispositivo(id);
        if (dispositivo == null) {
            System.out.println("[ERRO] Dispositivo nao encontrado!");
            return;
        }
        
        System.out.println("\nDispositivo atual:");
        System.out.println("  Nome: " + dispositivo.getNome());
        System.out.println("  Online: " + (dispositivo.getOnline() ? "Sim" : "Nao"));
        
        System.out.print("\nNovo nome (Enter para manter): ");
        String novoNome = scanner.nextLine().trim();
        if (!novoNome.isEmpty()) {
            dispositivo.setNome(novoNome);
        }
        
        System.out.print("Online? (s/n, Enter para manter): ");
        String onlineStr = scanner.nextLine().trim().toLowerCase();
        if (onlineStr.equals("s")) {
            dispositivo.setOnline(true);
        } else if (onlineStr.equals("n")) {
            dispositivo.setOnline(false);
        }
        
        DispositivoIoT atualizado = cliente.atualizarDispositivo(id, dispositivo);
        if (atualizado != null) {
            System.out.println("\n[SUCESSO] Dispositivo atualizado!");
            System.out.println("  Nome: " + atualizado.getNome());
            System.out.println("  Online: " + (atualizado.getOnline() ? "Sim" : "Nao"));
        } else {
            System.out.println("\n[ERRO] Falha ao atualizar dispositivo!");
        }
    }
    
    private static void executarAcao(ClienteRemotoAPI cliente, java.util.Scanner scanner) throws Exception {
        System.out.println("\n--- Executar Acao ---");
        System.out.print("Digite o ID do dispositivo: ");
        String id = scanner.nextLine().trim();
        
        if (id.isEmpty()) {
            System.out.println("[ERRO] ID nao pode ser vazio!");
            return;
        }
        
        System.out.print("Digite o comando (ligar/desligar): ");
        String comando = scanner.nextLine().trim();
        
        if (comando.isEmpty()) {
            System.out.println("[ERRO] Comando nao pode ser vazio!");
            return;
        }
        
        DispositivoIoT resultado = cliente.executarAcao(id, comando);
        if (resultado != null) {
            System.out.println("\n[SUCESSO] Acao executada!");
            System.out.println("  Dispositivo: " + resultado.getNome());
            System.out.println("  Online: " + (resultado.getOnline() ? "Sim" : "Nao"));
        } else {
            System.out.println("\n[ERRO] Falha ao executar acao!");
        }
    }
    
    private static void listarRotinas(ClienteRemotoAPI cliente) throws Exception {
        System.out.println("\n--- Listando Rotinas ---");
        List<Rotina> rotinas = cliente.listarRotinas();
        System.out.println("Total: " + rotinas.size() + " rotinas\n");
        
        if (rotinas.isEmpty()) {
            System.out.println("Nenhuma rotina encontrada.");
        } else {
            for (int i = 0; i < rotinas.size(); i++) {
                Rotina r = rotinas.get(i);
                System.out.println((i + 1) + ". " + r.getNome());
                System.out.println("   ID: " + r.getId());
                System.out.println("   Acoes: " + r.getAcoes().size());
                if (r.getHorarioInicio() != null) {
                    System.out.println("   Horario: " + r.getHorarioInicio());
                }
                System.out.println();
            }
        }
    }
    
    private static void criarRotina(ClienteRemotoAPI cliente, java.util.Scanner scanner) throws Exception {
        System.out.println("\n--- Criar Rotina ---");
        System.out.print("Nome da rotina: ");
        String nome = scanner.nextLine().trim();
        
        if (nome.isEmpty()) {
            System.out.println("[ERRO] Nome nao pode ser vazio!");
            return;
        }
        
        // Lista dispositivos para escolher
        List<DispositivoIoT> dispositivos = cliente.listarDispositivos();
        if (dispositivos.isEmpty()) {
            System.out.println("[ERRO] Nenhum dispositivo disponivel!");
            return;
        }
        
        System.out.println("\nDispositivos disponiveis:");
        for (int i = 0; i < dispositivos.size(); i++) {
            System.out.println((i + 1) + ". " + dispositivos.get(i).getNome() + " (" + dispositivos.get(i).getId() + ")");
        }
        
        System.out.print("\nEscolha o numero do dispositivo: ");
        int escolha = Integer.parseInt(scanner.nextLine().trim()) - 1;
        
        if (escolha < 0 || escolha >= dispositivos.size()) {
            System.out.println("[ERRO] Escolha invalida!");
            return;
        }
        
        System.out.print("Comando (ligar/desligar): ");
        String comando = scanner.nextLine().trim();
        
        List<Acao> acoes = new ArrayList<>();
        acoes.add(new Acao(dispositivos.get(escolha).getId(), comando, new HashMap<>()));
        
        Rotina rotina = new Rotina(nome, acoes, java.time.LocalDateTime.now().plusHours(1));
        Rotina criada = cliente.criarRotina(rotina);
        
        if (criada != null) {
            System.out.println("\n[SUCESSO] Rotina criada!");
            System.out.println("  Nome: " + criada.getNome());
            System.out.println("  ID: " + criada.getId());
        } else {
            System.out.println("\n[ERRO] Falha ao criar rotina!");
        }
    }
    
    private static void listarAlertas(ClienteRemotoAPI cliente) throws Exception {
        System.out.println("\n--- Listando Alertas ---");
        List<Alerta> alertas = cliente.listarAlertas();
        System.out.println("Total: " + alertas.size() + " alertas\n");
        
        if (alertas.isEmpty()) {
            System.out.println("Nenhum alerta encontrado.");
        } else {
            for (int i = 0; i < alertas.size(); i++) {
                Alerta a = alertas.get(i);
                System.out.println((i + 1) + ". " + a.getTitulo());
                System.out.println("   Mensagem: " + a.getMensagem());
                System.out.println("   Comodo: " + a.getComodo());
                System.out.println();
            }
        }
    }
    
    private static void obterComodo(ClienteRemotoAPI cliente, java.util.Scanner scanner) throws Exception {
        System.out.println("\n--- Obter Comodo ---");
        System.out.print("Digite o nome do comodo: ");
        String nome = scanner.nextLine().trim();
        
        if (nome.isEmpty()) {
            System.out.println("[ERRO] Nome nao pode ser vazio!");
            return;
        }
        
        Comodo comodo = cliente.obterComodo(nome);
        if (comodo != null) {
            System.out.println("\nComodo encontrado:");
            System.out.println("  Nome: " + comodo.getNome());
            if (comodo.getDispositivos() != null) {
                System.out.println("  Dispositivos: " + comodo.getDispositivos().size());
                for (DispositivoIoT d : comodo.getDispositivos()) {
                    System.out.println("    - " + d.getNome());
                }
            }
        } else {
            System.out.println("\n[ERRO] Comodo nao encontrado!");
        }
    }
}
