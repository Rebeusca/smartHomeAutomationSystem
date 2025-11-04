package smarthome.testes;

import smarthome.pojos.DispositivoIoT;
import smarthome.streams.DispositivoIoTInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class TesteStreamEntrada {

    public static void main(String[] args) throws IOException {

        // O arquivo 'estado_smarthome_output.bin' deve ter sido gerado
        // pelo TesteStreamSaida (Questão 2, item b.ii).

        // ---------------------------------------------------------------------
        // b) Teste: Entrada Padrão (System.in)
        // ---------------------------------------------------------------------
        System.out.println("--- 🧪 Teste b: Entrada Padrão (System.in) ---");
        System.out.println("[INFO] Este teste é conceitual. Para validar, você precisaria:");
        System.out.println("       1. Executar o TesteStreamSaida (b.i) e salvar a saída binária.");
        System.out.println("       2. Redirecionar essa saída para a entrada padrão deste programa (java ... < arquivo.bin).");
        // Se fosse rodado, o código seria:
        // readFromStream(System.in, "ENTRADA PADRÃO");


        // ---------------------------------------------------------------------
        // c) Teste: Arquivo (FileInputStream)
        // ---------------------------------------------------------------------
        String nomeArquivo = "estado_smarthome_output.bin";
        System.out.println("\n--- 🧪 Teste c: Arquivo (FileInputStream) ---");
        try (FileInputStream fis = new FileInputStream(nomeArquivo)) {
            readFromStream(fis, "ARQUIVO");
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao ler arquivo: " + e.getMessage());
            System.err.println("[SUGESTÃO] Execute o TesteStreamSaida (b.ii) primeiro!");
        }
        
        // ---------------------------------------------------------------------
        // d) Teste: Servidor Remoto (TCP) - Cliente de Leitura
        // ---------------------------------------------------------------------
        // Atenção: Este teste exige que o SERVIDOR seja modificado para ENVIAR o stream!
        int porta = 12346; // Usando uma porta diferente para o Server de ENTRADA
        String host = "localhost";
        System.out.println("\n--- 🧪 Teste d: Servidor Remoto (TCP) ---");
        try (Socket socket = new Socket(host, porta);
             InputStream is = socket.getInputStream()) {
            
            System.out.println("[INFO] Conectado ao servidor de envio de dados.");
            readFromStream(is, "REDE TCP");
            
        } catch (IOException e) {
            System.err.println("[ERRO] Falha no teste TCP: " + e.getMessage());
            System.err.println("[SUGESTÃO] Execute o ServidorDeEnvioTCP na porta " + porta + " primeiro.");
        }
    }

    private static void readFromStream(InputStream stream, String origemNome) throws IOException {
        try (DispositivoIoTInputStream in = new DispositivoIoTInputStream(stream)) {
            
            DispositivoIoT[] dispositivosLidos = in.readObjects();
            
            System.out.println("[SUCESSO] Dispositivos lidos da " + origemNome + ": " + dispositivosLidos.length);
            for (DispositivoIoT disp : dispositivosLidos) {
                System.out.println("   -> LIDO: " + disp.toString());
            }

        } catch (Exception e) {
            System.err.println("[ERRO FATAL] Falha na decodificação do stream: " + e.getMessage());
        }
    }
}
