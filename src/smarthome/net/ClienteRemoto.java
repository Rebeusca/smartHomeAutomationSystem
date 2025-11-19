package smarthome.net;

import smarthome.interfaces.ISmartHomeService;
import smarthome.pojos.DispositivoIoT;
import smarthome.pojos.Rotina;
import smarthome.pojos.Alerta;
import smarthome.pojos.Comodo;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

/**
 * Cliente remoto que se comunica com o servidor usando Invocação Remota.
 * Utiliza doOperation para enviar requisições e receber respostas.
 */
public class ClienteRemoto {
    
    private final ClientRequestHandler requestHandler;
    private final RemoteObjectRef remoteRef;
    
    public ClienteRemoto(String host, int porta) throws IOException {
        this.requestHandler = new ClientRequestHandler();
        this.remoteRef = new RemoteObjectRef(
            InetAddress.getByName(host),
            porta,
            1, // objectId único para o serviço SmartHome
            ISmartHomeService.class.getName()
        );
    }
    
    /**
     * Lista todos os dispositivos usando invocação remota.
     */
    @SuppressWarnings("unchecked")
    public List<DispositivoIoT> listarDispositivos() throws IOException, ClassNotFoundException {
        // Serializa argumentos (passagem por valor - neste caso não há argumentos)
        byte[] arguments = MessageMarshaller.serialize(null);
        
        // Invoca método remoto
        byte[] reply = requestHandler.doOperation(remoteRef, ISmartHomeService.METHOD_LISTAR_DISPOSITIVOS, arguments);
        
        // Deserializa resposta
        return (List<DispositivoIoT>) MessageMarshaller.deserialize(reply);
    }
    
    /**
     * Obtém um dispositivo por ID usando invocação remota.
     */
    public DispositivoIoT obterDispositivo(String dispositivoId) throws IOException, ClassNotFoundException {
        // Serializa argumentos (passagem por valor)
        byte[] arguments = MessageMarshaller.serialize(dispositivoId);
        
        // Invoca método remoto
        byte[] reply = requestHandler.doOperation(remoteRef, ISmartHomeService.METHOD_OBTER_DISPOSITIVO, arguments);
        
        // Deserializa resposta
        return (DispositivoIoT) MessageMarshaller.deserialize(reply);
    }
    
    /**
     * Atualiza um dispositivo usando invocação remota.
     * O dispositivo é passado por valor (serializado).
     */
    public DispositivoIoT atualizarDispositivo(String dispositivoId, DispositivoIoT dispositivo) throws IOException, ClassNotFoundException {
        // Cria objeto com ID e dispositivo para serialização
        Object[] args = {dispositivoId, dispositivo};
        byte[] arguments = MessageMarshaller.serialize(args);
        
        // Invoca método remoto
        byte[] reply = requestHandler.doOperation(remoteRef, ISmartHomeService.METHOD_ATUALIZAR_DISPOSITIVO, arguments);
        
        // Deserializa resposta
        return (DispositivoIoT) MessageMarshaller.deserialize(reply);
    }
    
    /**
     * Executa uma ação em um dispositivo usando invocação remota.
     * O comando é passado por valor (serializado).
     */
    public DispositivoIoT executarAcao(String dispositivoId, String comando) throws IOException, ClassNotFoundException {
        // Serializa argumentos (passagem por valor)
        Object[] args = {dispositivoId, comando};
        byte[] arguments = MessageMarshaller.serialize(args);
        
        // Invoca método remoto
        byte[] reply = requestHandler.doOperation(remoteRef, ISmartHomeService.METHOD_EXECUTAR_ACAO, arguments);
        
        // Deserializa resposta
        return (DispositivoIoT) MessageMarshaller.deserialize(reply);
    }
    
    /**
     * Lista todas as rotinas usando invocação remota.
     */
    @SuppressWarnings("unchecked")
    public List<Rotina> listarRotinas() throws IOException, ClassNotFoundException {
        byte[] arguments = MessageMarshaller.serialize(null);
        byte[] reply = requestHandler.doOperation(remoteRef, ISmartHomeService.METHOD_LISTAR_ROTINAS, arguments);
        return (List<Rotina>) MessageMarshaller.deserialize(reply);
    }
    
    /**
     * Cria uma nova rotina usando invocação remota.
     * A rotina é passada por valor (serializada).
     */
    public Rotina criarRotina(Rotina rotina) throws IOException, ClassNotFoundException {
        byte[] arguments = MessageMarshaller.serialize(rotina);
        byte[] reply = requestHandler.doOperation(remoteRef, ISmartHomeService.METHOD_CRIAR_ROTINA, arguments);
        return (Rotina) MessageMarshaller.deserialize(reply);
    }
    
    /**
     * Lista todos os alertas usando invocação remota.
     */
    @SuppressWarnings("unchecked")
    public List<Alerta> listarAlertas() throws IOException, ClassNotFoundException {
        byte[] arguments = MessageMarshaller.serialize(null);
        byte[] reply = requestHandler.doOperation(remoteRef, ISmartHomeService.METHOD_LISTAR_ALERTAS, arguments);
        return (List<Alerta>) MessageMarshaller.deserialize(reply);
    }
    
    /**
     * Obtém um cômodo por nome usando invocação remota.
     */
    public Comodo obterComodo(String nomeComodo) throws IOException, ClassNotFoundException {
        byte[] arguments = MessageMarshaller.serialize(nomeComodo);
        byte[] reply = requestHandler.doOperation(remoteRef, ISmartHomeService.METHOD_OBTER_COMODO, arguments);
        return (Comodo) MessageMarshaller.deserialize(reply);
    }
    
    public static void main(String[] args) {
        try {
            ClienteRemoto cliente = new ClienteRemoto("localhost", 54321);
            
            System.out.println("=== Cliente Remoto Smart Home (Invocação Remota) ===\n");
            
            // Teste 1: Listar dispositivos
            System.out.println("1. Listando dispositivos...");
            List<DispositivoIoT> dispositivos = cliente.listarDispositivos();
            System.out.println("   Total: " + dispositivos.size() + " dispositivos");
            for (DispositivoIoT d : dispositivos) {
                System.out.println("   - " + d.getId() + ": " + d.getNome());
            }
            
            if (!dispositivos.isEmpty()) {
                String primeiroId = dispositivos.get(0).getId();
                
                // Teste 2: Obter dispositivo específico
                System.out.println("\n2. Obtendo dispositivo: " + primeiroId);
                DispositivoIoT dispositivo = cliente.obterDispositivo(primeiroId);
                System.out.println("   Dispositivo: " + dispositivo);
                
                // Teste 3: Atualizar dispositivo (passagem por valor)
                System.out.println("\n3. Atualizando dispositivo: " + primeiroId);
                dispositivo.setOnline(true);
                dispositivo = cliente.atualizarDispositivo(primeiroId, dispositivo);
                System.out.println("   Dispositivo atualizado: " + dispositivo);
                
                // Teste 4: Executar ação (passagem por valor)
                System.out.println("\n4. Executando ação no dispositivo: " + primeiroId);
                dispositivo = cliente.executarAcao(primeiroId, "ligar");
                System.out.println("   Ação executada no dispositivo: " + dispositivo.getId());
            }
            
            // Teste 5: Listar rotinas
            System.out.println("\n5. Listando rotinas...");
            List<Rotina> rotinas = cliente.listarRotinas();
            System.out.println("   Total: " + rotinas.size() + " rotinas");
            
            // Teste 6: Listar alertas
            System.out.println("\n6. Listando alertas...");
            List<Alerta> alertas = cliente.listarAlertas();
            System.out.println("   Total: " + alertas.size() + " alertas");
            
        } catch (Exception e) {
            System.err.println("[ERRO] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
