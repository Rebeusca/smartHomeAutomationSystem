package smarthome.testes;

import smarthome.pojos.DispositivoIoT;
import smarthome.streams.DispositivoIoTInputStream;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * c) Teste utilizando como origem um arquivo (FileInputStream)
 * 
 * Para executar:
 * 1. Primeiro gere o arquivo: java -cp out smarthome.testes.GeradorDados
 * 2. Depois execute: java -cp out smarthome.testes.TesteArquivo
 */
public class TesteArquivo {
    
    private static final String ARQUIVO = "teste.bin";
    
    public static void main(String[] args) {
        System.out.println("=== TESTE c: Arquivo (FileInputStream) ===");
        
        try (FileInputStream fis = new FileInputStream(ARQUIVO);
             DispositivoIoTInputStream stream = new DispositivoIoTInputStream(fis)) {
            
            System.out.println("[INFO] Lendo dados do arquivo: " + ARQUIVO);
            
            DispositivoIoT[] dispositivos = stream.readObjects();
            
            System.out.println("\n[SUCESSO] Dispositivos lidos: " + dispositivos.length);
            for (DispositivoIoT disp : dispositivos) {
                System.out.println("  -> " + disp.toString());
            }
            
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao ler do arquivo: " + e.getMessage());
            System.err.println("[SUGESTÃO] Execute primeiro: java -cp out smarthome.testes.GeradorDados");
            e.printStackTrace();
            System.exit(1);
        }
    }
}

