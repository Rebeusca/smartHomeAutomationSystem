package smarthome.net;

import smarthome.pojos.DispositivoIoT;
import smarthome.pojos.Lampada;
import smarthome.pojos.Sensor;
import smarthome.pojos.Termostato;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servidor remoto que processa requisições de clientes.
 * 
 * Operações suportadas:
 * - LISTAR_DISPOSITIVOS: Retorna todos os dispositivos
 * - OBTER_DISPOSITIVO: Retorna um dispositivo específico por ID
 * - ATUALIZAR_DISPOSITIVO: Atualiza um dispositivo
 * - EXECUTAR_ACAO: Executa uma ação em um dispositivo
 */
public class ServidorRemoto {
    
    private static final int PORTA = 54321;
    private Map<String, DispositivoIoT> dispositivos; // Banco de dados em memória
    
    public ServidorRemoto() {
        this.dispositivos = new HashMap<>();
        inicializarDispositivos();
    }
    
    private void inicializarDispositivos() {
        // Cria alguns dispositivos de exemplo
        Lampada l1 = new Lampada("Luz Sala", "Sala", true, true, 80, 3000);
        Termostato t1 = new Termostato("Ar Condicionado", "Quarto", true, 24.0, 22.0);
        Sensor s1 = new Sensor("Sensor Movimento", "Corredor", true, "Movimento", false, 0.0);
        
        dispositivos.put(l1.getId(), l1);
        dispositivos.put(t1.getId(), t1);
        dispositivos.put(s1.getId(), s1);
    }
    
    public void iniciar() {
        System.out.println("=== Servidor Remoto Smart Home ===");
        System.out.println("Servidor iniciado na porta " + PORTA);
        System.out.println("Aguardando conexões de clientes...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("\n[CONEXÃO] Cliente conectado: " + clientSocket.getRemoteSocketAddress());
                
                // Processa cada cliente em uma thread separada (ou sequencialmente)
                processarCliente(clientSocket);
            }
            
        } catch (IOException e) {
            System.err.println("[ERRO] Falha no servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void processarCliente(Socket clientSocket) {
        try (InputStream is = clientSocket.getInputStream();
             OutputStream os = clientSocket.getOutputStream();
             MensagemInputStream in = new MensagemInputStream(is);
             MensagemOutputStream out = new MensagemOutputStream(os)) {
            
            // Desempacota a mensagem de requisição
            MensagemRequest request = in.lerRequest();
            System.out.println("[REQUEST] " + request);
            
            // Processa a requisição
            MensagemReply reply = processarRequest(request);
            System.out.println("[REPLY] " + reply);
            
            // Empacota e envia a resposta
            out.escreverReply(reply);
            System.out.println("[OK] Resposta enviada ao cliente");
            
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao processar cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private MensagemReply processarRequest(MensagemRequest request) {
        MensagemReply reply = new MensagemReply();
        
        try {
            switch (request.getTipoOperacao()) {
                case LISTAR_DISPOSITIVOS:
                    reply = processarListarDispositivos();
                    break;
                    
                case OBTER_DISPOSITIVO:
                    reply = processarObterDispositivo(request.getDispositivoId());
                    break;
                    
                case ATUALIZAR_DISPOSITIVO:
                    reply = processarAtualizarDispositivo(request.getDispositivoId(), request.getDados());
                    break;
                    
                case EXECUTAR_ACAO:
                    reply = processarExecutarAcao(request.getDispositivoId(), request.getDados());
                    break;
                    
                default:
                    reply.setStatus(MensagemReply.Status.ERRO);
                    reply.setMensagem("Operação não suportada: " + request.getTipoOperacao());
            }
        } catch (Exception e) {
            reply.setStatus(MensagemReply.Status.ERRO);
            reply.setMensagem("Erro ao processar requisição: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reply;
    }
    
    private MensagemReply processarListarDispositivos() {
        MensagemReply reply = new MensagemReply();
        reply.setStatus(MensagemReply.Status.SUCESSO);
        reply.setMensagem("Lista de dispositivos obtida com sucesso");
        
        List<DispositivoIoT> lista = new ArrayList<>(dispositivos.values());
        reply.setDispositivos(lista.toArray(new DispositivoIoT[0]));
        
        return reply;
    }
    
    private MensagemReply processarObterDispositivo(String dispositivoId) {
        MensagemReply reply = new MensagemReply();
        
        if (dispositivoId == null || dispositivoId.isEmpty()) {
            reply.setStatus(MensagemReply.Status.ERRO);
            reply.setMensagem("ID do dispositivo não fornecido");
            return reply;
        }
        
        DispositivoIoT dispositivo = dispositivos.get(dispositivoId);
        
        if (dispositivo == null) {
            reply.setStatus(MensagemReply.Status.DISPOSITIVO_NAO_ENCONTRADO);
            reply.setMensagem("Dispositivo não encontrado: " + dispositivoId);
        } else {
            reply.setStatus(MensagemReply.Status.SUCESSO);
            reply.setMensagem("Dispositivo obtido com sucesso");
            reply.setDispositivo(dispositivo);
        }
        
        return reply;
    }
    
    private MensagemReply processarAtualizarDispositivo(String dispositivoId, String dados) {
        MensagemReply reply = new MensagemReply();
        
        if (dispositivoId == null || dispositivoId.isEmpty()) {
            reply.setStatus(MensagemReply.Status.ERRO);
            reply.setMensagem("ID do dispositivo não fornecido");
            return reply;
        }
        
        DispositivoIoT dispositivo = dispositivos.get(dispositivoId);
        
        if (dispositivo == null) {
            reply.setStatus(MensagemReply.Status.DISPOSITIVO_NAO_ENCONTRADO);
            reply.setMensagem("Dispositivo não encontrado: " + dispositivoId);
            return reply;
        }
        
        // Simula atualização (em um caso real, processaria os dados)
        if (dados != null && dados.contains("online=true")) {
            dispositivo.setOnline(true);
        } else if (dados != null && dados.contains("online=false")) {
            dispositivo.setOnline(false);
        }
        
        dispositivos.put(dispositivoId, dispositivo);
        
        reply.setStatus(MensagemReply.Status.SUCESSO);
        reply.setMensagem("Dispositivo atualizado com sucesso");
        reply.setDispositivo(dispositivo);
        
        return reply;
    }
    
    private MensagemReply processarExecutarAcao(String dispositivoId, String dados) {
        MensagemReply reply = new MensagemReply();
        
        if (dispositivoId == null || dispositivoId.isEmpty()) {
            reply.setStatus(MensagemReply.Status.ERRO);
            reply.setMensagem("ID do dispositivo não fornecido");
            return reply;
        }
        
        DispositivoIoT dispositivo = dispositivos.get(dispositivoId);
        
        if (dispositivo == null) {
            reply.setStatus(MensagemReply.Status.DISPOSITIVO_NAO_ENCONTRADO);
            reply.setMensagem("Dispositivo não encontrado: " + dispositivoId);
            return reply;
        }
        
        // Simula execução de ação
        reply.setStatus(MensagemReply.Status.SUCESSO);
        reply.setMensagem("Ação executada com sucesso no dispositivo: " + dispositivoId);
        reply.setDispositivo(dispositivo);
        
        return reply;
    }
    
    public static void main(String[] args) {
        ServidorRemoto servidor = new ServidorRemoto();
        servidor.iniciar();
    }
}

