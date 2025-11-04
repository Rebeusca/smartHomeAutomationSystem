package smarthome.testes;

import smarthome.pojos.DispositivoIoT;
import smarthome.streams.DispositivoIoTInputStream;

import java.io.IOException;

/**
 * b) Teste utilizando como origem a entrada padrão (System.in)
 * 
 * Para executar no CMD:
 *   java -cp out smarthome.testes.TesteSystemIn < teste.bin
 * 
 * Para executar no PowerShell:
 *   Get-Content teste.bin | java -cp out smarthome.testes.TesteSystemIn
 * 
 * Ou use o arquivo teste_systemin.bat
 */
public class TesteSystemIn {
    
    public static void main(String[] args) {
        System.err.println("=== TESTE b: Entrada Padrão (System.in) ===");
        System.err.println("[INFO] Lendo dados de System.in...");
        
        try (DispositivoIoTInputStream stream = new DispositivoIoTInputStream(System.in)) {
            
            DispositivoIoT[] dispositivos = stream.readObjects();
            
            System.err.println("\n[SUCESSO] Dispositivos lidos: " + dispositivos.length);
            for (DispositivoIoT disp : dispositivos) {
                System.err.println("  -> " + disp.toString());
            }
            
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao ler de System.in: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

