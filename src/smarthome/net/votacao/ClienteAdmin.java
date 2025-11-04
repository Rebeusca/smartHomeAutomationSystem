package smarthome.net.votacao;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

/**
 * Cliente para administradores gerenciarem o sistema de votação.
 */
public class ClienteAdmin {
    
    private final String host;
    private final int porta;
    private Socket socket;
    private VotacaoInputStream in;
    private VotacaoOutputStream out;
    private String sessionId;
    private String username;
    private boolean logado = false;
    
    public ClienteAdmin(String host, int porta) {
        this.host = host;
        this.porta = porta;
        this.sessionId = "admin-" + System.currentTimeMillis();
    }
    
    private void conectar() throws IOException {
        socket = new Socket(host, porta);
        in = new VotacaoInputStream(socket.getInputStream());
        out = new VotacaoOutputStream(socket.getOutputStream());
    }
    
    private void desconectar() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Ignora
        }
    }
    
    private VotacaoReply enviarRequest(VotacaoRequest request) throws IOException {
        if (socket == null || socket.isClosed()) {
            conectar();
        }
        out.escreverRequest(request);
        return in.lerReply();
    }
    
    public boolean login(String username, String senha) throws IOException {
        this.username = username;
        String dados = String.format("{\"username\":\"%s\",\"senha\":\"%s\"}", username, senha);
        VotacaoRequest request = new VotacaoRequest(VotacaoRequest.TipoOperacao.LOGIN, dados);
        VotacaoReply reply = enviarRequest(request);
        
        if (reply.getStatus() == VotacaoReply.Status.SUCESSO) {
            logado = true;
            System.out.println("[OK] " + reply.getMensagem());
            return true;
        } else {
            System.out.println("[ERRO] " + reply.getMensagem());
            return false;
        }
    }
    
    public boolean iniciarVotacao(int duracaoMinutos) throws IOException {
        if (!logado) {
            throw new IllegalStateException("É necessário fazer login primeiro");
        }
        
        String dados = String.format("{\"duracaoMinutos\":%d}", duracaoMinutos);
        VotacaoRequest request = new VotacaoRequest(VotacaoRequest.TipoOperacao.INICIAR_VOTACAO, dados);
        VotacaoReply reply = enviarRequest(request);
        
        if (reply.getStatus() == VotacaoReply.Status.SUCESSO) {
            System.out.println("[OK] " + reply.getMensagem());
            return true;
        } else {
            System.out.println("[ERRO] " + reply.getMensagem());
            return false;
        }
    }
    
    public boolean encerrarVotacao() throws IOException {
        if (!logado) {
            throw new IllegalStateException("É necessário fazer login primeiro");
        }
        
        VotacaoRequest request = new VotacaoRequest(VotacaoRequest.TipoOperacao.ENCERRAR_VOTACAO);
        VotacaoReply reply = enviarRequest(request);
        
        if (reply.getStatus() == VotacaoReply.Status.SUCESSO) {
            System.out.println("[OK] " + reply.getMensagem());
            
            // Exibe resultados
            if (reply.getDados() != null && !reply.getDados().isEmpty()) {
                ResultadoVotacao resultado = parseResultado(reply.getDados());
                System.out.println("\n" + resultado.toString());
            }
            return true;
        } else {
            System.out.println("[ERRO] " + reply.getMensagem());
            return false;
        }
    }
    
    public boolean adicionarCandidato(String id, String nome) throws IOException {
        if (!logado) {
            throw new IllegalStateException("É necessário fazer login primeiro");
        }
        
        String dados = String.format("{\"id\":\"%s\",\"nome\":\"%s\"}", id, nome);
        VotacaoRequest request = new VotacaoRequest(VotacaoRequest.TipoOperacao.ADICIONAR_CANDIDATO, dados);
        VotacaoReply reply = enviarRequest(request);
        
        if (reply.getStatus() == VotacaoReply.Status.SUCESSO) {
            System.out.println("[OK] " + reply.getMensagem());
            return true;
        } else {
            System.out.println("[ERRO] " + reply.getMensagem());
            return false;
        }
    }
    
    public boolean removerCandidato(String id) throws IOException {
        if (!logado) {
            throw new IllegalStateException("É necessário fazer login primeiro");
        }
        
        String dados = String.format("{\"candidatoId\":\"%s\"}", id);
        VotacaoRequest request = new VotacaoRequest(VotacaoRequest.TipoOperacao.REMOVER_CANDIDATO, dados);
        VotacaoReply reply = enviarRequest(request);
        
        if (reply.getStatus() == VotacaoReply.Status.SUCESSO) {
            System.out.println("[OK] " + reply.getMensagem());
            return true;
        } else {
            System.out.println("[ERRO] " + reply.getMensagem());
            return false;
        }
    }
    
    public ResultadoVotacao obterResultados() throws IOException {
        if (!logado) {
            throw new IllegalStateException("É necessário fazer login primeiro");
        }
        
        VotacaoRequest request = new VotacaoRequest(VotacaoRequest.TipoOperacao.OBTER_RESULTADOS);
        VotacaoReply reply = enviarRequest(request);
        
        if (reply.getStatus() == VotacaoReply.Status.SUCESSO) {
            return parseResultado(reply.getDados());
        } else {
            throw new IOException("Erro ao obter resultados: " + reply.getMensagem());
        }
    }
    
    public void enviarNota(String titulo, String mensagem) {
        ServidorMulticastNotas.enviarNotaInformativa(titulo, mensagem, username);
        System.out.println("[OK] Nota enviada via multicast: " + titulo);
    }
    
    private ResultadoVotacao parseResultado(String json) {
        return VotacaoJsonSerializer.jsonToResultado(json);
    }
    
    public void desconectarFinal() {
        desconectar();
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ClienteAdmin cliente = new ClienteAdmin("localhost", 54322);
        
        try {
            System.out.println("=== Cliente Administrador ===");
            System.out.println("Conectando ao servidor...\n");
            
            // Login
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Senha: ");
            String senha = scanner.nextLine();
            
            if (!cliente.login(username, senha)) {
                System.out.println("Falha no login. Encerrando...");
                return;
            }
            
            // Menu de opções
            boolean continuar = true;
            while (continuar) {
                System.out.println("\n--- Menu Administrador ---");
                System.out.println("1. Iniciar votação");
                System.out.println("2. Encerrar votação");
                System.out.println("3. Adicionar candidato");
                System.out.println("4. Remover candidato");
                System.out.println("5. Obter resultados");
                System.out.println("6. Enviar nota informativa");
                System.out.println("0. Sair");
                System.out.print("Escolha uma opção: ");
                
                int opcao = scanner.nextInt();
                scanner.nextLine(); // consume newline
                
                switch (opcao) {
                    case 1:
                        System.out.print("Duração da votação (minutos): ");
                        int duracao = scanner.nextInt();
                        scanner.nextLine();
                        cliente.iniciarVotacao(duracao);
                        break;
                        
                    case 2:
                        cliente.encerrarVotacao();
                        break;
                        
                    case 3:
                        System.out.print("ID do candidato: ");
                        String id = scanner.nextLine();
                        System.out.print("Nome do candidato: ");
                        String nome = scanner.nextLine();
                        cliente.adicionarCandidato(id, nome);
                        break;
                        
                    case 4:
                        System.out.print("ID do candidato a remover: ");
                        String idRemover = scanner.nextLine();
                        cliente.removerCandidato(idRemover);
                        break;
                        
                    case 5:
                        ResultadoVotacao resultado = cliente.obterResultados();
                        System.out.println("\n" + resultado.toString());
                        break;
                        
                    case 6:
                        System.out.print("Título da nota: ");
                        String titulo = scanner.nextLine();
                        System.out.print("Mensagem: ");
                        String mensagem = scanner.nextLine();
                        cliente.enviarNota(titulo, mensagem);
                        break;
                        
                    case 0:
                        continuar = false;
                        break;
                        
                    default:
                        System.out.println("Opção inválida!");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cliente.desconectarFinal();
            scanner.close();
        }
    }
}
