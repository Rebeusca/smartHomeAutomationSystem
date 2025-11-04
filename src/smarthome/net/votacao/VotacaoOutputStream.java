package smarthome.net.votacao;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Stream para empacotar mensagens de votação para envio via TCP.
 */
public class VotacaoOutputStream extends OutputStream {
    
    private final OutputStream destino;
    
    public VotacaoOutputStream(OutputStream destino) {
        this.destino = destino;
    }
    
    @Override
    public void write(int b) throws IOException {
        destino.write(b);
    }
    
    /**
     * Empacota e envia uma VotacaoRequest
     */
    public void escreverRequest(VotacaoRequest request) throws IOException {
        // Tipo: Request (1)
        destino.write(1);
        
        // Código da operação
        writeInt(request.getTipoOperacao().getCodigo());
        
        // Dados (JSON)
        if (request.getDados() != null) {
            writeString(request.getDados());
        } else {
            writeString(""); // String vazia indica null
        }
        
        destino.flush();
    }
    
    /**
     * Empacota e envia uma VotacaoReply
     */
    public void escreverReply(VotacaoReply reply) throws IOException {
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
        
        // Dados (JSON)
        if (reply.getDados() != null) {
            writeString(reply.getDados());
        } else {
            writeString("");
        }
        
        destino.flush();
    }
    
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
