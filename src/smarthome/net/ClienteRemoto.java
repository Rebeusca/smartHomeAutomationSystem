package smarthome.net;

import smarthome.pojos.DispositivoIoT;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Cliente remoto que se comunica com o servidor.
 * 
 * Exemplo de uso:
 * - Conecta ao servidor
 * - Envia requisições (empacotadas)
 * - Recebe respostas (desempacotadas)
 */
public class ClienteRemoto {
    
    private final String host;
    private final int porta;
    
    public ClienteRemoto(String host, int porta) {
        this.host = host;
        this.porta = porta;
    }
    
    /**
     * Envia uma requisição e recebe a resposta
     */
    public MensagemReply enviarRequest(MensagemRequest request) throws IOException {
        try (Socket socket = new Socket(host, porta);
             InputStream is = socket.getInputStream();
             OutputStream os = socket.getOutputStream();
             MensagemOutputStream out = new MensagemOutputStream(os);
             MensagemInputStream in = new MensagemInputStream(is)) {
            
            // Empacota e envia a requisição
            System.out.println("[CLIENTE] Enviando requisição: " + request);
            out.escreverRequest(request);
            
            // Desempacota e recebe a resposta
            MensagemReply reply = in.lerReply();
            System.out.println("[CLIENTE] Resposta recebida: " + reply);
            
            return reply;
            
        } catch (IOException e) {
            System.err.println("[ERRO] Falha na comunicação: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Lista todos os dispositivos
     */
    public DispositivoIoT[] listarDispositivos() throws IOException {
        MensagemRequest request = new MensagemRequest(MensagemRequest.TipoOperacao.LISTAR_DISPOSITIVOS);
        MensagemReply reply = enviarRequest(request);
        
        if (reply.getStatus() == MensagemReply.Status.SUCESSO) {
            return reply.getDispositivos();
        } else {
            throw new IOException("Erro ao listar dispositivos: " + reply.getMensagem());
        }
    }
    
    /**
     * Obtém um dispositivo por ID
     */
    public DispositivoIoT obterDispositivo(String dispositivoId) throws IOException {
        MensagemRequest request = new MensagemRequest(
            MensagemRequest.TipoOperacao.OBTER_DISPOSITIVO, 
            dispositivoId
        );
        MensagemReply reply = enviarRequest(request);
        
        if (reply.getStatus() == MensagemReply.Status.SUCESSO) {
            return reply.getDispositivo();
        } else {
            throw new IOException("Erro ao obter dispositivo: " + reply.getMensagem());
        }
    }
    
    /**
     * Atualiza um dispositivo
     */
    public DispositivoIoT atualizarDispositivo(String dispositivoId, String dados) throws IOException {
        MensagemRequest request = new MensagemRequest(
            MensagemRequest.TipoOperacao.ATUALIZAR_DISPOSITIVO,
            dispositivoId,
            dados
        );
        MensagemReply reply = enviarRequest(request);
        
        if (reply.getStatus() == MensagemReply.Status.SUCESSO) {
            return reply.getDispositivo();
        } else {
            throw new IOException("Erro ao atualizar dispositivo: " + reply.getMensagem());
        }
    }
    
    /**
     * Executa uma ação em um dispositivo
     */
    public DispositivoIoT executarAcao(String dispositivoId, String dados) throws IOException {
        MensagemRequest request = new MensagemRequest(
            MensagemRequest.TipoOperacao.EXECUTAR_ACAO,
            dispositivoId,
            dados
        );
        MensagemReply reply = enviarRequest(request);
        
        if (reply.getStatus() == MensagemReply.Status.SUCESSO) {
            return reply.getDispositivo();
        } else {
            throw new IOException("Erro ao executar ação: " + reply.getMensagem());
        }
    }
    
    public static void main(String[] args) {
        ClienteRemoto cliente = new ClienteRemoto("localhost", 54321);
        
        try {
            System.out.println("=== Cliente Remoto Smart Home ===\n");
            
            // Teste 1: Listar dispositivos
            System.out.println("1. Listando dispositivos...");
            DispositivoIoT[] dispositivos = cliente.listarDispositivos();
            System.out.println("   Total: " + dispositivos.length + " dispositivos");
            for (DispositivoIoT d : dispositivos) {
                System.out.println("   - " + d.getId() + ": " + d.getNome());
            }
            
            if (dispositivos.length > 0) {
                String primeiroId = dispositivos[0].getId();
                
                // Teste 2: Obter dispositivo específico
                System.out.println("\n2. Obtendo dispositivo: " + primeiroId);
                DispositivoIoT dispositivo = cliente.obterDispositivo(primeiroId);
                System.out.println("   Dispositivo: " + dispositivo);
                
                // Teste 3: Atualizar dispositivo
                System.out.println("\n3. Atualizando dispositivo: " + primeiroId);
                dispositivo = cliente.atualizarDispositivo(primeiroId, "online=true");
                System.out.println("   Dispositivo atualizado: " + dispositivo);
                
                // Teste 4: Executar ação
                System.out.println("\n4. Executando ação no dispositivo: " + primeiroId);
                dispositivo = cliente.executarAcao(primeiroId, "ligar");
                System.out.println("   Ação executada no dispositivo: " + dispositivo.getId());
            }
            
        } catch (IOException e) {
            System.err.println("[ERRO] " + e.getMessage());
            e.printStackTrace();
        }
    }
}

