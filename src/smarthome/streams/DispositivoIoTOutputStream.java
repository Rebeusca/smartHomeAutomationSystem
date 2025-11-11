package smarthome.streams;

import smarthome.pojos.DispositivoIoT;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Subclasse de OutputStream que envia o estado de um array de DispositivoIoT.
 *
 * Implementa a regra: para cada objeto, gravar pelo menos 3 atributos.
 * Protocolo de gravação (para cada dispositivo):
 * 1. Número total de objetos (4 bytes - Int)
 * 2. ID: [Tamanho String (4 bytes)] [Bytes String (N bytes)]
 * 3. NOME: [Tamanho String (4 bytes)] [Bytes String (N bytes)]
 * 4. ONLINE: [Boolean (1 byte)]
 * 5. CÔMODO: [Tamanho String (4 bytes)] [Bytes String (N bytes)]
 */
public class DispositivoIoTOutputStream extends OutputStream {

    private final DispositivoIoT[] dispositivos;
    private final int numObjetos;
    private final OutputStream destino;

    // a) Construtor conforme as regras do trabalho
    public DispositivoIoTOutputStream(
            DispositivoIoT[] dispositivos, // (i) array de objetos
            int numObjetos,             // (ii) número de Objetos
            OutputStream destino        // (iv) OutputStream de destino
    ) {
        this.dispositivos = dispositivos;
        this.numObjetos = numObjetos;
        this.destino = destino;
    }

    // Método obrigatório de OutputStream (encaminha para o destino)
    @Override
    public void write(int b) throws IOException {
        destino.write(b);
    }
    
    // Método principal para iniciar a gravação dos objetos
    public void writeObjects() throws IOException {
        
        // 1. Envia o número total de objetos a serem transmitidos (Regra ii)
        writeInt(numObjetos); 

        // 2. Itera sobre o array de dispositivos
        for (int i = 0; i < numObjetos; i++) {
            if (i >= dispositivos.length) break;
            DispositivoIoT disp = dispositivos[i];
            
            // Grava 4 atributos (Regra iii exige 3+)
            gravarDispositivo(disp);
        }
        
        destino.flush(); 
    }

    private void gravarDispositivo(DispositivoIoT disp) throws IOException {
        
        // Atributo 1: TIPO (String) - Nome da classe para identificar o tipo
        String tipo = disp.getClass().getSimpleName();
        writeString(tipo);
        
        // Atributo 2: ID (String)
        writeString(disp.getId()); 
        
        // Atributo 3: NOME (String)
        writeString(disp.getNome()); 
        
        // Atributo 4: ONLINE (boolean) - 1 byte
        destino.write(disp.getOnline() ? 1 : 0); 
        
        // Atributo 5: CÔMODO (String)
        writeString(disp.getComodo());
    }
    
    // Auxiliar: Escreve uma String (4 bytes do tamanho + N bytes dos dados)
    private void writeString(String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        writeInt(bytes.length); 
        destino.write(bytes); 
    }

    // Auxiliar: Escreve um inteiro (4 bytes)
    private void writeInt(int v) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(v);
        destino.write(buffer.array()); 
    }
}
