# Como Testar o Projeto

## 1. Compilar o Projeto

```bash
javac -d out -cp out src/smarthome\pojos\*.java src/smarthome\streams\*.java src/smarthome\testes\*.java
```

---

## 2. Executar os Testes

### Teste b: Entrada Padrão (System.in)

**Passo 1:** Gerar arquivo de dados
```bash
java -cp out smarthome.testes.GeradorDados
```

**Passo 2:** Executar teste

**No CMD (Prompt de Comando):**
```bash
java -cp out smarthome.testes.TesteSystemIn < teste.bin
```

---

### Teste c: Arquivo (FileInputStream)

**Passo 1:** Gerar arquivo de dados
```bash
java -cp out smarthome.testes.GeradorDados
```

**Passo 2:** Executar teste
```bash
java -cp out smarthome.testes.TesteArquivo
```

---

### Teste d: Servidor Remoto (TCP)

**Terminal 1 - Servidor:**
```bash
java -cp out smarthome.testes.ServidorTCP
```

**Terminal 2 - Cliente:**
```bash
java -cp out smarthome.testes.TesteTCP
```

---

## Resumo dos Arquivos

| Arquivo | Função |
|---------|--------|
| `GeradorDados.java` | Gera arquivo `teste.bin` com dados de teste |
| `TesteSystemIn.java` | Teste b: lê de System.in |
| `TesteArquivo.java` | Teste c: lê de arquivo |
| `ServidorTCP.java` | Servidor que envia dados via TCP |
| `TesteTCP.java` | Teste d: lê de servidor TCP |

---

## Observações

- O arquivo `teste.bin` será criado na raiz do projeto
- O servidor TCP usa a porta **12345**
- Para o teste TCP, você precisa de 2 terminais abertos

