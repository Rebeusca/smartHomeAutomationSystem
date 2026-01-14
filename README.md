# Smart Home Automation System

Este diretório contém implementações de clientes em diferentes linguagens de programação para interagir com o servidor Smart Home.

## Requisitos

### Servidor Java
O servidor deve estar rodando na porta 8080:
```bash
java -cp out smarthome.net.ServidorRemotoAPI
```

## Clientes Disponíveis

### 1. Cliente Python

**Requisitos:**
- Python 3.6 ou superior
- Biblioteca padrão (urllib) - não requer instalação adicional

**Uso:**
```bash
cd clientes/python
python cliente_remoto.py
```

**Menu Interativo:**
O cliente possui um menu interativo que permite escolher operações:
- Digite o número da opção desejada
- Siga as instruções na tela
- Pressione Enter após cada operação para voltar ao menu

**Exemplo de código:**
```python
from cliente_remoto import ClienteRemotoAPI

cliente = ClienteRemotoAPI("localhost", 8080)

# Listar dispositivos
dispositivos = cliente.listar_dispositivos()
print(f"Total: {len(dispositivos)} dispositivos")

# Obter dispositivo específico
dispositivo = cliente.obter_dispositivo("id-do-dispositivo")

# Executar ação
resultado = cliente.executar_acao("id-do-dispositivo", "ligar")
```

### 2. Cliente JavaScript/Node.js

**Requisitos:**
- Node.js 12 ou superior
- Apenas módulos nativos (http) - não requer npm install

**Uso:**
```bash
cd clientes/javascript
node cliente_remoto.js
```

**Menu Interativo:**
O cliente possui um menu interativo que permite escolher operações:
- Digite o número da opção desejada
- Siga as instruções na tela
- Pressione Enter após cada operação para voltar ao menu

**Exemplo de código:**
```javascript
const ClienteRemotoAPI = require('./cliente_remoto');

const cliente = new ClienteRemotoAPI('localhost', 8080);

// Listar dispositivos
cliente.listarDispositivos()
    .then(dispositivos => {
        console.log(`Total: ${dispositivos.length} dispositivos`);
    });

// Obter dispositivo específico
cliente.obterDispositivo('id-do-dispositivo')
    .then(dispositivo => {
        console.log(dispositivo);
    });

// Executar ação
cliente.executarAcao('id-do-dispositivo', 'ligar')
    .then(resultado => {
        console.log(resultado);
    });
```

### 3. Cliente Java

O cliente Java original também está disponível:
```bash
java -cp out smarthome.net.ClienteRemotoAPI
```

## API Endpoints

Todos os clientes acessam os mesmos endpoints REST:

- `GET /api/dispositivos` - Lista todos os dispositivos
- `POST /api/dispositivos/obter` - Obtém um dispositivo por ID
- `POST /api/dispositivos/atualizar` - Atualiza um dispositivo
- `POST /api/dispositivos/acao` - Executa uma ação em um dispositivo
- `GET /api/rotinas` - Lista todas as rotinas
- `POST /api/rotinas/criar` - Cria uma nova rotina
- `GET /api/alertas` - Lista todos os alertas
- `POST /api/comodos/obter` - Obtém um cômodo por nome

## Formato de Comunicação

O servidor suporta dois formatos:

1. **Serialização Java** (padrão): Usado pelo cliente Java
   - Content-Type: `application/octet-stream`
   - Usa ObjectOutputStream/ObjectInputStream

2. **JSON**: Usado pelos clientes Python e JavaScript
   - Content-Type: `application/json`
   - Accept: `application/json`
   - Formato JSON padrão

## Notas

- Os clientes Python e JavaScript enviam requisições com `Content-Type: application/json` e `Accept: application/json`
- O servidor detecta automaticamente o formato desejado e retorna a resposta no formato apropriado
- Para operações complexas (atualizar dispositivo, criar rotina), ainda é necessário usar serialização Java ou implementar um parser JSON completo no servidor
