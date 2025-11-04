package smarthome.testes;

// IMPORTAÇÕES dos POJOS e do OutputStream para enviar os dados
import smarthome.pojos.DispositivoIoT;
import smarthome.pojos.Lampada; 
import smarthome.pojos.Sensor;
import smarthome.streams.DispositivoIoTOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorDeEnvioTCP {

    private static final int PORTA = 12346; // Porta usada pelo TesteStreamEntrada (item 3.d)

    public static void main(String[] args) {
        System.out.println("Servidor de Envio TCP iniciado na porta " + PORTA + "...");

        // Dados de teste para enviar
        DispositivoIoT l1 = new Lampada("Luz Cozinha", "Cozinha", true, false, 50, 4000);
        DispositivoIoT s1 = new Sensor("Fumaça", "Cozinha", true, "Fumaça", false, 0.0);
        DispositivoIoT[] dadosParaEnviar = {l1, s1};

        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            System.out.println("Aguardando cliente de leitura (TesteStreamEntrada)...");
            
            try (Socket clientSocket = serverSocket.accept();
                 OutputStream os = clientSocket.getOutputStream();
                 // Usa o stream de SAÍDA customizado para codificar os dados
                 DispositivoIoTOutputStream streamOut = 
                     new DispositivoIoTOutputStream(dadosParaEnviar, dadosParaEnviar.length, os)) {
                
                System.out.println("\n[SUCESSO] Cliente conectado. Enviando stream...");
                
                streamOut.writeObjects(); // Codifica e envia os dados
                
                System.out.println("[SUCESSO] Dados enviados. Servidor encerrando a conexão.");

            } catch (IOException e) {
                System.err.println("Erro ao lidar com a conexão do cliente: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Erro ao iniciar o Servidor: " + e.getMessage());
        }
    }
}
