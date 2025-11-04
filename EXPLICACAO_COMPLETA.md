# Explicação Completa - Sistema de Streams Smart Home

## 📚 ÍNDICE

1. [Visão Geral](#visão-geral)
2. [O Problema](#o-problema)
3. [A Solução](#a-solução)
4. [Implementação Detalhada](#implementação-detalhada)
5. [Como Funciona na Prática](#como-funciona-na-prática)
6. [Os Testes](#os-testes)
7. [Fluxo Completo de Dados](#fluxo-completo-de-dados)

---

## 🎯 VISÃO GERAL

Você tem um sistema Smart Home com dispositivos IoT (Lâmpadas, Termostatos, Sensores). 

**Problema:** Como salvar/carregar esses objetos em arquivos, enviar pela rede ou usar entrada padrão?

**Solução:** Criar streams customizados que convertem objetos em bytes e vice-versa.

---

## ❓ O PROBLEMA

### O que você precisa fazer:

1. **Salvar** objetos `DispositivoIoT` em arquivo
2. **Enviar** objetos pela rede TCP
3. **Ler** objetos de arquivo
4. **Receber** objetos pela rede
5. **Ler** objetos da entrada padrão (System.in)

### Por que não usar Serialização padrão?

Você precisa de **controle total** sobre o formato dos bytes:
- Formato customizado
- Apenas alguns atributos (não todos)
- Compatibilidade com diferentes origens (arquivo, rede, stdin)

---

## ✅ A SOLUÇÃO

### Dois Streams Customizados:

1. **`DispositivoIoTOutputStream`** → **ESCREVE** objetos como bytes
2. **`DispositivoIoTInputStream`** → **LÊ** bytes e reconstrói objetos

### Por que Streams?

- **Streams** são a forma padrão Java de trabalhar com I/O
- Funcionam com **qualquer origem/destino** (arquivo, rede, stdin)
- **Reutilizáveis** - mesmo código para diferentes origens

---

## 🔧 IMPLEMENTAÇÃO DETALHADA

### 1. DispositivoIoTOutputStream (ESCREVER)

**O que faz:** Converte objetos `DispositivoIoT` em sequência de bytes.

#### Estrutura da Classe:

```java
public class DispositivoIoTOutputStream extends OutputStream {
    private final DispositivoIoT[] dispositivos;  // Array de objetos a enviar
    private final int numObjetos;                // Quantidade de objetos
    private final OutputStream destino;         // Para onde enviar (arquivo/rede/stdout)
}
```

#### Protocolo de Gravação (Formato dos Bytes):

```
[4 bytes: Número de objetos]
Para cada objeto:
  [4 bytes: Tamanho do ID] [N bytes: ID]
  [4 bytes: Tamanho do Nome] [N bytes: Nome]
  [1 byte: Online (1=true, 0=false)]
  [4 bytes: Tamanho do Cômodo] [N bytes: Cômodo]
```

#### Métodos Principais:

**1. `writeObjects()`** - Método principal que inicia a gravação:
```java
public void writeObjects() throws IOException {
    writeInt(numObjetos);  // Escreve quantos objetos serão enviados
    
    for (int i = 0; i < numObjetos; i++) {
        gravarDispositivo(dispositivos[i]);  // Grava cada dispositivo
    }
    
    destino.flush();  // Garante que tudo foi enviado
}
```

**2. `gravarDispositivo()`** - Grava os atributos de um dispositivo:
```java
private void gravarDispositivo(DispositivoIoT disp) throws IOException {
    writeString(disp.getId());        // ID
    writeString(disp.getNome());      // Nome
    destino.write(disp.getOnline() ? 1 : 0);  // Online (1 byte)
    writeString(disp.getComodo());    // Cômodo
}
```

**3. `writeString()`** - Escreve uma String (tamanho + dados):
```java
private void writeString(String s) throws IOException {
    byte[] bytes = s.getBytes(StandardCharsets.UTF_8);  // Converte String para bytes
    writeInt(bytes.length);  // Escreve o tamanho (4 bytes)
    destino.write(bytes);     // Escreve os dados da string
}
```

**4. `writeInt()`** - Escreve um inteiro (4 bytes):
```java
private void writeInt(int v) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(4);  // Buffer de 4 bytes
    buffer.putInt(v);                            // Coloca o inteiro no buffer
    destino.write(buffer.array());               // Escreve os 4 bytes
}
```

#### Por que este formato?

- **Tamanho antes dos dados**: Para saber quantos bytes ler depois
- **4 bytes para inteiros**: Formato padrão (Big-Endian)
- **UTF-8 para strings**: Suporta caracteres especiais
- **1 byte para boolean**: Economia de espaço

---

### 2. DispositivoIoTInputStream (LER)

**O que faz:** Lê bytes e reconstrói objetos `DispositivoIoT`.

#### Estrutura da Classe:

```java
public class DispositivoIoTInputStream extends InputStream {
    private final InputStream origem;  // De onde ler (arquivo/rede/stdin)
}
```

#### Métodos Principais:

**1. `readObjects()`** - Método principal que lê todos os objetos:
```java
public DispositivoIoT[] readObjects() throws IOException {
    int numObjetos = readInt();  // Lê quantos objetos existem
    
    List<DispositivoIoT> lista = new ArrayList<>();
    
    for (int i = 0; i < numObjetos; i++) {
        lista.add(readDispositivo());  // Lê cada dispositivo
    }
    
    return lista.toArray(new DispositivoIoT[0]);
}
```

**2. `readDispositivo()`** - Lê um dispositivo e reconstrói:
```java
private DispositivoIoT readDispositivo() throws IOException {
    String id = readString();      // Lê ID
    String nome = readString();    // Lê Nome
    boolean online = read() == 1;  // Lê Online (1 byte)
    String comodo = readString();  // Lê Cômodo
    
    // Reconstrói o objeto (usa Lampada como exemplo)
    Lampada dispositivo = new Lampada(nome, comodo, online, false, 0, 0);
    dispositivo.setId(id);
    
    return dispositivo;
}
```

**3. `readString()`** - Lê uma String (tamanho + dados):
```java
private String readString() throws IOException {
    int length = readInt();  // Lê o tamanho (4 bytes)
    
    byte[] bytes = new byte[length];
    
    // Garante que todos os bytes sejam lidos
    int offset = 0;
    while (offset < length) {
        int bytesRead = origem.read(bytes, offset, length - offset);
        if (bytesRead == -1) {
            throw new IOException("Fim inesperado do stream");
        }
        offset += bytesRead;
    }
    
    return new String(bytes, StandardCharsets.UTF_8);  // Converte bytes para String
}
```

**4. `readInt()`** - Lê um inteiro (4 bytes):
```java
private int readInt() throws IOException {
    byte[] bytes = new byte[4];
    
    // Garante que 4 bytes sejam lidos
    int offset = 0;
    while (offset < 4) {
        int bytesRead = origem.read(bytes, offset, 4 - offset);
        if (bytesRead == -1) {
            throw new IOException("Fim inesperado do stream");
        }
        offset += bytesRead;
    }
    
    return ByteBuffer.wrap(bytes).getInt();  // Converte bytes para inteiro
}
```

#### Por que ler em loop?

- Streams podem não retornar todos os bytes de uma vez
- Garante que **todos** os bytes necessários sejam lidos
- Previne erros de dados incompletos

---

## 🎬 COMO FUNCIONA NA PRÁTICA

### Exemplo: Escrever e Ler um Dispositivo

**Dados de entrada:**
```java
Lampada lampada = new Lampada("Luz Sala", "Sala", true, true, 80, 3000);
// ID: "abc-123"
// Nome: "Luz Sala"
// Online: true
// Cômodo: "Sala"
```

**1. ESCREVER (OutputStream):**
```
Bytes escritos:
[00 00 00 01]           ← 1 objeto
[00 00 00 07][abc-123]  ← ID: tamanho 7 + "abc-123"
[00 00 00 08][Luz Sala] ← Nome: tamanho 8 + "Luz Sala"
[01]                    ← Online: true (1)
[00 00 00 04][Sala]    ← Cômodo: tamanho 4 + "Sala"
```

**2. LER (InputStream):**
```
Lê [00 00 00 01] → 1 objeto
Lê [00 00 00 07] → tamanho 7
Lê [abc-123] → ID = "abc-123"
Lê [00 00 00 08] → tamanho 8
Lê [Luz Sala] → Nome = "Luz Sala"
Lê [01] → Online = true
Lê [00 00 00 04] → tamanho 4
Lê [Sala] → Cômodo = "Sala"

Reconstrói objeto Lampada com esses dados
```

---

## 🧪 OS TESTES

### Por que testar?

Para garantir que o código funciona com **diferentes origens de dados**:
- Arquivo
- Entrada padrão (System.in)
- Rede TCP

### Estrutura dos Testes:

```
GeradorDados.java    → Cria arquivo teste.bin com dados de exemplo
TesteArquivo.java    → Testa leitura de arquivo
TesteSystemIn.java   → Testa leitura de System.in
ServidorTCP.java     → Servidor que envia dados via TCP
TesteTCP.java        → Cliente que recebe dados via TCP
```

---

### TESTE 1: GeradorDados.java

**O que faz:** Cria arquivo binário com dados de teste.

```java
// Cria 3 dispositivos
Lampada l1 = new Lampada("Luz Principal", "Sala", true, true, 85, 3000);
Termostato t1 = new Termostato("Ar Condicionado", "Quarto", true, 24.5, 22.0);
Sensor s1 = new Sensor("Sensor Movimento", "Corredor", true, "Movimento", false, 0.0);

DispositivoIoT[] dispositivos = {l1, t1, s1};

// Escreve no arquivo usando DispositivoIoTOutputStream
try (FileOutputStream fos = new FileOutputStream("teste.bin");
     DispositivoIoTOutputStream stream = 
         new DispositivoIoTOutputStream(dispositivos, dispositivos.length, fos)) {
    
    stream.writeObjects();  // Converte objetos em bytes e salva
}
```

**Resultado:** Arquivo `teste.bin` criado com os bytes dos 3 dispositivos.

---

### TESTE 2: TesteArquivo.java (Teste c)

**O que faz:** Lê dispositivos de um arquivo.

```java
// Abre o arquivo
try (FileInputStream fis = new FileInputStream("teste.bin");
     DispositivoIoTInputStream stream = new DispositivoIoTInputStream(fis)) {
    
    // Lê os objetos
    DispositivoIoT[] dispositivos = stream.readObjects();
    
    // Exibe os resultados
    for (DispositivoIoT disp : dispositivos) {
        System.out.println(disp.toString());
    }
}
```

**Fluxo:**
1. Abre `teste.bin` como `FileInputStream`
2. Passa para `DispositivoIoTInputStream`
3. Lê os bytes e reconstrói os objetos
4. Exibe os objetos reconstruídos

---

### TESTE 3: TesteSystemIn.java (Teste b)

**O que faz:** Lê dispositivos da entrada padrão (System.in).

```java
// System.in é automaticamente a entrada padrão
try (DispositivoIoTInputStream stream = new DispositivoIoTInputStream(System.in)) {
    
    DispositivoIoT[] dispositivos = stream.readObjects();
    
    // Exibe os resultados
    for (DispositivoIoT disp : dispositivos) {
        System.err.println(disp.toString());
    }
}
```

**Como usar:**
```bash
# Redireciona o arquivo para System.in
java -cp out smarthome.testes.TesteSystemIn < teste.bin
```

**Fluxo:**
1. Arquivo `teste.bin` é redirecionado para `System.in`
2. `DispositivoIoTInputStream` lê de `System.in`
3. Reconstrói os objetos
4. Exibe os resultados

**Por que usar System.in?**
- Permite pipe entre programas
- Útil para scripts e automação
- Testa se funciona com entrada padrão

---

### TESTE 4: ServidorTCP.java + TesteTCP.java (Teste d)

**ServidorTCP.java** - Envia dados via rede:

```java
// Cria servidor na porta 12345
ServerSocket serverSocket = new ServerSocket(12345);
Socket clientSocket = serverSocket.accept();  // Espera cliente conectar

// Cria dados de teste
Lampada l1 = new Lampada("Luz Cozinha", "Cozinha", true, false, 50, 4000);
Sensor s1 = new Sensor("Sensor Fumaça", "Cozinha", true, "Fumaça", false, 0.0);
DispositivoIoT[] dispositivos = {l1, s1};

// Envia via TCP usando DispositivoIoTOutputStream
try (OutputStream os = clientSocket.getOutputStream();
     DispositivoIoTOutputStream stream = 
         new DispositivoIoTOutputStream(dispositivos, dispositivos.length, os)) {
    
    stream.writeObjects();  // Converte objetos em bytes e envia pela rede
}
```

**TesteTCP.java** - Recebe dados via rede:

```java
// Conecta ao servidor
Socket socket = new Socket("localhost", 12345);

// Recebe via TCP usando DispositivoIoTInputStream
try (InputStream is = socket.getInputStream();
     DispositivoIoTInputStream stream = new DispositivoIoTInputStream(is)) {
    
    DispositivoIoT[] dispositivos = stream.readObjects();
    
    // Exibe os resultados
    for (DispositivoIoT disp : dispositivos) {
        System.out.println(disp.toString());
    }
}
```

**Fluxo:**
1. Servidor espera conexão na porta 12345
2. Cliente conecta ao servidor
3. Servidor envia bytes usando `DispositivoIoTOutputStream`
4. Cliente recebe bytes usando `DispositivoIoTInputStream`
5. Cliente reconstrói os objetos e exibe

**Por que testar TCP?**
- Simula comunicação entre máquinas
- Testa se funciona com streams de rede
- Valida que o protocolo funciona em rede

---

## 🔄 FLUXO COMPLETO DE DADOS

### Cenário: Salvar e Carregar Dispositivos

```
┌─────────────────────────────────────────────────────────┐
│ 1. CRIAR DISPOSITIVOS                                   │
│    Lampada l1 = new Lampada(...);                       │
│    Termostato t1 = new Termostato(...);                 │
│    DispositivoIoT[] dispositivos = {l1, t1};           │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│ 2. ESCREVER (DispositivoIoTOutputStream)                │
│    FileOutputStream fos = new FileOutputStream(...);    │
│    DispositivoIoTOutputStream out =                     │
│        new DispositivoIoTOutputStream(dispositivos, ...);│
│    out.writeObjects();                                   │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│ 3. CONVERSÃO PARA BYTES                                 │
│    [4 bytes: 2 objetos]                                 │
│    [Objeto 1: ID + Nome + Online + Cômodo]             │
│    [Objeto 2: ID + Nome + Online + Cômodo]             │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│ 4. BYTES SALVOS (em arquivo/rede/stdout)                │
│    teste.bin contém os bytes                            │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│ 5. LER (DispositivoIoTInputStream)                       │
│    FileInputStream fis = new FileInputStream(...);      │
│    DispositivoIoTInputStream in =                        │
│        new DispositivoIoTInputStream(fis);               │
│    DispositivoIoT[] lidos = in.readObjects();           │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│ 6. RECONSTRUÇÃO DOS OBJETOS                             │
│    Lê bytes → Converte para Strings/boolean             │
│    Cria novas instâncias de Lampada/Termostato          │
│    Seta os atributos lidos                              │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│ 7. OBJETOS RECONSTRUÍDOS                                │
│    DispositivoIoT[] dispositivosLidos                   │
│    (com os mesmos dados dos originais)                  │
└─────────────────────────────────────────────────────────┘
```

---

## 📝 RESUMO

### O que você tem:

1. **DispositivoIoTOutputStream**: Converte objetos em bytes
2. **DispositivoIoTInputStream**: Converte bytes em objetos
3. **Protocolo de comunicação**: Formato definido de bytes
4. **Testes**: Validam que funciona com diferentes origens

### Por que funciona:

- **Mesmo protocolo**: Ambos usam o mesmo formato de bytes
- **Abstração de Streams**: Funciona com qualquer InputStream/OutputStream
- **Leitura/Escrita confiável**: Garante que todos os bytes sejam lidos/escritos

### Quando usar:

- Salvar estado de dispositivos em arquivo
- Enviar dispositivos pela rede
- Receber dispositivos via rede
- Integrar com outros programas via stdin/stdout

---

## 🎓 CONCEITOS IMPORTANTES

### Streams em Java:

- **InputStream**: Lê dados (arquivo, rede, stdin)
- **OutputStream**: Escreve dados (arquivo, rede, stdout)
- **FileInputStream/FileOutputStream**: Para arquivos
- **Socket.getInputStream/OutputStream**: Para rede TCP
- **System.in/out**: Entrada/saída padrão

### Por que criar Streams customizados?

- **Reutilização**: Mesmo código para diferentes origens
- **Abstração**: Não precisa saber se é arquivo ou rede
- **Padrão Java**: Segue o padrão de I/O do Java
- **Flexibilidade**: Funciona com qualquer InputStream/OutputStream

---