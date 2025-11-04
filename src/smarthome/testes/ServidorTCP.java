package smarthome.testes;

import smarthome.pojos.DispositivoIoT;
import smarthome.pojos.Lampada;
import smarthome.pojos.Sensor;
import smarthome.streams.DispositivoIoTOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Servidor TCP que envia dados de teste via rede.
 * Este servidor será usado pelo teste de entrada TCP (d).
 * 
 * Para executar: java -cp out smarthome.testes.ServidorTCP
 */
public class ServidorTCP {
    
    private static final int PORTA = 12345;
    
    public static void main(String[] args) {
        System.out.println("=== Servidor TCP - Envio de Dados ===");
        System.out.println("[INFO] Servidor iniciado na porta " + PORTA);
        System.out.println("[INFO] Aguardando conexão de cliente...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            
            Socket clientSocket = serverSocket.accept();
            System.out.println("[OK] Cliente conectado: " + clientSocket.getRemoteSocketAddress());
            
            // Criar dados de teste
            Lampada l1 = new Lampada("Luz Cozinha", "Cozinha", true, false, 50, 4000);
            Sensor s1 = new Sensor("Sensor Fumaça", "Cozinha", true, "Fumaça", false, 0.0);
            DispositivoIoT[] dispositivos = {l1, s1};
            
            try (OutputStream os = clientSocket.getOutputStream();
                 DispositivoIoTOutputStream stream = 
                     new DispositivoIoTOutputStream(dispositivos, dispositivos.length, os)) {
                
                System.out.println("[INFO] Enviando " + dispositivos.length + " dispositivos...");
                stream.writeObjects();
                System.out.println("[SUCESSO] Dados enviados com sucesso!");
                
            } catch (IOException e) {
                System.err.println("[ERRO] Falha ao enviar dados: " + e.getMessage());
                e.printStackTrace();
            }
            
            clientSocket.close();
            System.out.println("[INFO] Conexão encerrada.");
            
        } catch (IOException e) {
            System.err.println("[ERRO] Falha no servidor: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

