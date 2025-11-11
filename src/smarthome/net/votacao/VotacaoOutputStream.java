package smarthome.net.votacao;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class VotacaoOutputStream extends OutputStream {
    
    private final OutputStream destino;
    
    public VotacaoOutputStream(OutputStream destino) {
        this.destino = destino;
    }
    
    @Override
    public void write(int b) throws IOException {
        destino.write(b);
    }
    
    public void escreverRequest(VotacaoRequest request) throws IOException {
        destino.write(1);
        
        writeInt(request.getTipoOperacao().getCodigo());
        
        if (request.getDados() != null) {
            writeString(request.getDados());
        } else {
            writeString("");
        }
        
        destino.flush();
    }
    
    public void escreverReply(VotacaoReply reply) throws IOException {
        destino.write(2);
        
        writeInt(reply.getStatus().getCodigo());
        
        if (reply.getMensagem() != null) {
            writeString(reply.getMensagem());
        } else {
            writeString("");
        }
        
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
