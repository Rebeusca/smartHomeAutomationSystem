package smarthome.net;

import smarthome.pojos.DispositivoIoT;
import smarthome.streams.DispositivoIoTInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Stream para desempacotar (deserializar) mensagens Request e Reply recebidas via rede.
 */
public class MensagemInputStream extends InputStream {
    
    private final InputStream origem;
    
    public MensagemInputStream(InputStream origem) {
        this.origem = origem;
    }
    
    @Override
    public int read() throws IOException {
        return origem.read();
    }
    
    /**
     * Desempacota e lê uma MensagemRequest
     */
    public MensagemRequest lerRequest() throws IOException {
        // Lê o tipo (deve ser 1 = Request)
        int tipo = origem.read();
        if (tipo != 1) {
            throw new IOException("Tipo de mensagem inválido. Esperado Request (1), recebido: " + tipo);
        }
        
        MensagemRequest request = new MensagemRequest();
        
        // Lê código da operação
        int codigoOperacao = readInt();
        request.setTipoOperacao(MensagemRequest.TipoOperacao.fromCodigo(codigoOperacao));
        
        // Lê DispositivoId
        String dispositivoId = readString();
        if (!dispositivoId.isEmpty()) {
            request.setDispositivoId(dispositivoId);
        }
        
        // Lê dados adicionais
        String dados = readString();
        if (!dados.isEmpty()) {
            request.setDados(dados);
        }
        
        return request;
    }
    
    /**
     * Desempacota e lê uma MensagemReply
     */
    public MensagemReply lerReply() throws IOException {
        int tipo = origem.read();
        if (tipo != 2) {
            throw new IOException("Tipo de mensagem inválido. Esperado Reply (2), recebido: " + tipo);
        }
        
        MensagemReply reply = new MensagemReply();
        
        // Lê código do status
        int codigoStatus = readInt();
        reply.setStatus(MensagemReply.Status.fromCodigo(codigoStatus));
        
        // Lê mensagem
        String mensagem = readString();
        if (!mensagem.isEmpty()) {
            reply.setMensagem(mensagem);
        }
        
        // Lê array de dispositivos
        int numDispositivos = readInt();
        if (numDispositivos > 0) {
            DispositivoIoTInputStream streamDispositivos = new DispositivoIoTInputStream(origem);
            DispositivoIoT[] dispositivos = streamDispositivos.readObjects();
            reply.setDispositivos(dispositivos);
        }
        
        // Lê dispositivo único
        int existeDispositivo = origem.read();
        if (existeDispositivo == 1) {
            DispositivoIoTInputStream streamDispositivo = new DispositivoIoTInputStream(origem);
            DispositivoIoT[] dispositivos = streamDispositivo.readObjects();
            if (dispositivos.length > 0) {
                reply.setDispositivo(dispositivos[0]);
            }
        }
        
        return reply;
    }
    
    // Métodos auxiliares
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

