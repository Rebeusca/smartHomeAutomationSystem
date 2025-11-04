package smarthome.net;

import smarthome.pojos.DispositivoIoT;
import smarthome.streams.DispositivoIoTOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Stream para empacotar (serializar) mensagens Request e Reply para envio via rede.
 * Protocolo:
 * [1 byte: Tipo (1=Request, 2=Reply)]
 * [4 bytes: Código da operação/status]
 * [Dados específicos conforme o tipo]
 */
public class MensagemOutputStream extends OutputStream {
    
    private final OutputStream destino;
    
    public MensagemOutputStream(OutputStream destino) {
        this.destino = destino;
    }
    
    @Override
    public void write(int b) throws IOException {
        destino.write(b);
    }
    
    /**
     * Empacota e envia uma MensagemRequest
     */
    public void escreverRequest(MensagemRequest request) throws IOException {
        // Tipo: Request (1)
        destino.write(1);
        
        // Código da operação
        writeInt(request.getTipoOperacao().getCodigo());
        
        // DispositivoId (se existir)
        if (request.getDispositivoId() != null) {
            writeString(request.getDispositivoId());
        } else {
            writeString(""); // String vazia indica null
        }
        
        // Dados adicionais (se existir)
        if (request.getDados() != null) {
            writeString(request.getDados());
        } else {
            writeString(""); // String vazia indica null
        }
        
        destino.flush();
    }
    
    /**
     * Empacota e envia uma MensagemReply
     */
    public void escreverReply(MensagemReply reply) throws IOException {
        // Tipo: Reply (2)
        destino.write(2);
        
        // Código do status
        writeInt(reply.getStatus().getCodigo());
        
        // Mensagem
        if (reply.getMensagem() != null) {
            writeString(reply.getMensagem());
        } else {
            writeString("");
        }
        
        // Array de dispositivos (se existir)
        if (reply.getDispositivos() != null) {
            writeInt(reply.getDispositivos().length);
            // Usa DispositivoIoTOutputStream para serializar os dispositivos
            DispositivoIoTOutputStream streamDispositivos = 
                new DispositivoIoTOutputStream(reply.getDispositivos(), 
                                               reply.getDispositivos().length, 
                                               destino);
            streamDispositivos.writeObjects();
        } else {
            writeInt(0); // Zero indica null
        }
        
        // Dispositivo único (se existir)
        if (reply.getDispositivo() != null) {
            destino.write(1); // 1 = existe
            DispositivoIoT[] single = {reply.getDispositivo()};
            DispositivoIoTOutputStream streamDispositivo = 
                new DispositivoIoTOutputStream(single, 1, destino);
            streamDispositivo.writeObjects();
        } else {
            destino.write(0); // 0 = não existe
        }
        
        destino.flush();
    }
    
    // Métodos auxiliares
    private void writeString(String s) throws IOException {
        if (s == null) {
            writeInt(0);
            return;
        }
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        writeInt(bytes.length);
        destino.write(bytes);
    }
    
    private void writeInt(int v) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(v);
        destino.write(buffer.array());
    }
}

