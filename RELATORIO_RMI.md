# Relatório: Implementação de RMI para Comunicação Cliente-Servidor

## 1. Introdução

Este relatório descreve a implementação de um sistema de Invocação Remota de Métodos (RMI) customizado para comunicação entre cliente e servidor no sistema Smart Home Automation. A implementação utiliza UDP (DatagramSocket) como protocolo de transporte e implementa os mecanismos fundamentais de RMI, incluindo marshalling/unmarshalling, passagem por valor e referências remotas.

## 2. Arquitetura Geral

A arquitetura do sistema RMI é composta pelas seguintes camadas:

### 2.1 Camada de Aplicação
- **ClienteRemoto**: Interface de alto nível para o cliente
- **ServidorRemoto**: Servidor que processa requisições remotas
- **ISmartHomeService**: Interface que define os métodos remotos disponíveis
- **SmartHomeServiceImpl**: Implementação dos serviços remotos

### 2.2 Camada de Comunicação
- **ClientRequestHandler**: Gerencia requisições do lado do cliente
- **ServerRequestHandler**: Gerencia requisições do lado do servidor
- **MessageMarshaller**: Responsável por serialização/deserialização de mensagens

### 2.3 Camada de Referência Remota
- **RemoteObjectRef**: Representa uma referência a um objeto remoto

## 3. Componentes Principais

### 3.1 RemoteObjectRef

A classe `RemoteObjectRef` representa uma referência a um objeto remoto, implementando o conceito de passagem por referência em RMI.

```12:23:src/smarthome/net/RemoteObjectRef.java
public class RemoteObjectRef implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private InetAddress host;
    private int port;
    private int objectId;  // Identificador único do objeto remoto
    private String interfaceName;  // Nome da interface do objeto remoto
    
    public RemoteObjectRef(InetAddress host, int port, int objectId, String interfaceName) {
        this.host = host;
        this.port = port;
        this.objectId = objectId;
        this.interfaceName = interfaceName;
    }
```

**Características:**
- Armazena informações de localização do objeto remoto (host, porta)
- Identifica o objeto através de um `objectId` único
- Especifica a interface do objeto através de `interfaceName`
- Implementa `Serializable` para permitir serialização
- Implementa `equals()` e `hashCode()` para comparação adequada

### 3.2 MessageMarshaller

A classe `MessageMarshaller` é responsável pelo empacotamento (marshalling) e desempacotamento (unmarshalling) de mensagens de requisição e resposta.

#### 3.2.1 Marshalling de Requisições

```20:40:src/smarthome/net/MessageMarshaller.java
    public static byte[] marshalRequest(int methodId, byte[] arguments) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Escreve methodId (4 bytes)
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(methodId);
        baos.write(buffer.array());
        
        // Escreve tamanho dos argumentos (4 bytes)
        int argsSize = (arguments != null) ? arguments.length : 0;
        buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(argsSize);
        baos.write(buffer.array());
        
        // Escreve argumentos
        if (arguments != null && arguments.length > 0) {
            baos.write(arguments);
        }
        
        return baos.toByteArray();
    }
```

**Formato da requisição:**
- [4 bytes: methodId]
- [4 bytes: tamanho dos argumentos]
- [N bytes: argumentos serializados]

#### 3.2.2 Marshalling de Respostas

```73:93:src/smarthome/net/MessageMarshaller.java
    public static byte[] marshalReply(int status, byte[] reply) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Escreve status (4 bytes)
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(status);
        baos.write(buffer.array());
        
        // Escreve tamanho da resposta (4 bytes)
        int replySize = (reply != null) ? reply.length : 0;
        buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(replySize);
        baos.write(buffer.array());
        
        // Escreve resposta
        if (reply != null && reply.length > 0) {
            baos.write(reply);
        }
        
        return baos.toByteArray();
    }
```

**Formato da resposta:**
- [4 bytes: status]
- [4 bytes: tamanho da resposta]
- [N bytes: resposta serializada]

#### 3.2.3 Serialização/Deserialização

```125:149:src/smarthome/net/MessageMarshaller.java
    public static byte[] serialize(Object obj) throws IOException {
        if (obj == null) {
            return null;
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        return baos.toByteArray();
    }
    
    /**
     * Deserializa um objeto de um array de bytes (passagem por valor).
     */
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        if (data == null || data.length == 0) {
            return null;
        }
        
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        }
    }
```

**Características:**
- Utiliza `ObjectOutputStream` e `ObjectInputStream` do Java para serialização
- Implementa passagem por valor: objetos são serializados antes do envio
- Suporta objetos `null`

### 3.3 ClientRequestHandler

O `ClientRequestHandler` implementa o método `doOperation`, que é o ponto de entrada para invocações remotas do lado do cliente.

```26:82:src/smarthome/net/ClientRequestHandler.java
    public byte[] doOperation(RemoteObjectRef o, int methodId, byte[] arguments) throws IOException {
        if (o == null) {
            throw new IllegalArgumentException("RemoteObjectRef não pode ser null");
        }
        
        // Empacota a requisição
        byte[] requestData = MessageMarshaller.marshalRequest(methodId, arguments);
        
        // Cria socket UDP
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            
            // Empacota RemoteObjectRef junto com a requisição
            byte[] remoteRefData = MessageMarshaller.serialize(o);
            byte[] fullRequest = combineRequestData(remoteRefData, requestData);
            
            // Cria pacote de requisição
            DatagramPacket requestPacket = new DatagramPacket(
                fullRequest,
                fullRequest.length,
                o.getHost(),
                o.getPort()
            );
            
            // Envia requisição
            System.out.println("[CLIENTE] Enviando requisição para " + o.getHost() + ":" + o.getPort() + 
                             " (methodId=" + methodId + ")");
            socket.send(requestPacket);
            
            // Recebe resposta
            byte[] responseBuffer = new byte[MAX_PACKET_SIZE];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
            
            try {
                socket.receive(responsePacket);
                
                // Extrai dados da resposta
                byte[] responseData = new byte[responsePacket.getLength()];
                System.arraycopy(responsePacket.getData(), 0, responseData, 0, responsePacket.getLength());
                
                // Desempacota resposta
                Object[] replyParts = MessageMarshaller.unmarshalReply(responseData);
                int status = (Integer) replyParts[0];
                byte[] reply = (byte[]) replyParts[1];
                
                if (status != 0) { // 0 = sucesso
                    throw new IOException("Erro no servidor: status=" + status);
                }
                
                System.out.println("[CLIENTE] Resposta recebida com sucesso");
                return reply;
                
            } catch (SocketTimeoutException e) {
                throw new IOException("Timeout ao aguardar resposta do servidor", e);
            }
        }
    }
```

**Características:**
- Utiliza UDP (DatagramSocket) para comunicação
- Implementa timeout de 5 segundos para evitar bloqueios
- Empacota `RemoteObjectRef` junto com a requisição
- Trata erros e timeouts adequadamente
- Desempacota a resposta e verifica o status

### 3.4 ServerRequestHandler

O `ServerRequestHandler` implementa os métodos `getRequest` e `sendReply`, que são os pontos de entrada para o servidor processar requisições remotas.

#### 3.4.1 getRequest

```35:79:src/smarthome/net/ServerRequestHandler.java
    public Object[] getRequest() throws IOException {
        byte[] buffer = new byte[MAX_PACKET_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        
        System.out.println("[SERVIDOR] Aguardando requisição...");
        serverSocket.receive(packet);
        
        InetAddress clientHost = packet.getAddress();
        int clientPort = packet.getPort();
        
        System.out.println("[SERVIDOR] Requisição recebida de " + clientHost + ":" + clientPort);
        
        // Extrai dados do pacote
        byte[] data = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
        
        // Desempacota RemoteObjectRef
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(data);
        java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.allocate(Integer.BYTES);
        
        byte[] refSizeBytes = new byte[Integer.BYTES];
        bais.read(refSizeBytes);
        int refSize = java.nio.ByteBuffer.wrap(refSizeBytes).getInt();
        
        RemoteObjectRef remoteRef = null;
        if (refSize > 0) {
            byte[] refData = new byte[refSize];
            bais.read(refData);
            try {
                remoteRef = (RemoteObjectRef) MessageMarshaller.deserialize(refData);
            } catch (ClassNotFoundException e) {
                throw new IOException("Erro ao deserializar RemoteObjectRef", e);
            }
        }
        
        // Desempacota requisição (methodId + arguments)
        byte[] requestData = new byte[bais.available()];
        bais.read(requestData);
        Object[] requestParts = MessageMarshaller.unmarshalRequest(requestData);
        int methodId = (Integer) requestParts[0];
        byte[] arguments = (byte[]) requestParts[1];
        
        // Armazena informações do cliente para resposta
        return new Object[]{remoteRef, methodId, arguments, clientHost, clientPort};
    }
```

**Retorno:** Array contendo:
- `[0]`: RemoteObjectRef
- `[1]`: methodId
- `[2]`: arguments (byte[])
- `[3]`: clientHost (InetAddress)
- `[4]`: clientPort (int)

#### 3.4.2 sendReply

```89:105:src/smarthome/net/ServerRequestHandler.java
    public void sendReply(byte[] reply, InetAddress clientHost, int clientPort) throws IOException {
        // Empacota resposta (status 0 = sucesso)
        byte[] replyData = MessageMarshaller.marshalReply(0, reply);
        
        // Cria pacote de resposta
        DatagramPacket replyPacket = new DatagramPacket(
            replyData,
            replyData.length,
            clientHost,
            clientPort
        );
        
        // Envia resposta
        System.out.println("[SERVIDOR] Enviando resposta para " + clientHost + ":" + clientPort);
        serverSocket.send(replyPacket);
        System.out.println("[SERVIDOR] Resposta enviada com sucesso");
    }
```

**Características:**
- Empacota a resposta com status de sucesso (0)
- Envia resposta para o endereço e porta do cliente
- Utiliza o mesmo socket UDP para envio

### 3.5 ClienteRemoto

O `ClienteRemoto` fornece uma interface de alto nível para o cliente, encapsulando a complexidade da invocação remota.

```22:30:src/smarthome/net/ClienteRemoto.java
    public ClienteRemoto(String host, int porta) throws IOException {
        this.requestHandler = new ClientRequestHandler();
        this.remoteRef = new RemoteObjectRef(
            InetAddress.getByName(host),
            porta,
            1, // objectId único para o serviço SmartHome
            ISmartHomeService.class.getName()
        );
    }
```

**Exemplo de método remoto:**

```35:45:src/smarthome/net/ClienteRemoto.java
    @SuppressWarnings("unchecked")
    public List<DispositivoIoT> listarDispositivos() throws IOException, ClassNotFoundException {
        // Serializa argumentos (passagem por valor - neste caso não há argumentos)
        byte[] arguments = MessageMarshaller.serialize(null);
        
        // Invoca método remoto
        byte[] reply = requestHandler.doOperation(remoteRef, ISmartHomeService.METHOD_LISTAR_DISPOSITIVOS, arguments);
        
        // Deserializa resposta
        return (List<DispositivoIoT>) MessageMarshaller.deserialize(reply);
    }
```

**Fluxo de invocação:**
1. Serializa argumentos (passagem por valor)
2. Chama `doOperation` com `RemoteObjectRef`, `methodId` e argumentos
3. Recebe resposta serializada
4. Deserializa resposta e retorna

### 3.6 ServidorRemoto

O `ServidorRemoto` processa requisições remotas e delega a execução para a implementação do serviço.

```29:61:src/smarthome/net/ServidorRemoto.java
    public void iniciar() {
        System.out.println("=== Servidor Remoto Smart Home (Invocação Remota) ===");
        System.out.println("Servidor iniciado na porta " + PORTA);
        System.out.println("Aguardando requisições de clientes...\n");
        
        try {
            while (true) {
                // Obtém requisição usando getRequest
                Object[] requestData = requestHandler.getRequest();
                
                @SuppressWarnings("unused")
                RemoteObjectRef remoteRef = (RemoteObjectRef) requestData[0];
                int methodId = (Integer) requestData[1];
                byte[] arguments = (byte[]) requestData[2];
                InetAddress clientHost = (InetAddress) requestData[3];
                int clientPort = (Integer) requestData[4];
                
                System.out.println("[SERVIDOR] Processando requisição: methodId=" + methodId);
                
                // Processa a requisição
                byte[] reply = processarRequest(methodId, arguments);
                
                // Envia resposta usando sendReply
                requestHandler.sendReply(reply, clientHost, clientPort);
                System.out.println("[SERVIDOR] Requisição processada com sucesso\n");
            }
        } catch (IOException e) {
            System.err.println("[ERRO] Falha no servidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            requestHandler.close();
        }
    }
```

**Processamento de requisições:**

```67:125:src/smarthome/net/ServidorRemoto.java
    private byte[] processarRequest(int methodId, byte[] arguments) throws IOException {
        try {
            Object result = null;
            
            switch (methodId) {
                case ISmartHomeService.METHOD_LISTAR_DISPOSITIVOS:
                    result = smartHomeService.listarDispositivos();
                    break;
                    
                case ISmartHomeService.METHOD_OBTER_DISPOSITIVO:
                    String dispositivoId = (String) MessageMarshaller.deserialize(arguments);
                    result = smartHomeService.obterDispositivo(dispositivoId);
                    break;
                    
                case ISmartHomeService.METHOD_ATUALIZAR_DISPOSITIVO:
                    Object[] updateArgs = (Object[]) MessageMarshaller.deserialize(arguments);
                    String id = (String) updateArgs[0];
                    DispositivoIoT dispositivo = (DispositivoIoT) updateArgs[1];
                    result = smartHomeService.atualizarDispositivo(id, dispositivo);
                    break;
                    
                case ISmartHomeService.METHOD_EXECUTAR_ACAO:
                    Object[] actionArgs = (Object[]) MessageMarshaller.deserialize(arguments);
                    String deviceId = (String) actionArgs[0];
                    String comando = (String) actionArgs[1];
                    result = smartHomeService.executarAcao(deviceId, comando);
                    break;
                    
                case ISmartHomeService.METHOD_LISTAR_ROTINAS:
                    result = smartHomeService.listarRotinas();
                    break;
                    
                case ISmartHomeService.METHOD_CRIAR_ROTINA:
                    Rotina rotina = (Rotina) MessageMarshaller.deserialize(arguments);
                    result = smartHomeService.criarRotina(rotina);
                    break;
                    
                case ISmartHomeService.METHOD_LISTAR_ALERTAS:
                    result = smartHomeService.listarAlertas();
                    break;
                    
                case ISmartHomeService.METHOD_OBTER_COMODO:
                    String nomeComodo = (String) MessageMarshaller.deserialize(arguments);
                    result = smartHomeService.obterComodo(nomeComodo);
                    break;
                    
                default:
                    throw new IllegalArgumentException("Método não suportado: " + methodId);
            }
            
            // Serializa resultado (passagem por valor)
            return MessageMarshaller.serialize(result);
            
        } catch (ClassNotFoundException e) {
            throw new IOException("Erro ao deserializar argumentos", e);
        } catch (Exception e) {
            throw new IOException("Erro ao processar requisição: " + e.getMessage(), e);
        }
    }
```

**Fluxo de processamento:**
1. Recebe requisição via `getRequest`
2. Deserializa argumentos
3. Identifica método através de `methodId`
4. Executa método no serviço
5. Serializa resultado
6. Envia resposta via `sendReply`

## 4. Protocolo de Comunicação

### 4.1 Protocolo de Requisição

O protocolo de requisição segue o seguinte formato:

```
[4 bytes: tamanho RemoteObjectRef]
[N bytes: RemoteObjectRef serializado]
[4 bytes: methodId]
[4 bytes: tamanho dos argumentos]
[N bytes: argumentos serializados]
```

### 4.2 Protocolo de Resposta

O protocolo de resposta segue o seguinte formato:

```
[4 bytes: status]
[4 bytes: tamanho da resposta]
[N bytes: resposta serializada]
```

### 4.3 Transporte

- **Protocolo:** UDP (User Datagram Protocol)
- **Porta padrão:** 54321
- **Tamanho máximo de pacote:** 65507 bytes (limite UDP)
- **Timeout:** 5 segundos no cliente

## 5. Passagem por Valor vs Passagem por Referência

### 5.1 Passagem por Valor

A implementação utiliza **passagem por valor** para todos os argumentos e retornos:

- Objetos são serializados antes do envio
- Uma cópia do objeto é enviada, não a referência
- Alterações no objeto no cliente não afetam o objeto no servidor e vice-versa

**Exemplo:**

```65:75:src/smarthome/net/ClienteRemoto.java
    public DispositivoIoT atualizarDispositivo(String dispositivoId, DispositivoIoT dispositivo) throws IOException, ClassNotFoundException {
        // Cria objeto com ID e dispositivo para serialização
        Object[] args = {dispositivoId, dispositivo};
        byte[] arguments = MessageMarshaller.serialize(args);
        
        // Invoca método remoto
        byte[] reply = requestHandler.doOperation(remoteRef, ISmartHomeService.METHOD_ATUALIZAR_DISPOSITIVO, arguments);
        
        // Deserializa resposta
        return (DispositivoIoT) MessageMarshaller.deserialize(reply);
    }
```

### 5.2 Passagem por Referência

A `RemoteObjectRef` implementa o conceito de **passagem por referência**:

- A referência ao objeto remoto é serializada e enviada
- O objeto remoto permanece no servidor
- Múltiplas invocações usam a mesma referência

## 6. Interface Remota

A interface `ISmartHomeService` define os métodos remotos disponíveis:

```17:25:src/smarthome/interfaces/ISmartHomeService.java
    // Constantes para methodId
    int METHOD_LISTAR_DISPOSITIVOS = 1;
    int METHOD_OBTER_DISPOSITIVO = 2;
    int METHOD_ATUALIZAR_DISPOSITIVO = 3;
    int METHOD_EXECUTAR_ACAO = 4;
    int METHOD_LISTAR_ROTINAS = 5;
    int METHOD_CRIAR_ROTINA = 6;
    int METHOD_LISTAR_ALERTAS = 7;
    int METHOD_OBTER_COMODO = 8;
```

**Métodos disponíveis:**
1. `listarDispositivos()` - Lista todos os dispositivos IoT
2. `obterDispositivo(String)` - Obtém um dispositivo por ID
3. `atualizarDispositivo(String, DispositivoIoT)` - Atualiza um dispositivo
4. `executarAcao(String, String)` - Executa uma ação em um dispositivo
5. `listarRotinas()` - Lista todas as rotinas
6. `criarRotina(Rotina)` - Cria uma nova rotina
7. `listarAlertas()` - Lista todos os alertas
8. `obterComodo(String)` - Obtém um cômodo por nome

## 7. Fluxo de Comunicação Completo

### 7.1 Fluxo de Invocação Remota

```
Cliente                          Servidor
  |                                |
  |--- doOperation() ------------->|
  |   (RemoteObjectRef,            |
  |    methodId, arguments)        |
  |                                |
  |                                |--- getRequest()
  |                                |   (recebe requisição)
  |                                |
  |                                |--- processarRequest()
  |                                |   (deserializa args,
  |                                |    executa método,
  |                                |    serializa resultado)
  |                                |
  |<-- sendReply() ----------------|
  |   (resposta serializada)       |
  |                                |
  |--- deserializa resposta        |
  |--- retorna resultado           |
```

### 7.2 Detalhamento do Fluxo

1. **Cliente:**
   - Serializa argumentos usando `MessageMarshaller.serialize()`
   - Cria `RemoteObjectRef` com informações do servidor
   - Chama `doOperation()` do `ClientRequestHandler`
   - `doOperation()` empacota requisição e envia via UDP
   - Aguarda resposta com timeout de 5 segundos
   - Desempacota e deserializa resposta
   - Retorna resultado ao cliente

2. **Servidor:**
   - `ServerRequestHandler` recebe pacote UDP via `getRequest()`
   - Desempacota `RemoteObjectRef` e requisição
   - Extrai `methodId` e argumentos
   - `ServidorRemoto` processa requisição:
     - Deserializa argumentos
     - Identifica método através de `methodId`
     - Executa método no `SmartHomeServiceImpl`
     - Serializa resultado
   - Envia resposta via `sendReply()`

## 8. Tratamento de Erros

### 8.1 Erros no Cliente

- **Timeout:** Se o servidor não responder em 5 segundos, lança `IOException`
- **Erro de serialização:** `IOException` ao serializar argumentos
- **Erro de deserialização:** `ClassNotFoundException` ao deserializar resposta
- **Erro do servidor:** Se status != 0, lança `IOException` com mensagem de erro

### 8.2 Erros no Servidor

- **Método não suportado:** `IllegalArgumentException` para `methodId` inválido
- **Erro de deserialização:** `IOException` ao deserializar argumentos
- **Erro na execução:** `IOException` com mensagem de erro do método

## 9. Características da Implementação

### 9.1 Vantagens

1. **Simplicidade:** Implementação direta e fácil de entender
2. **Flexibilidade:** Permite extensão fácil de novos métodos remotos
3. **Independência:** Não depende de frameworks externos
4. **Controle total:** Controle completo sobre o protocolo de comunicação
5. **Passagem por valor:** Garante que objetos não sejam modificados acidentalmente

### 9.2 Limitações

1. **UDP não confiável:** Não garante entrega de pacotes (pode perder pacotes)
2. **Sem conexão persistente:** Cada requisição cria um novo socket
3. **Tamanho limitado:** Limitado ao tamanho máximo de pacote UDP (65507 bytes)
4. **Sem autenticação:** Não implementa mecanismos de segurança
5. **Sem transações:** Não suporta transações distribuídas
6. **Sem callbacks:** Não implementa notificações assíncronas

## 10. Melhorias Futuras

1. **TCP em vez de UDP:** Para garantir entrega confiável
2. **Pool de conexões:** Reutilizar conexões para melhor performance
3. **Compressão:** Comprimir dados grandes antes do envio
4. **Autenticação e autorização:** Implementar segurança
5. **Callbacks:** Suportar notificações assíncronas
6. **Load balancing:** Distribuir carga entre múltiplos servidores
7. **Caching:** Cache de resultados para melhorar performance
8. **Logging estruturado:** Sistema de logs mais robusto

## 11. Conclusão

A implementação de RMI customizada demonstra os conceitos fundamentais de invocação remota de métodos:

- **Marshalling/Unmarshalling:** Serialização e deserialização de mensagens
- **Passagem por valor:** Objetos são copiados, não referenciados
- **Referências remotas:** `RemoteObjectRef` identifica objetos remotos
- **Protocolo de comunicação:** Formato estruturado para requisições e respostas
- **Tratamento de erros:** Mecanismos para lidar com falhas

A arquitetura é modular e permite fácil extensão para novos métodos remotos e funcionalidades adicionais. Embora tenha limitações (principalmente relacionadas ao uso de UDP), a implementação serve como uma base sólida para um sistema de comunicação distribuída.

---

**Data do Relatório:** Dezembro 2024  
**Sistema:** Smart Home Automation System  
**Implementação:** RMI Customizado sobre UDP

