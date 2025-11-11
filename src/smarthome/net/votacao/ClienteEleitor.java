package smarthome.net.votacao;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class ClienteEleitor {
    
    private final String host;
    private final int porta;
    private Socket socket;
    private VotacaoInputStream in;
    private VotacaoOutputStream out;
    private String sessionId;
    private String username;
    private boolean logado = false;
    
    public ClienteEleitor(String host, int porta) {
        this.host = host;
        this.porta = porta;
        this.sessionId = "eleitor-" + System.currentTimeMillis();
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
        }
    }
    
    private VotacaoReply enviarRequest(VotacaoRequest request) throws IOException {
        try {
            conectar();
            out.escreverRequest(request);
            VotacaoReply reply = in.lerReply();
            return reply;
        } finally {
            desconectar();
        }
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
    
    public List<Candidato> listarCandidatos() throws IOException {
        if (!logado) {
            throw new IllegalStateException("É necessário fazer login primeiro");
        }
        
        String dados = String.format("{\"username\":\"%s\"}", username);
        VotacaoRequest request = new VotacaoRequest(VotacaoRequest.TipoOperacao.LISTAR_CANDIDATOS, dados);
        VotacaoReply reply = enviarRequest(request);
        
        if (reply.getStatus() == VotacaoReply.Status.SUCESSO) {
            return VotacaoJsonSerializer.jsonToCandidatos(reply.getDados());
        } else {
            throw new IOException("Erro ao listar candidatos: " + reply.getMensagem());
        }
    }
    
    public boolean votar(String candidatoId) throws IOException {
        if (!logado) {
            throw new IllegalStateException("É necessário fazer login primeiro");
        }
        
        String dados = String.format("{\"username\":\"%s\",\"candidatoId\":\"%s\"}", username, candidatoId);
        VotacaoRequest request = new VotacaoRequest(VotacaoRequest.TipoOperacao.VOTAR, dados);
        VotacaoReply reply = enviarRequest(request);
        
        if (reply.getStatus() == VotacaoReply.Status.SUCESSO) {
            System.out.println("[OK] " + reply.getMensagem());
            return true;
        } else {
            System.out.println("[ERRO] " + reply.getMensagem());
            return false;
        }
    }
    
    public void desconectarFinal() {
        desconectar();
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ClienteEleitor cliente = new ClienteEleitor("localhost", 54322);
        
        try {
            System.out.println("=== Cliente Eleitor ===");
            System.out.println("Conectando ao servidor...\n");
            
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Senha: ");
            String senha = scanner.nextLine();
            
            if (!cliente.login(username, senha)) {
                System.out.println("Falha no login. Encerrando...");
                return;
            }
            
            System.out.println("\n--- Lista de Candidatos ---");
            List<Candidato> candidatos = cliente.listarCandidatos();
            
            if (candidatos.isEmpty()) {
                System.out.println("Nenhum candidato disponível.");
            } else {
                System.out.println("\nCandidatos disponíveis:");
                for (int i = 0; i < candidatos.size(); i++) {
                    Candidato c = candidatos.get(i);
                    System.out.println((i + 1) + ". " + c.getNome() + " (ID: " + c.getId() + ")");
                }
                
                System.out.print("\nDigite o ID do candidato para votar: ");
                String candidatoId = scanner.nextLine();
                
                cliente.votar(candidatoId);
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
