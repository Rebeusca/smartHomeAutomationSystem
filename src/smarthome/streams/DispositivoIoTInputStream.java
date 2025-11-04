package smarthome.streams;

import smarthome.pojos.DispositivoIoT;
import smarthome.pojos.Lampada; 
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Subclasse de InputStream que lê os bytes gerados pelo DispositivoIoTOutputStream
 * e reconstrói o array de DispositivoIoT.
 */
public class DispositivoIoTInputStream extends InputStream {

    private final InputStream origem;

    // a) Construtor conforme a regra
    public DispositivoIoTInputStream(InputStream origem) {
        this.origem = origem;
    }

    // Método obrigatório de InputStream (encaminha a leitura para a origem)
    @Override
    public int read() throws IOException {
        return origem.read();
    }
    
    // Método principal para ler todos os objetos
    public DispositivoIoT[] readObjects() throws IOException {
        
        // 1. Lê o número total de objetos (primeiro dado enviado pelo OutputStream)
        int numObjetos = readInt();
        
        if (numObjetos <= 0) {
            return new DispositivoIoT[0];
        }

        List<DispositivoIoT> listaDispositivos = new ArrayList<>(numObjetos);

        // 2. Itera para ler cada objeto
        for (int i = 0; i < numObjetos; i++) {
            listaDispositivos.add(readDispositivo());
        }

        return listaDispositivos.toArray(new DispositivoIoT[0]);
    }

    // Método auxiliar para ler e reconstruir um DispositivoIoT
    private DispositivoIoT readDispositivo() throws IOException {
        // Lógica INVERSA à gravação:
        
        // Atributo 1: ID (String)
        String id = readString(); 
        
        // Atributo 2: NOME (String)
        String nome = readString(); 
        
        // Atributo 3: ONLINE (boolean) - 1 byte
        boolean online = read() == 1; // Lê 1 byte (1 é true, 0 é false)
        
        // Atributo 4: CÔMODO (String)
        String comodo = readString();
        
        // --- Reconstrução Simples ---
        // Nota: Como não sabemos qual subclasse (Lampada, Termostato, Sensor) foi enviada,
        // criamos uma instância genérica (mock) ou uma instância de uma subclasse base,
        // pois a superclasse DispositivoIoT é abstrata.
        
        // Para fins de teste, criaremos uma instância de Lampada e setaremos os campos:
        Lampada dispositivo = new Lampada(nome, comodo, online, false, 0, 0); // Mock
        dispositivo.setId(id);
        
        return dispositivo;
    }
    
    // Auxiliar: Lê uma String (Lê 4 bytes do tamanho + N bytes dos dados)
    private String readString() throws IOException {
        int length = readInt(); // 1. Lê o tamanho (4 bytes)
        if (length < 0) throw new IOException("Tamanho de String inválido: " + length);
        
        byte[] bytes = new byte[length];
        
        // 2. Lê os bytes da string
        int offset = 0;
        while (offset < length) {
            int bytesRead = origem.read(bytes, offset, length - offset);
            if (bytesRead == -1) {
                 // Fim do stream inesperado
                throw new IOException("Fim inesperado do stream ao ler String."); 
            }
            offset += bytesRead;
        }
        
        return new String(bytes, StandardCharsets.UTF_8);
    }

    // Auxiliar: Lê um inteiro (4 bytes)
    private int readInt() throws IOException {
        byte[] bytes = new byte[Integer.BYTES];
        
        // Garante que 4 bytes sejam lidos
        int offset = 0;
        while (offset < Integer.BYTES) {
            int bytesRead = origem.read(bytes, offset, Integer.BYTES - offset);
            if (bytesRead == -1) {
                // Fim do stream inesperado
                throw new IOException("Fim inesperado do stream ao ler Int."); 
            }
            offset += bytesRead;
        }
        
        return ByteBuffer.wrap(bytes).getInt(); 
    }
}
