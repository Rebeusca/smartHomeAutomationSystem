# Guia do Serviço Remoto - Cliente-Servidor

## 📋 Visão Geral

Sistema de comunicação cliente-servidor que permite:
- **Cliente** envia requisições (Request) empacotadas
- **Servidor** processa e retorna respostas (Reply) empacotadas
- Toda comunicação usa **serialização customizada** via streams

---

## 🏗️ Arquitetura

### Componentes:

1. **MensagemRequest** - Requisição do cliente
2. **MensagemReply** - Resposta do servidor
3. **MensagemOutputStream** - Empacota mensagens para envio
4. **MensagemInputStream** - Desempacota mensagens recebidas
5. **ServidorRemoto** - Processa requisições
6. **ClienteRemoto** - Envia requisições

---

## 📦 Protocolo de Comunicação

### Formato das Mensagens:

#### Request (Requisição):
```
[1 byte: Tipo = 1]
[4 bytes: Código da Operação]
[4 bytes: Tamanho ID] [N bytes: ID]
[4 bytes: Tamanho Dados] [N bytes: Dados]
```

#### Reply (Resposta):
```
[1 byte: Tipo = 2]
[4 bytes: Código do Status]
[4 bytes: Tamanho Mensagem] [N bytes: Mensagem]
[4 bytes: Número de Dispositivos]
[Bytes dos Dispositivos serializados]
[1 byte: Existe Dispositivo Único?]
[Bytes do Dispositivo único (se existir)]
```

---

## 🚀 Como Usar

### 1. Compilar o Projeto

```bash
javac -d out -cp out src/smarthome/pojos/*.java src/smarthome/streams/*.java src/smarthome/net/*.java
```

### 2. Iniciar o Servidor

**Terminal 1:**
```bash
java -cp out smarthome.net.ServidorRemoto
```

O servidor ficará aguardando conexões na porta **54321**.

### 3. Executar o Cliente

**Terminal 2:**
```bash
java -cp out smarthome.net.ClienteRemoto
```

---

## 📝 Operações Disponíveis

### 1. LISTAR_DISPOSITIVOS

**Cliente:**
```java
DispositivoIoT[] dispositivos = cliente.listarDispositivos();
```

**Servidor:** Retorna todos os dispositivos cadastrados.

---

### 2. OBTER_DISPOSITIVO

**Cliente:**
```java
DispositivoIoT dispositivo = cliente.obterDispositivo("id-do-dispositivo");
```

**Servidor:** Retorna um dispositivo específico por ID.

---

### 3. ATUALIZAR_DISPOSITIVO

**Cliente:**
```java
DispositivoIoT dispositivo = cliente.atualizarDispositivo("id", "online=true");
```

**Servidor:** Atualiza um dispositivo e retorna o dispositivo atualizado.

---

### 4. EXECUTAR_ACAO

**Cliente:**
```java
DispositivoIoT dispositivo = cliente.executarAcao("id", "ligar");
```

**Servidor:** Executa uma ação no dispositivo e retorna o resultado.

---

## 🔄 Fluxo de Comunicação

### Cenário: Cliente lista dispositivos

```
┌─────────────┐                    ┌─────────────┐
│   CLIENTE   │                    │   SERVIDOR  │
└─────────────┘                    └─────────────┘
       │                                   │
       │ 1. Cria MensagemRequest           │
       │    (LISTAR_DISPOSITIVOS)          │
       │                                   │
       │ 2. Empacota Request               │
       │    (MensagemOutputStream)         │
       │                                   │
       │ 3. Envia bytes via TCP            │
       │──────────────────────────────────>│
       │                                   │
       │                                   │ 4. Desempacota Request
       │                                   │    (MensagemInputStream)
       │                                   │
       │                                   │ 5. Processa requisição
       │                                   │    (busca dispositivos)
       │                                   │
       │                                   │ 6. Cria MensagemReply
       │                                   │    (com dispositivos)
       │                                   │
       │                                   │ 7. Empacota Reply
       │                                   │    (MensagemOutputStream)
       │                                   │
       │ 8. Recebe bytes via TCP           │
       │<──────────────────────────────────│
       │                                   │
       │ 9. Desempacota Reply              │
       │    (MensagemInputStream)          │
       │                                   │
       │ 10. Processa resposta             │
       │     (extrai dispositivos)        │
       │                                   │
```

---

## 📊 Serialização (Empacotamento/Desempacotamento)

### Cliente - Empacotar Request:

```java
MensagemRequest request = new MensagemRequest(
    MensagemRequest.TipoOperacao.LISTAR_DISPOSITIVOS
);

// Empacota
MensagemOutputStream out = new MensagemOutputStream(socket.getOutputStream());
out.escreverRequest(request);
```

**O que acontece:**
1. Escreve tipo (1 = Request)
2. Escreve código da operação
3. Escreve ID (se existir)
4. Escreve dados (se existir)
5. Envia bytes via TCP

### Servidor - Desempacotar Request:

```java
// Desempacota
MensagemInputStream in = new MensagemInputStream(socket.getInputStream());
MensagemRequest request = in.lerRequest();
```

**O que acontece:**
1. Lê tipo (deve ser 1)
2. Lê código da operação
3. Lê ID
4. Lê dados
5. Reconstrói MensagemRequest

### Servidor - Empacotar Reply:

```java
MensagemReply reply = new MensagemReply();
reply.setStatus(MensagemReply.Status.SUCESSO);
reply.setDispositivos(dispositivos);

// Empacota
MensagemOutputStream out = new MensagemOutputStream(socket.getOutputStream());
out.escreverReply(reply);
```

**O que acontece:**
1. Escreve tipo (2 = Reply)
2. Escreve código do status
3. Escreve mensagem
4. Escreve array de dispositivos (usando DispositivoIoTOutputStream)
5. Escreve dispositivo único (se existir)
6. Envia bytes via TCP

### Cliente - Desempacotar Reply:

```java
// Desempacota
MensagemInputStream in = new MensagemInputStream(socket.getInputStream());
MensagemReply reply = in.lerReply();
```

**O que acontece:**
1. Lê tipo (deve ser 2)
2. Lê código do status
3. Lê mensagem
4. Lê array de dispositivos (usando DispositivoIoTInputStream)
5. Lê dispositivo único (se existir)
6. Reconstrói MensagemReply

---

## 🎯 Exemplo Completo

### Código do Cliente:

```java
ClienteRemoto cliente = new ClienteRemoto("localhost", 54321);

// Listar dispositivos
DispositivoIoT[] dispositivos = cliente.listarDispositivos();
System.out.println("Total: " + dispositivos.length);

// Obter dispositivo específico
DispositivoIoT disp = cliente.obterDispositivo(dispositivos[0].getId());
System.out.println("Dispositivo: " + disp);

// Atualizar dispositivo
dispositivo = cliente.atualizarDispositivo(disp.getId(), "online=true");
System.out.println("Atualizado: " + dispositivo);
```

### O que acontece internamente:

1. **Cliente cria Request** → `MensagemRequest`
2. **Cliente empacota** → `MensagemOutputStream.escreverRequest()`
3. **Cliente envia** → Bytes via TCP
4. **Servidor recebe** → Bytes via TCP
5. **Servidor desempacota** → `MensagemInputStream.lerRequest()`
6. **Servidor processa** → Busca dispositivos
7. **Servidor cria Reply** → `MensagemReply`
8. **Servidor empacota** → `MensagemOutputStream.escreverReply()`
9. **Servidor envia** → Bytes via TCP
10. **Cliente recebe** → Bytes via TCP
11. **Cliente desempacota** → `MensagemInputStream.lerReply()`
12. **Cliente processa** → Extrai dispositivos

---

## 🔍 Verificando o Funcionamento

### 1. Inicie o servidor:
```
=== Servidor Remoto Smart Home ===
Servidor iniciado na porta 54321
Aguardando conexões de clientes...
```

### 2. Execute o cliente:
```
=== Cliente Remoto Smart Home ===

1. Listando dispositivos...
[CLIENTE] Enviando requisição: MensagemRequest{...}
[CLIENTE] Resposta recebida: MensagemReply{...}
   Total: 3 dispositivos
   - abc-123: Luz Sala
   - def-456: Ar Condicionado
   - ghi-789: Sensor Movimento
```

### 3. No servidor aparecerá:
```
[CONEXÃO] Cliente conectado: /127.0.0.1:xxxxx
[REQUEST] MensagemRequest{tipoOperacao=LISTAR_DISPOSITIVOS, ...}
[REPLY] MensagemReply{status=SUCESSO, ...}
[OK] Resposta enviada ao cliente
```

---

## 📚 Resumo

### O que foi implementado:

✅ **Empacotamento (Cliente):**
- Cliente empacota Request antes de enviar
- Usa `MensagemOutputStream.escreverRequest()`

✅ **Desempacotamento (Servidor):**
- Servidor desempacota Request recebido
- Usa `MensagemInputStream.lerRequest()`

✅ **Empacotamento (Servidor):**
- Servidor empacota Reply antes de enviar
- Usa `MensagemOutputStream.escreverReply()`

✅ **Desempacotamento (Cliente):**
- Cliente desempacota Reply recebido
- Usa `MensagemInputStream.lerReply()`

### Protocolo:

- **TCP**: Comunicação via sockets
- **Streams de bytes**: Toda comunicação usa bytes
- **Serialização customizada**: Formato definido de empacotamento
- **Reutilização**: Usa `DispositivoIoTInputStream/OutputStream` para dispositivos

---

## 🎓 Conceitos Implementados

1. **Serialização**: Conversão de objetos em bytes
2. **Deserialização**: Conversão de bytes em objetos
3. **Empacotamento**: Organização dos dados em formato específico
4. **Desempacotamento**: Extração dos dados do formato
5. **Protocolo**: Formato definido de comunicação
6. **Cliente-Servidor**: Arquitetura de comunicação remota

---

## ✅ Conclusão

O sistema implementa completamente:
- ✅ Comunicação cliente-servidor via TCP
- ✅ Serialização/deserialização de mensagens
- ✅ Empacotamento/desempacotamento bidirecional
- ✅ Protocolo definido e funcional
- ✅ Operações básicas (listar, obter, atualizar, executar)

O sistema está pronto para uso! 🎉

