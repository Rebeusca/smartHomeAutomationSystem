package smarthome.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

/**
 * Responsável por empacotar (marshalling) e desempacotar (unmarshalling) mensagens
 * de requisição e resposta para comunicação remota.
 */
public class MessageMarshaller {
    
    /**
     * Empacota uma requisição em um array de bytes.
     * Formato: [4 bytes: methodId][4 bytes: tamanho dos argumentos][bytes: argumentos]
     */
    public static byte[] marshalRequest(int methodId, byte[] arguments) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Escreve methodId (4 bytes)
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(methodId);
        baos.write(buffer.array());
        
        // Escreve tamanho dos argumentos (4 bytes)
        int argsSize = (arguments != null) ? arguments.length : 0;
        buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(argsSize);
        baos.write(buffer.array());
        
        // Escreve argumentos
        if (arguments != null && arguments.length > 0) {
            baos.write(arguments);
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Desempacota uma requisição de um array de bytes.
     * Retorna um array com [methodId, argumentos]
     */
    public static Object[] unmarshalRequest(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        
        // Lê methodId (4 bytes)
        byte[] methodIdBytes = new byte[Integer.BYTES];
        bais.read(methodIdBytes);
        int methodId = ByteBuffer.wrap(methodIdBytes).getInt();
        
        // Lê tamanho dos argumentos (4 bytes)
        byte[] sizeBytes = new byte[Integer.BYTES];
        bais.read(sizeBytes);
        int argsSize = ByteBuffer.wrap(sizeBytes).getInt();
        
        // Lê argumentos
        byte[] arguments = null;
        if (argsSize > 0) {
            arguments = new byte[argsSize];
            bais.read(arguments);
        }
        
        return new Object[]{methodId, arguments};
    }
    
    /**
     * Empacota uma resposta em um array de bytes.
     * Formato: [4 bytes: status][4 bytes: tamanho da resposta][bytes: resposta]
     */
    public static byte[] marshalReply(int status, byte[] reply) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Escreve status (4 bytes)
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(status);
        baos.write(buffer.array());
        
        // Escreve tamanho da resposta (4 bytes)
        int replySize = (reply != null) ? reply.length : 0;
        buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(replySize);
        baos.write(buffer.array());
        
        // Escreve resposta
        if (reply != null && reply.length > 0) {
            baos.write(reply);
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Desempacota uma resposta de um array de bytes.
     * Retorna um array com [status, resposta]
     */
    public static Object[] unmarshalReply(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        
        // Lê status (4 bytes)
        byte[] statusBytes = new byte[Integer.BYTES];
        bais.read(statusBytes);
        int status = ByteBuffer.wrap(statusBytes).getInt();
        
        // Lê tamanho da resposta (4 bytes)
        byte[] sizeBytes = new byte[Integer.BYTES];
        bais.read(sizeBytes);
        int replySize = ByteBuffer.wrap(sizeBytes).getInt();
        
        // Lê resposta
        byte[] reply = null;
        if (replySize > 0) {
            reply = new byte[replySize];
            bais.read(reply);
        }
        
        return new Object[]{status, reply};
    }
    
    /**
     * Serializa um objeto para array de bytes (passagem por valor).
     */
    public static byte[] serialize(Object obj) throws IOException {
        if (obj == null) {
            return null;
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        return baos.toByteArray();
    }
    
    /**
     * Deserializa um objeto de um array de bytes (passagem por valor).
     */
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        if (data == null || data.length == 0) {
            return null;
        }
        
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        }
    }
}

