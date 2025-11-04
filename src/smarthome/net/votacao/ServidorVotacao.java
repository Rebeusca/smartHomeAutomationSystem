package smarthome.net.votacao;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servidor de votação multi-threaded.
 * Gerencia login, candidatos, votos e resultados.
 */
public class ServidorVotacao {
    
    private static final int PORTA_TCP = 54322;
    
    // Dados compartilhados (thread-safe)
    private final Map<String, Eleitor> eleitores;
    private final Map<String, Candidato> candidatos;
    private final Map<String, Eleitor> eleitoresLogados; // SessionId -> Eleitor
    private final Map<String, String> sessionEleitores; // Username -> SessionId
    
    // Estado da votação
    private boolean votacaoIniciada = false;
    private boolean votacaoEncerrada = false;
    private long prazoFinal = 0; // timestamp
    private final Object lock = new Object();
    
    public ServidorVotacao() {
        this.eleitores = new ConcurrentHashMap<>();
        this.candidatos = new ConcurrentHashMap<>();
        this.eleitoresLogados = new ConcurrentHashMap<>();
        this.sessionEleitores = new ConcurrentHashMap<>();
        inicializarDados();
    }
    
    private void inicializarDados() {
        // Cria alguns eleitores de exemplo
        eleitores.put("eleitor1", new Eleitor("eleitor1", "senha1", false));
        eleitores.put("eleitor2", new Eleitor("eleitor2", "senha2", false));
        eleitores.put("eleitor3", new Eleitor("eleitor3", "senha3", false));
        
        // Cria administradores
        eleitores.put("admin", new Eleitor("admin", "admin123", true));
        eleitores.put("admin2", new Eleitor("admin2", "admin456", true));
        
        // Cria alguns candidatos iniciais
        candidatos.put("cand1", new Candidato("cand1", "João Silva"));
        candidatos.put("cand2", new Candidato("cand2", "Maria Santos"));
        candidatos.put("cand3", new Candidato("cand3", "Pedro Oliveira"));
    }
    
    public void iniciar() {
        System.out.println("=== Servidor de Votação ===");
        System.out.println("Servidor TCP iniciado na porta " + PORTA_TCP);
        System.out.println("Aguardando conexões...\n");
        
        try (ServerSocket serverSocket = new ServerSocket(PORTA_TCP)) {
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[CONEXÃO] Cliente conectado: " + clientSocket.getRemoteSocketAddress());
                
                // Processa cada cliente em uma thread separada
                Thread clientThread = new Thread(() -> processarCliente(clientSocket));
                clientThread.start();
            }
            
        } catch (IOException e) {
            System.err.println("[ERRO] Falha no servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void processarCliente(Socket clientSocket) {
        try (InputStream is = clientSocket.getInputStream();
             OutputStream os = clientSocket.getOutputStream();
             VotacaoInputStream in = new VotacaoInputStream(is);
             VotacaoOutputStream out = new VotacaoOutputStream(os)) {
            
            // Desempacota a mensagem de requisição
            VotacaoRequest request = in.lerRequest();
            System.out.println("[REQUEST] " + request.getTipoOperacao() + " de " + clientSocket.getRemoteSocketAddress());
            
            // Processa a requisição
            VotacaoReply reply = processarRequest(request, clientSocket.getRemoteSocketAddress().toString());
            System.out.println("[REPLY] " + reply.getStatus());
            
            // Empacota e envia a resposta
            out.escreverReply(reply);
            
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao processar cliente: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Ignora
            }
        }
    }
    
    private VotacaoReply processarRequest(VotacaoRequest request, String sessionId) {
        try {
            synchronized (lock) {
                switch (request.getTipoOperacao()) {
                    case LOGIN:
                        return processarLogin(request, sessionId);
                        
                    case LISTAR_CANDIDATOS:
                        return processarListarCandidatos(sessionId);
                        
                    case VOTAR:
                        return processarVoto(request, sessionId);
                        
                    case ADICIONAR_CANDIDATO:
                        return processarAdicionarCandidato(request, sessionId);
                        
                    case REMOVER_CANDIDATO:
                        return processarRemoverCandidato(request, sessionId);
                        
                    case OBTER_RESULTADOS:
                        return processarObterResultados(sessionId);
                        
                    case INICIAR_VOTACAO:
                        return processarIniciarVotacao(request, sessionId);
                        
                    case ENCERRAR_VOTACAO:
                        return processarEncerrarVotacao(sessionId);
                        
                    default:
                        return new VotacaoReply(VotacaoReply.Status.ERRO, "Operação não suportada");
                }
            }
        } catch (Exception e) {
            return new VotacaoReply(VotacaoReply.Status.ERRO, "Erro: " + e.getMessage());
        }
    }
    
    private VotacaoReply processarLogin(VotacaoRequest request, String sessionId) {
        String[] loginData = VotacaoJsonSerializer.parseLoginJson(request.getDados());
        String username = loginData[0];
        String senha = loginData[1];
        
        if (username == null || senha == null) {
            return new VotacaoReply(VotacaoReply.Status.LOGIN_INVALIDO, "Dados de login inválidos");
        }
        
        Eleitor eleitor = eleitores.get(username);
        if (eleitor == null || !eleitor.validarSenha(senha)) {
            return new VotacaoReply(VotacaoReply.Status.LOGIN_INVALIDO, "Usuário ou senha incorretos");
        }
        
        // Verifica se já está logado em outra sessão
        String sessaoAnterior = sessionEleitores.get(username);
        if (sessaoAnterior != null) {
            eleitoresLogados.remove(sessaoAnterior);
        }
        
        // Cria nova sessão
        eleitoresLogados.put(sessionId, eleitor);
        sessionEleitores.put(username, sessionId);
        
        String tipoUsuario = eleitor.isAdmin() ? "Administrador" : "Eleitor";
        String dados = "{\"username\":\"" + username + "\",\"isAdmin\":" + eleitor.isAdmin() + "}";
        return new VotacaoReply(VotacaoReply.Status.SUCESSO, 
            "Login realizado com sucesso. Bem-vindo, " + tipoUsuario + "!", dados);
    }
    
    private VotacaoReply processarListarCandidatos(String sessionId) {
        Eleitor eleitor = eleitoresLogados.get(sessionId);
        if (eleitor == null) {
            return new VotacaoReply(VotacaoReply.Status.NAO_AUTORIZADO, "É necessário fazer login primeiro");
        }
        
        if (!votacaoIniciada) {
            return new VotacaoReply(VotacaoReply.Status.VOTACAO_NAO_INICIADA, 
                "A votação ainda não foi iniciada");
        }
        
        if (votacaoEncerrada) {
            return new VotacaoReply(VotacaoReply.Status.VOTACAO_ENCERRADA, 
                "A votação já foi encerrada");
        }
        
        List<Candidato> lista = new ArrayList<>(candidatos.values());
        // Remove a contagem de votos para não influenciar a escolha
        List<Candidato> candidatosSemVotos = new ArrayList<>();
        for (Candidato c : lista) {
            Candidato copia = new Candidato(c.getId(), c.getNome());
            candidatosSemVotos.add(copia);
        }
        
        String dados = VotacaoJsonSerializer.candidatosToJson(candidatosSemVotos);
        return new VotacaoReply(VotacaoReply.Status.SUCESSO, "Lista de candidatos obtida", dados);
    }
    
    private VotacaoReply processarVoto(VotacaoRequest request, String sessionId) {
        Eleitor eleitor = eleitoresLogados.get(sessionId);
        if (eleitor == null) {
            return new VotacaoReply(VotacaoReply.Status.NAO_AUTORIZADO, "É necessário fazer login primeiro");
        }
        
        if (eleitor.isAdmin()) {
            return new VotacaoReply(VotacaoReply.Status.NAO_AUTORIZADO, "Administradores não podem votar");
        }
        
        if (!votacaoIniciada) {
            return new VotacaoReply(VotacaoReply.Status.VOTACAO_NAO_INICIADA, 
                "A votação ainda não foi iniciada");
        }
        
        if (votacaoEncerrada || System.currentTimeMillis() > prazoFinal) {
            votacaoEncerrada = true;
            return new VotacaoReply(VotacaoReply.Status.VOTACAO_ENCERRADA, 
                "A votação foi encerrada. Prazo final atingido.");
        }
        
        if (eleitor.isJaVotou()) {
            return new VotacaoReply(VotacaoReply.Status.JA_VOTOU, "Você já votou nesta eleição");
        }
        
        String candidatoId = VotacaoJsonSerializer.parseVotoJson(request.getDados());
        if (candidatoId == null) {
            return new VotacaoReply(VotacaoReply.Status.ERRO, "ID do candidato não fornecido");
        }
        
        Candidato candidato = candidatos.get(candidatoId);
        if (candidato == null) {
            return new VotacaoReply(VotacaoReply.Status.CANDIDATO_NAO_ENCONTRADO, 
                "Candidato não encontrado: " + candidatoId);
        }
        
        // Registra o voto
        candidato.adicionarVoto();
        eleitor.setJaVotou(true);
        
        return new VotacaoReply(VotacaoReply.Status.SUCESSO, 
            "Voto registrado com sucesso para: " + candidato.getNome());
    }
    
    private VotacaoReply processarAdicionarCandidato(VotacaoRequest request, String sessionId) {
        Eleitor eleitor = eleitoresLogados.get(sessionId);
        if (eleitor == null || !eleitor.isAdmin()) {
            return new VotacaoReply(VotacaoReply.Status.NAO_AUTORIZADO, 
                "Apenas administradores podem adicionar candidatos");
        }
        
        String[] candData = VotacaoJsonSerializer.parseCandidatoJson(request.getDados());
        String id = candData[0];
        String nome = candData[1];
        
        if (id == null || nome == null) {
            return new VotacaoReply(VotacaoReply.Status.ERRO, "Dados do candidato inválidos");
        }
        
        if (candidatos.containsKey(id)) {
            return new VotacaoReply(VotacaoReply.Status.ERRO, "Candidato com ID já existe: " + id);
        }
        
        candidatos.put(id, new Candidato(id, nome));
        return new VotacaoReply(VotacaoReply.Status.SUCESSO, 
            "Candidato adicionado com sucesso: " + nome);
    }
    
    private VotacaoReply processarRemoverCandidato(VotacaoRequest request, String sessionId) {
        Eleitor eleitor = eleitoresLogados.get(sessionId);
        if (eleitor == null || !eleitor.isAdmin()) {
            return new VotacaoReply(VotacaoReply.Status.NAO_AUTORIZADO, 
                "Apenas administradores podem remover candidatos");
        }
        
        String candidatoId = VotacaoJsonSerializer.parseVotoJson(request.getDados());
        if (candidatoId == null) {
            return new VotacaoReply(VotacaoReply.Status.ERRO, "ID do candidato não fornecido");
        }
        
        Candidato removido = candidatos.remove(candidatoId);
        if (removido == null) {
            return new VotacaoReply(VotacaoReply.Status.CANDIDATO_NAO_ENCONTRADO, 
                "Candidato não encontrado: " + candidatoId);
        }
        
        return new VotacaoReply(VotacaoReply.Status.SUCESSO, 
            "Candidato removido com sucesso: " + removido.getNome());
    }
    
    private VotacaoReply processarObterResultados(String sessionId) {
        Eleitor eleitor = eleitoresLogados.get(sessionId);
        if (eleitor == null) {
            return new VotacaoReply(VotacaoReply.Status.NAO_AUTORIZADO, "É necessário fazer login primeiro");
        }
        
        if (!votacaoEncerrada && System.currentTimeMillis() <= prazoFinal) {
            return new VotacaoReply(VotacaoReply.Status.ERRO, 
                "A votação ainda não foi encerrada. Resultados disponíveis apenas após o encerramento.");
        }
        
        ResultadoVotacao resultado = calcularResultados();
        String dados = VotacaoJsonSerializer.resultadoToJson(resultado);
        return new VotacaoReply(VotacaoReply.Status.SUCESSO, "Resultados obtidos com sucesso", dados);
    }
    
    private VotacaoReply processarIniciarVotacao(VotacaoRequest request, String sessionId) {
        Eleitor eleitor = eleitoresLogados.get(sessionId);
        if (eleitor == null || !eleitor.isAdmin()) {
            return new VotacaoReply(VotacaoReply.Status.NAO_AUTORIZADO, 
                "Apenas administradores podem iniciar a votação");
        }
        
        if (votacaoIniciada && !votacaoEncerrada) {
            return new VotacaoReply(VotacaoReply.Status.ERRO, "A votação já está em andamento");
        }
        
        // Parse da duração (em minutos) - padrão 5 minutos
        int duracaoMinutos = 5;
        if (request.getDados() != null && !request.getDados().isEmpty()) {
            try {
                String duracao = request.getDados().replace("{", "").replace("}", "")
                    .replace("\"", "").replace("duracaoMinutos:", "");
                duracaoMinutos = Integer.parseInt(duracao.trim());
            } catch (Exception e) {
                // Usa padrão
            }
        }
        
        votacaoIniciada = true;
        votacaoEncerrada = false;
        prazoFinal = System.currentTimeMillis() + (duracaoMinutos * 60 * 1000L);
        
        // Reset dos votos
        for (Candidato c : candidatos.values()) {
            c.setVotos(0);
        }
        for (Eleitor e : eleitores.values()) {
            e.setJaVotou(false);
        }
        
        return new VotacaoReply(VotacaoReply.Status.SUCESSO, 
            String.format("Votação iniciada! Prazo: %d minutos (até %s)", 
                duracaoMinutos, new Date(prazoFinal).toString()));
    }
    
    private VotacaoReply processarEncerrarVotacao(String sessionId) {
        Eleitor eleitor = eleitoresLogados.get(sessionId);
        if (eleitor == null || !eleitor.isAdmin()) {
            return new VotacaoReply(VotacaoReply.Status.NAO_AUTORIZADO, 
                "Apenas administradores podem encerrar a votação");
        }
        
        if (!votacaoIniciada || votacaoEncerrada) {
            return new VotacaoReply(VotacaoReply.Status.ERRO, "A votação não está em andamento");
        }
        
        votacaoEncerrada = true;
        ResultadoVotacao resultado = calcularResultados();
        
        return new VotacaoReply(VotacaoReply.Status.SUCESSO, 
            "Votação encerrada. Resultados calculados.", 
            VotacaoJsonSerializer.resultadoToJson(resultado));
    }
    
    private ResultadoVotacao calcularResultados() {
        ResultadoVotacao resultado = new ResultadoVotacao();
        
        int totalVotos = 0;
        for (Candidato c : candidatos.values()) {
            totalVotos += c.getVotos();
        }
        resultado.setTotalVotos(totalVotos);
        
        Candidato vencedor = null;
        int maxVotos = -1;
        
        for (Candidato c : candidatos.values()) {
            double percentual = totalVotos > 0 ? (c.getVotos() * 100.0 / totalVotos) : 0;
            resultado.getResultados().add(
                new ResultadoVotacao.ResultadoCandidato(c, c.getVotos(), percentual));
            
            if (c.getVotos() > maxVotos) {
                maxVotos = c.getVotos();
                vencedor = c;
            }
        }
        
        resultado.setVencedor(vencedor);
        return resultado;
    }
    
    public static void main(String[] args) {
        ServidorVotacao servidor = new ServidorVotacao();
        servidor.iniciar();
    }
}
