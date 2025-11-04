# Guia do Sistema de Votação

## 📋 Visão Geral

Sistema de votação distribuído que suporta:
- **Eleitores**: Fazem login e votam em candidatos
- **Administradores**: Gerenciam candidatos, iniciam/encerram votação e enviam notas informativas
- **Comunicação TCP**: Login, lista de candidatos, votos, gerenciamento
- **Comunicação UDP Multicast**: Notas informativas dos administradores
- **Prazo de Votação**: Votação com prazo máximo configurável
- **Resultados**: Cálculo automático de votos, percentagens e vencedor

---

## 🏗️ Arquitetura

### Componentes:

1. **ServidorVotacao** - Servidor TCP multi-threaded (porta 54322)
2. **ServidorMulticastNotas** - Servidor UDP multicast (230.0.0.1:54323)
3. **ClienteEleitor** - Cliente para eleitores votarem
4. **ClienteAdmin** - Cliente para administradores gerenciarem
5. **ClienteMulticastNotas** - Cliente para receber notas informativas

### Representação de Dados:
- **JSON** para serialização de dados nas mensagens TCP
- **JSON** para mensagens multicast UDP

---

## 🚀 Como Compilar e Executar

### 1. Compilar o Projeto

```bash
javac -d out -cp out src/smarthome/net/votacao/*.java
```

### 2. Iniciar o Servidor de Votação

**Terminal 1:**
```bash
java -cp out smarthome.net.votacao.ServidorVotacao
```

O servidor ficará aguardando conexões na porta **54322**.

### 3. Iniciar Cliente Multicast (opcional, para receber notas)

**Terminal 2:**
```bash
java -cp out smarthome.net.votacao.ClienteMulticastNotas
```

Este cliente receberá todas as notas informativas enviadas via multicast.

### 4. Executar Cliente Administrador

**Terminal 3:**
```bash
java -cp out smarthome.net.votacao.ClienteAdmin
```

Credenciais de administrador:
- Username: `admin` / Senha: `admin123`
- Username: `admin2` / Senha: `admin456`

### 5. Executar Cliente Eleitor

**Terminal 4 (e outros):**
```bash
java -cp out smarthome.net.votacao.ClienteEleitor
```

Credenciais de eleitor:
- Username: `eleitor1` / Senha: `senha1`
- Username: `eleitor2` / Senha: `senha2`
- Username: `eleitor3` / Senha: `senha3`

---

## 📝 Fluxo de Uso

### Passo 1: Administrador Inicia a Votação

1. Execute `ClienteAdmin`
2. Faça login com credenciais de administrador
3. Escolha opção **1** para iniciar a votação
4. Defina a duração em minutos (ex: 5 minutos)

### Passo 2: Eleitores Votam

1. Execute `ClienteEleitor` (um por eleitor)
2. Faça login com credenciais de eleitor
3. O sistema exibe a lista de candidatos
4. Digite o ID do candidato escolhido
5. O voto é registrado

### Passo 3: Administrador Encerra a Votação

1. No `ClienteAdmin`, escolha opção **2** para encerrar a votação
2. Os resultados são exibidos automaticamente

### Passo 4: Ver Resultados

1. Qualquer usuário logado pode escolher opção **5** no menu admin (ou implementar no eleitor)
2. Os resultados mostram:
   - Total de votos
   - Votos por candidato
   - Percentuais
   - Candidato vencedor

### Passo 5: Enviar Notas Informativas (Multicast)

1. No `ClienteAdmin`, escolha opção **6**
2. Digite o título e a mensagem da nota
3. A nota é enviada via multicast UDP
4. Todos os clientes multicast conectados recebem a nota

---

## 🔄 Operações TCP

### Eleitores:

| Operação | Descrição |
|----------|-----------|
| `LOGIN` | Autentica o eleitor no sistema |
| `LISTAR_CANDIDATOS` | Obtém lista de candidatos disponíveis |
| `VOTAR` | Registra voto em um candidato |

### Administradores:

| Operação | Descrição |
|----------|-----------|
| `LOGIN` | Autentica o administrador |
| `INICIAR_VOTACAO` | Inicia a votação com prazo configurável |
| `ENCERRAR_VOTACAO` | Encerra a votação e calcula resultados |
| `ADICIONAR_CANDIDATO` | Adiciona um novo candidato |
| `REMOVER_CANDIDATO` | Remove um candidato |
| `OBTER_RESULTADOS` | Obtém os resultados da votação |

---

## 📡 Comunicação Multicast (UDP)

### Notas Informativas:

- **Grupo Multicast**: `230.0.0.1`
- **Porta**: `54323`
- **Formato**: JSON
- **Uso**: Apenas administradores enviam notas

### Exemplo de Nota:

```json
{
  "titulo": "Importante",
  "mensagem": "A votação será encerrada em 5 minutos",
  "admin": "admin",
  "timestamp": 1234567890
}
```

---

## 🔒 Segurança e Regras

1. **Login Obrigatório**: Todas as operações requerem login
2. **Um Voto por Eleitor**: Eleitores não podem votar duas vezes
3. **Prazo de Votação**: Após o prazo, votos não são aceitos
4. **Administradores não votam**: Apenas eleitores podem votar
5. **Resultados após encerramento**: Resultados só disponíveis após encerrar a votação

---

## 📊 Estrutura de Dados

### Candidato:
```json
{
  "id": "cand1",
  "nome": "João Silva",
  "votos": 5
}
```

### Resultado:
```json
{
  "totalVotos": 10,
  "vencedor": {
    "id": "cand1",
    "nome": "João Silva",
    "votos": 5
  },
  "resultados": [
    {
      "candidato": {...},
      "votos": 5,
      "percentual": 50.0
    }
  ]
}
```

---

## ✅ Funcionalidades Implementadas

- ✅ Login de eleitores e administradores
- ✅ Lista de candidatos enviada após login
- ✅ Sistema de votação com prazo máximo
- ✅ Cálculo de total de votos, percentagens e vencedor
- ✅ Administradores podem adicionar/remover candidatos
- ✅ Notas informativas via multicast UDP
- ✅ Servidor TCP multi-threaded
- ✅ Representação externa de dados via JSON
- ✅ Controle de sessão e autenticação

---

## 🎯 Exemplo de Uso Completo

### Terminal 1 - Servidor:
```bash
java -cp out smarthome.net.votacao.ServidorVotacao
```

### Terminal 2 - Admin:
```bash
java -cp out smarthome.net.votacao.ClienteAdmin
# Login: admin / admin123
# Opção 1: Iniciar votação (5 minutos)
# Opção 3: Adicionar candidato
# Opção 6: Enviar nota informativa
# Opção 2: Encerrar votação
```

### Terminal 3 - Eleitor 1:
```bash
java -cp out smarthome.net.votacao.ClienteEleitor
# Login: eleitor1 / senha1
# Escolher candidato e votar
```

### Terminal 4 - Cliente Multicast:
```bash
java -cp out smarthome.net.votacao.ClienteMulticastNotas
# Recebe todas as notas informativas
```

---

## 📚 Conceitos Implementados

1. **Comunicação TCP**: Sockets TCP para operações de login, votação e gerenciamento
2. **Comunicação UDP Multicast**: Sockets UDP para distribuição de notas
3. **Multi-threading**: Servidor processa múltiplos clientes simultaneamente
4. **Serialização JSON**: Representação externa de dados em JSON
5. **Gerenciamento de Sessão**: Controle de usuários logados
6. **Controle de Prazo**: Timer para encerramento automático da votação

---

## ✅ Conclusão

O sistema implementa completamente:
- ✅ Sistema de votação distribuído
- ✅ Comunicação TCP multi-threaded
- ✅ Comunicação UDP multicast
- ✅ Representação externa via JSON
- ✅ Controle de prazo e resultados
- ✅ Gerenciamento de candidatos e notas informativas

O sistema está pronto para uso! 🎉
