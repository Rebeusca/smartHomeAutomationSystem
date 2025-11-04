package smarthome.testes;

import smarthome.pojos.DispositivoIoT;
import smarthome.pojos.Lampada;
import smarthome.pojos.Sensor;
import smarthome.pojos.Termostato;
import smarthome.streams.DispositivoIoTOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Gera arquivo de dados de teste para ser usado pelos outros testes.
 * Execute este primeiro para criar o arquivo teste.bin
 */
public class GeradorDados {
    
    private static final String ARQUIVO = "teste.bin";
    
    public static void main(String[] args) {
        System.out.println("=== Gerando arquivo de dados de teste ===");
        
        // Criar dispositivos de teste
        Lampada l1 = new Lampada("Luz Principal", "Sala", true, true, 85, 3000);
        Termostato t1 = new Termostato("Ar Condicionado", "Quarto", true, 24.5, 22.0);
        Sensor s1 = new Sensor("Sensor Movimento", "Corredor", true, "Movimento", false, 0.0);
        
        DispositivoIoT[] dispositivos = {l1, t1, s1};
        
        try (FileOutputStream fos = new FileOutputStream(ARQUIVO);
             DispositivoIoTOutputStream stream = 
                 new DispositivoIoTOutputStream(dispositivos, dispositivos.length, fos)) {
            
            stream.writeObjects();
            System.out.println("[OK] Arquivo gerado: " + ARQUIVO);
            System.out.println("[OK] Total de dispositivos: " + dispositivos.length);
            
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao gerar arquivo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

