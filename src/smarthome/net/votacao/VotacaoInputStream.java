package smarthome.net.votacao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Stream para desempacotar mensagens de votação recebidas via TCP.
 */
public class VotacaoInputStream extends InputStream {
    
    private final InputStream origem;
    
    public VotacaoInputStream(InputStream origem) {
        this.origem = origem;
    }
    
    @Override
    public int read() throws IOException {
        return origem.read();
    }
    
    /**
     * Desempacota e lê uma VotacaoRequest
     */
    public VotacaoRequest lerRequest() throws IOException {
        // Lê o tipo (deve ser 1 = Request)
        int tipo = origem.read();
        if (tipo != 1) {
            throw new IOException("Tipo de mensagem inválido. Esperado Request (1), recebido: " + tipo);
        }
        
        VotacaoRequest request = new VotacaoRequest();
        
        // Lê código da operação
        int codigoOperacao = readInt();
        request.setTipoOperacao(VotacaoRequest.TipoOperacao.fromCodigo(codigoOperacao));
        
        // Lê dados (JSON)
        String dados = readString();
        if (!dados.isEmpty()) {
            request.setDados(dados);
        }
        
        return request;
    }
    
    /**
     * Desempacota e lê uma VotacaoReply
     */
    public VotacaoReply lerReply() throws IOException {
        // Lê o tipo (deve ser 2 = Reply)
        int tipo = origem.read();
        if (tipo != 2) {
            throw new IOException("Tipo de mensagem inválido. Esperado Reply (2), recebido: " + tipo);
        }
        
        VotacaoReply reply = new VotacaoReply();
        
        // Lê código do status
        int codigoStatus = readInt();
        reply.setStatus(VotacaoReply.Status.fromCodigo(codigoStatus));
        
        // Lê mensagem
        String mensagem = readString();
        if (!mensagem.isEmpty()) {
            reply.setMensagem(mensagem);
        }
        
        // Lê dados (JSON)
        String dados = readString();
        if (!dados.isEmpty()) {
            reply.setDados(dados);
        }
        
        return reply;
    }
    
    private String readString() throws IOException {
        int length = readInt();
        if (length == 0) {
            return "";
        }
        if (length < 0) {
            throw new IOException("Tamanho de String inválido: " + length);
        }
        
        byte[] bytes = new byte[length];
        int offset = 0;
        while (offset < length) {
            int bytesRead = origem.read(bytes, offset, length - offset);
            if (bytesRead == -1) {
                throw new IOException("Fim inesperado do stream ao ler String.");
            }
            offset += bytesRead;
        }
        
        return new String(bytes, StandardCharsets.UTF_8);
    }
    
    private int readInt() throws IOException {
        byte[] bytes = new byte[Integer.BYTES];
        int offset = 0;
        while (offset < Integer.BYTES) {
            int bytesRead = origem.read(bytes, offset, Integer.BYTES - offset);
            if (bytesRead == -1) {
                throw new IOException("Fim inesperado do stream ao ler Int.");
            }
            offset += bytesRead;
        }
        return ByteBuffer.wrap(bytes).getInt();
    }
}
