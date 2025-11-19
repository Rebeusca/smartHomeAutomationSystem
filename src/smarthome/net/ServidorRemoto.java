package smarthome.net;

import smarthome.interfaces.ISmartHomeService;
import smarthome.services.SmartHomeServiceImpl;
import smarthome.pojos.DispositivoIoT;
import smarthome.pojos.Rotina;
import smarthome.pojos.Alerta;
import smarthome.pojos.Comodo;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

/**
 * Servidor remoto que processa requisições de clientes usando Invocação Remota.
 * Utiliza getRequest e sendReply para receber requisições e enviar respostas.
 */
public class ServidorRemoto {
    
    private static final int PORTA = 54321;
    private ServerRequestHandler requestHandler;
    private ISmartHomeService smartHomeService;
    
    public ServidorRemoto() throws IOException {
        this.requestHandler = new ServerRequestHandler(PORTA);
        this.smartHomeService = new SmartHomeServiceImpl();
    }
    
    public void iniciar() {
        System.out.println("=== Servidor Remoto Smart Home (Invocação Remota) ===");
        System.out.println("Servidor iniciado na porta " + PORTA);
        System.out.println("Aguardando requisições de clientes...\n");
        
        try {
            while (true) {
                // Obtém requisição usando getRequest
                Object[] requestData = requestHandler.getRequest();
                
                @SuppressWarnings("unused")
                RemoteObjectRef remoteRef = (RemoteObjectRef) requestData[0];
                int methodId = (Integer) requestData[1];
                byte[] arguments = (byte[]) requestData[2];
                InetAddress clientHost = (InetAddress) requestData[3];
                int clientPort = (Integer) requestData[4];
                
                System.out.println("[SERVIDOR] Processando requisição: methodId=" + methodId);
                
                // Processa a requisição
                byte[] reply = processarRequest(methodId, arguments);
                
                // Envia resposta usando sendReply
                requestHandler.sendReply(reply, clientHost, clientPort);
                System.out.println("[SERVIDOR] Requisição processada com sucesso\n");
            }
        } catch (IOException e) {
            System.err.println("[ERRO] Falha no servidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            requestHandler.close();
        }
    }
    
    /**
     * Processa uma requisição baseada no methodId e argumentos.
     * Retorna a resposta serializada.
     */
    private byte[] processarRequest(int methodId, byte[] arguments) throws IOException {
        try {
            Object result = null;
            
            switch (methodId) {
                case ISmartHomeService.METHOD_LISTAR_DISPOSITIVOS:
                    result = smartHomeService.listarDispositivos();
                    break;
                    
                case ISmartHomeService.METHOD_OBTER_DISPOSITIVO:
                    String dispositivoId = (String) MessageMarshaller.deserialize(arguments);
                    result = smartHomeService.obterDispositivo(dispositivoId);
                    break;
                    
                case ISmartHomeService.METHOD_ATUALIZAR_DISPOSITIVO:
                    Object[] updateArgs = (Object[]) MessageMarshaller.deserialize(arguments);
                    String id = (String) updateArgs[0];
                    DispositivoIoT dispositivo = (DispositivoIoT) updateArgs[1];
                    result = smartHomeService.atualizarDispositivo(id, dispositivo);
                    break;
                    
                case ISmartHomeService.METHOD_EXECUTAR_ACAO:
                    Object[] actionArgs = (Object[]) MessageMarshaller.deserialize(arguments);
                    String deviceId = (String) actionArgs[0];
                    String comando = (String) actionArgs[1];
                    result = smartHomeService.executarAcao(deviceId, comando);
                    break;
                    
                case ISmartHomeService.METHOD_LISTAR_ROTINAS:
                    result = smartHomeService.listarRotinas();
                    break;
                    
                case ISmartHomeService.METHOD_CRIAR_ROTINA:
                    Rotina rotina = (Rotina) MessageMarshaller.deserialize(arguments);
                    result = smartHomeService.criarRotina(rotina);
                    break;
                    
                case ISmartHomeService.METHOD_LISTAR_ALERTAS:
                    result = smartHomeService.listarAlertas();
                    break;
                    
                case ISmartHomeService.METHOD_OBTER_COMODO:
                    String nomeComodo = (String) MessageMarshaller.deserialize(arguments);
                    result = smartHomeService.obterComodo(nomeComodo);
                    break;
                    
                default:
                    throw new IllegalArgumentException("Método não suportado: " + methodId);
            }
            
            // Serializa resultado (passagem por valor)
            return MessageMarshaller.serialize(result);
            
        } catch (ClassNotFoundException e) {
            throw new IOException("Erro ao deserializar argumentos", e);
        } catch (Exception e) {
            throw new IOException("Erro ao processar requisição: " + e.getMessage(), e);
        }
    }
    
    public static void main(String[] args) {
        try {
            ServidorRemoto servidor = new ServidorRemoto();
            servidor.iniciar();
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao iniciar servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
