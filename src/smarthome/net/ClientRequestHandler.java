package smarthome.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

/**
 * Cliente que envia requisições remotas usando UDP (DatagramSocket).
 * Implementa o método doOperation para invocação remota.
 */
public class ClientRequestHandler {
    
    private static final int TIMEOUT_MS = 5000; // 5 segundos
    private static final int MAX_PACKET_SIZE = 65507; // Tamanho máximo UDP
    
    /**
     * Envia uma mensagem de requisição para o objeto remoto e retorna a resposta.
     * 
     * @param o Referência ao objeto remoto
     * @param methodId ID do método a ser chamado
     * @param arguments Argumentos serializados para o método
     * @return Resposta serializada do servidor
     * @throws IOException Se houver erro na comunicação
     */
    public byte[] doOperation(RemoteObjectRef o, int methodId, byte[] arguments) throws IOException {
        if (o == null) {
            throw new IllegalArgumentException("RemoteObjectRef não pode ser null");
        }
        
        // Empacota a requisição
        byte[] requestData = MessageMarshaller.marshalRequest(methodId, arguments);
        
        // Cria socket UDP
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            
            // Empacota RemoteObjectRef junto com a requisição
            byte[] remoteRefData = MessageMarshaller.serialize(o);
            byte[] fullRequest = combineRequestData(remoteRefData, requestData);
            
            // Cria pacote de requisição
            DatagramPacket requestPacket = new DatagramPacket(
                fullRequest,
                fullRequest.length,
                o.getHost(),
                o.getPort()
            );
            
            // Envia requisição
            System.out.println("[CLIENTE] Enviando requisição para " + o.getHost() + ":" + o.getPort() + 
                             " (methodId=" + methodId + ")");
            socket.send(requestPacket);
            
            // Recebe resposta
            byte[] responseBuffer = new byte[MAX_PACKET_SIZE];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
            
            try {
                socket.receive(responsePacket);
                
                // Extrai dados da resposta
                byte[] responseData = new byte[responsePacket.getLength()];
                System.arraycopy(responsePacket.getData(), 0, responseData, 0, responsePacket.getLength());
                
                // Desempacota resposta
                Object[] replyParts = MessageMarshaller.unmarshalReply(responseData);
                int status = (Integer) replyParts[0];
                byte[] reply = (byte[]) replyParts[1];
                
                if (status != 0) { // 0 = sucesso
                    throw new IOException("Erro no servidor: status=" + status);
                }
                
                System.out.println("[CLIENTE] Resposta recebida com sucesso");
                return reply;
                
            } catch (SocketTimeoutException e) {
                throw new IOException("Timeout ao aguardar resposta do servidor", e);
            }
        }
    }
    
    /**
     * Combina os dados da referência remota com os dados da requisição.
     * Formato: [4 bytes: tamanho RemoteObjectRef][bytes: RemoteObjectRef][bytes: requestData]
     */
    private byte[] combineRequestData(byte[] remoteRefData, byte[] requestData) throws IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(Integer.BYTES);
        
        // Tamanho do RemoteObjectRef
        int refSize = (remoteRefData != null) ? remoteRefData.length : 0;
        buffer.putInt(refSize);
        baos.write(buffer.array());
        
        // Dados do RemoteObjectRef
        if (remoteRefData != null && remoteRefData.length > 0) {
            baos.write(remoteRefData);
        }
        
        // Dados da requisição
        if (requestData != null && requestData.length > 0) {
            baos.write(requestData);
        }
        
        return baos.toByteArray();
    }
}

