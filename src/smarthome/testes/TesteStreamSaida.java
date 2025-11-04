package smarthome.testes;

import smarthome.pojos.DispositivoIoT;
import smarthome.pojos.Lampada; 
import smarthome.pojos.Termostato;
import smarthome.pojos.Sensor; // NOVO: Importa a classe Sensor
import smarthome.streams.DispositivoIoTOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class TesteStreamSaida {
    
    public static void main(String[] args) {
        
        // 1. Inicialização dos dados de teste
        
        // 1.1. Instância de Lampada
        Lampada l1 = new Lampada(
            "Luz Principal", 
            "Sala", 
            true,   // online
            true,   // ligada
            85,     // intensidade
            3000    // temperatura (K)
        );
        
        // 1.2. Instância de Termostato
        Termostato t1 = new Termostato(
            "Climatizador Central", 
            "Quarto", 
            true,    // status (ativo)
            24.5,    // temperaturaAtual
            22.0     // temperaturaDesejada
        );
        
        // 1.3. Instância de Sensor (Substitui a Lâmpada l2)
        Sensor s1 = new Sensor(
            "Sensor de Movimento", 
            "Corredor", 
            true,   // online
            "Movimento", // tipo
            false,  // unidadeMedida (não se aplica a movimento)
            0.0     // valor (0.0 significa sem movimento)
        );
        s1.setOnline(true); // Garante que o atributo online da superclasse está setado.

        // O array de DispositivoIoT será transmitido
        DispositivoIoT[] dispositivos = {l1, t1, s1}; // Array atualizado: Lampada, Termostato, Sensor
        int numParaEnviar = dispositivos.length;
        
        // ---------------------------------------------------------------------
        // b) i. Teste: Saída Padrão (System.out)
        // ---------------------------------------------------------------------
        System.out.println("--- 🧪 Teste i: Saída Padrão (System.out) ---");
        try (DispositivoIoTOutputStream streamOut = 
                new DispositivoIoTOutputStream(dispositivos, numParaEnviar, System.out)) {
            System.out.println("[INFO] Enviando bytes para o console...");
            streamOut.writeObjects();
            System.out.println("\n[SUCESSO] Bytes do stream customizado escritos.");
        } catch (IOException e) {
            System.err.println("[ERRO] Falha no teste System.out: " + e.getMessage());
        }
        
        // ---------------------------------------------------------------------
        // b) ii. Teste: Arquivo (FileOutputStream)
        // ---------------------------------------------------------------------
        String nomeArquivo = "estado_smarthome_output.bin";
        System.out.println("\n--- 🧪 Teste ii: Arquivo (FileOutputStream) ---");
        try (FileOutputStream fos = new FileOutputStream(nomeArquivo);
             DispositivoIoTOutputStream streamFile = 
                 new DispositivoIoTOutputStream(dispositivos, numParaEnviar, fos)) {
            streamFile.writeObjects();
            System.out.println("[SUCESSO] Dados salvos em: " + nomeArquivo);
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao salvar no arquivo: " + e.getMessage());
        }
        
        // ---------------------------------------------------------------------
        // b) iii. Teste: Servidor Remoto (TCP)
        // ---------------------------------------------------------------------
        int porta = 12345;
        String host = "localhost";
        System.out.println("\n--- 🧪 Teste iii: Servidor Remoto (TCP) ---");
        try (Socket socket = new Socket(host, porta);
             OutputStream os = socket.getOutputStream();
             DispositivoIoTOutputStream streamRede = 
                 new DispositivoIoTOutputStream(dispositivos, numParaEnviar, os)) {
            
            System.out.println("[INFO] Conectado e enviando dados para o Servidor TCP...");
            streamRede.writeObjects();
            System.out.println("[SUCESSO] Dados dos dispositivos enviados via TCP.");
            
        } catch (IOException e) {
            System.err.println("[ERRO] Falha no teste TCP. Motivo: " + e.getMessage());
            System.err.println("[SUGESTÃO] Certifique-se de que o Servidor TCP está rodando na porta " + porta);
        }
    }
}