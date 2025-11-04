package smarthome.testes;

import smarthome.pojos.DispositivoIoT;
import smarthome.streams.DispositivoIoTInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * d) Teste utilizando como origem um servidor remoto (TCP)
 * 
 * Para executar:
 * 1. Em um terminal, inicie o servidor: java -cp out smarthome.testes.ServidorTCP
 * 2. Em outro terminal, execute: java -cp out smarthome.testes.TesteTCP
 */
public class TesteTCP {
    
    private static final String HOST = "localhost";
    private static final int PORTA = 12345;
    
    public static void main(String[] args) {
        System.out.println("=== TESTE d: Servidor Remoto (TCP) ===");
        System.out.println("[INFO] Conectando ao servidor " + HOST + ":" + PORTA + "...");
        
        try (Socket socket = new Socket(HOST, PORTA);
             InputStream is = socket.getInputStream();
             DispositivoIoTInputStream stream = new DispositivoIoTInputStream(is)) {
            
            System.out.println("[OK] Conectado ao servidor!");
            System.out.println("[INFO] Lendo dados da rede...");
            
            DispositivoIoT[] dispositivos = stream.readObjects();
            
            System.out.println("\n[SUCESSO] Dispositivos lidos: " + dispositivos.length);
            for (DispositivoIoT disp : dispositivos) {
                System.out.println("  -> " + disp.toString());
            }
            
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao ler da rede TCP: " + e.getMessage());
            System.err.println("[SUGESTÃO] Certifique-se de que o ServidorTCP está rodando na porta " + PORTA);
            e.printStackTrace();
            System.exit(1);
        }
    }
}

