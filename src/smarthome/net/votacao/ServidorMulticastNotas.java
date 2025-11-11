package smarthome.net.votacao;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

public class ServidorMulticastNotas {
    
    private static final String MULTICAST_GROUP = "230.0.0.1";
    private static final int MULTICAST_PORT = 54323;
    
    private MulticastSocket socket;
    private InetAddress group;
    
    public ServidorMulticastNotas() throws IOException {
        this.socket = new MulticastSocket();
        this.group = InetAddress.getByName(MULTICAST_GROUP);
    }
    
    public void enviarNota(NotaInformativa nota) throws IOException {
        String json = String.format("{\"titulo\":\"%s\",\"mensagem\":\"%s\",\"admin\":\"%s\",\"timestamp\":%d}",
            escapeJson(nota.getTitulo()),
            escapeJson(nota.getMensagem()),
            escapeJson(nota.getAdmin()),
            nota.getTimestamp());
        
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, group, MULTICAST_PORT);
        socket.send(packet);
        
        System.out.println("[MULTICAST] Nota enviada: " + nota.getTitulo());
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }
    
    public void fechar() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
    
    public static void enviarNotaInformativa(String titulo, String mensagem, String admin) {
        try {
            ServidorMulticastNotas servidor = new ServidorMulticastNotas();
            NotaInformativa nota = new NotaInformativa(titulo, mensagem, admin);
            servidor.enviarNota(nota);
            servidor.fechar();
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao enviar nota via multicast: " + e.getMessage());
        }
    }
}
