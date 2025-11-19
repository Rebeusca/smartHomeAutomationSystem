package smarthome.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Servidor que recebe requisições remotas usando UDP (DatagramSocket).
 * Implementa os métodos getRequest e sendReply para invocação remota.
 */
public class ServerRequestHandler {
    
    private static final int MAX_PACKET_SIZE = 65507; // Tamanho máximo UDP
    private DatagramSocket serverSocket;
    private int serverPort;
    
    /**
     * Cria um ServerRequestHandler que escuta na porta especificada.
     */
    public ServerRequestHandler(int serverPort) throws SocketException {
        this.serverPort = serverPort;
        this.serverSocket = new DatagramSocket(serverPort);
        System.out.println("[SERVIDOR] Escutando na porta " + serverPort);
    }
    
    /**
     * Obtém uma requisição de um cliente através da porta servidora.
     * Retorna um array com [RemoteObjectRef, methodId, arguments]
     * 
     * @return Array contendo [RemoteObjectRef, methodId, arguments]
     * @throws IOException Se houver erro ao receber a requisição
     */
    public Object[] getRequest() throws IOException {
        byte[] buffer = new byte[MAX_PACKET_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        
        System.out.println("[SERVIDOR] Aguardando requisição...");
        serverSocket.receive(packet);
        
        InetAddress clientHost = packet.getAddress();
        int clientPort = packet.getPort();
        
        System.out.println("[SERVIDOR] Requisição recebida de " + clientHost + ":" + clientPort);
        
        // Extrai dados do pacote
        byte[] data = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
        
        // Desempacota RemoteObjectRef
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(data);
        java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.allocate(Integer.BYTES);
        
        byte[] refSizeBytes = new byte[Integer.BYTES];
        bais.read(refSizeBytes);
        int refSize = java.nio.ByteBuffer.wrap(refSizeBytes).getInt();
        
        RemoteObjectRef remoteRef = null;
        if (refSize > 0) {
            byte[] refData = new byte[refSize];
            bais.read(refData);
            try {
                remoteRef = (RemoteObjectRef) MessageMarshaller.deserialize(refData);
            } catch (ClassNotFoundException e) {
                throw new IOException("Erro ao deserializar RemoteObjectRef", e);
            }
        }
        
        // Desempacota requisição (methodId + arguments)
        byte[] requestData = new byte[bais.available()];
        bais.read(requestData);
        Object[] requestParts = MessageMarshaller.unmarshalRequest(requestData);
        int methodId = (Integer) requestParts[0];
        byte[] arguments = (byte[]) requestParts[1];
        
        // Armazena informações do cliente para resposta
        return new Object[]{remoteRef, methodId, arguments, clientHost, clientPort};
    }
    
    /**
     * Envia a mensagem de resposta para o cliente, endereçando-a a seu endereço IP e porta.
     * 
     * @param reply Resposta serializada
     * @param clientHost Endereço IP do cliente
     * @param clientPort Porta do cliente
     * @throws IOException Se houver erro ao enviar a resposta
     */
    public void sendReply(byte[] reply, InetAddress clientHost, int clientPort) throws IOException {
        // Empacota resposta (status 0 = sucesso)
        byte[] replyData = MessageMarshaller.marshalReply(0, reply);
        
        // Cria pacote de resposta
        DatagramPacket replyPacket = new DatagramPacket(
            replyData,
            replyData.length,
            clientHost,
            clientPort
        );
        
        // Envia resposta
        System.out.println("[SERVIDOR] Enviando resposta para " + clientHost + ":" + clientPort);
        serverSocket.send(replyPacket);
        System.out.println("[SERVIDOR] Resposta enviada com sucesso");
    }
    
    /**
     * Fecha o socket do servidor.
     */
    public void close() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
            System.out.println("[SERVIDOR] Socket fechado");
        }
    }
    
    /**
     * Retorna a porta do servidor.
     */
    public int getServerPort() {
        return serverPort;
    }
}

