package smarthome.net.votacao;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ClienteMulticastNotas implements Runnable {
    
    private static final String MULTICAST_GROUP = "230.0.0.1";
    private static final int MULTICAST_PORT = 54323;
    
    private MulticastSocket socket;
    private InetAddress group;
    private boolean executando = true;
    
    public ClienteMulticastNotas() throws IOException {
        this.socket = new MulticastSocket(MULTICAST_PORT);
        this.group = InetAddress.getByName(MULTICAST_GROUP);
        this.socket.joinGroup(group);
    }
    
    @Override
    public void run() {
        System.out.println("=== Cliente Multicast de Notas ===");
        System.out.println("Aguardando notas informativas no grupo " + MULTICAST_GROUP + ":" + MULTICAST_PORT);
        System.out.println("(Pressione Ctrl+C para sair)\n");
        
        byte[] buffer = new byte[4096];
        
        while (executando) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                String json = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                NotaInformativa nota = parseNotaFromJson(json);
                
                exibirNota(nota);
                
            } catch (IOException e) {
                if (executando) {
                    System.err.println("[ERRO] Falha ao receber nota: " + e.getMessage());
                }
            }
        }
    }
    
    private NotaInformativa parseNotaFromJson(String json) {
        NotaInformativa nota = new NotaInformativa();
        
        try {
            json = json.replace("{", "").replace("}", "").trim();
            String[] campos = json.split(",");
            
            for (String campo : campos) {
                String[] kv = campo.split(":");
                if (kv.length == 2) {
                    String key = kv[0].trim().replace("\"", "");
                    String value = kv[1].trim().replace("\"", "");
                    
                    switch (key) {
                        case "titulo":
                            nota.setTitulo(value);
                            break;
                        case "mensagem":
                            nota.setMensagem(value);
                            break;
                        case "admin":
                            nota.setAdmin(value);
                            break;
                        case "timestamp":
                            try {
                                nota.setTimestamp(Long.parseLong(value));
                            } catch (NumberFormatException e) {
                            }
                            break;
                    }
                }
            }
        } catch (Exception e) {
            nota.setTitulo("Nota Informativa");
            nota.setMensagem(json);
        }
        
        return nota;
    }
    
    private void exibirNota(NotaInformativa nota) {
        String separador = "==================================================";
        System.out.println("\n" + separador);
        System.out.println("[NOTA INFORMATIVA] NOVA NOTA");
        System.out.println(separador);
        System.out.println(nota.toString());
        System.out.println(separador + "\n");
    }
    
    public void parar() {
        executando = false;
        if (socket != null && !socket.isClosed()) {
            try {
                socket.leaveGroup(group);
                socket.close();
            } catch (IOException e) {
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            ClienteMulticastNotas cliente = new ClienteMulticastNotas();
            
            Runtime.getRuntime().addShutdownHook(new Thread(cliente::parar));
            
            cliente.run();
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao iniciar cliente multicast: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
