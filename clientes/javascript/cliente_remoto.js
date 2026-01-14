/**
 * Cliente Remoto Smart Home - JavaScript/Node.js
 * Comunica-se com o servidor Java via API REST HTTP usando JSON.
 */

const http = require('http');

class ClienteRemotoAPI {
    /**
     * Inicializa o cliente.
     * @param {string} host - Endereço do servidor
     * @param {number} porta - Porta do servidor
     */
    constructor(host = 'localhost', porta = 8080) {
        this.baseUrl = `http://${host}:${porta}`;
    }

    /**
     * Executa uma requisição HTTP e retorna a resposta.
     * @param {string} endpoint - Endpoint da API
     * @param {string} method - Método HTTP (GET, POST)
     * @param {any} dados - Dados para enviar no corpo da requisição
     * @returns {Promise<any>} Resposta deserializada
     */
    async _fazerRequisicao(endpoint, method = 'GET', dados = null) {
        return new Promise((resolve, reject) => {
            const url = new URL(this.baseUrl + endpoint);
            
            const options = {
                hostname: url.hostname,
                port: url.port,
                path: url.pathname,
                method: method,
                headers: {}
            };

            // Sempre solicita JSON
            options.headers['Accept'] = 'application/json';
            
            if (dados !== null) {
                const jsonData = JSON.stringify(dados);
                options.headers['Content-Type'] = 'application/json';
                options.headers['Content-Length'] = Buffer.byteLength(jsonData);
            }

            console.log(`[CLIENTE] ${method} ${endpoint}`);

            const req = http.request(options, (res) => {
                let responseData = '';

                res.on('data', (chunk) => {
                    responseData += chunk;
                });

                res.on('end', () => {
                    try {
                        // Tenta deserializar como JSON
                        const parsed = JSON.parse(responseData);
                        resolve(parsed);
                    } catch (e) {
                        // Se não for JSON, retorna como string
                        resolve(responseData);
                    }
                });
            });

            req.on('error', (error) => {
                reject(new Error(`Erro na requisição: ${error.message}`));
            });

            if (dados !== null) {
                req.write(JSON.stringify(dados));
            }

            req.end();
        });
    }

    /**
     * Lista todos os dispositivos IoT.
     * @returns {Promise<Array>} Lista de dispositivos
     */
    async listarDispositivos() {
        return this._fazerRequisicao('/api/dispositivos', 'GET');
    }

    /**
     * Obtém um dispositivo por ID.
     * @param {string} dispositivoId - ID do dispositivo
     * @returns {Promise<Object>} Dispositivo encontrado
     */
    async obterDispositivo(dispositivoId) {
        return this._fazerRequisicao('/api/dispositivos/obter', 'POST', dispositivoId);
    }

    /**
     * Atualiza um dispositivo.
     * @param {string} dispositivoId - ID do dispositivo
     * @param {Object} dispositivo - Dados do dispositivo
     * @returns {Promise<Object>} Dispositivo atualizado
     */
    async atualizarDispositivo(dispositivoId, dispositivo) {
        const dados = [dispositivoId, dispositivo];
        return this._fazerRequisicao('/api/dispositivos/atualizar', 'POST', dados);
    }

    /**
     * Executa uma ação em um dispositivo.
     * @param {string} dispositivoId - ID do dispositivo
     * @param {string} comando - Comando a executar
     * @returns {Promise<Object>} Dispositivo após execução
     */
    async executarAcao(dispositivoId, comando) {
        const dados = [dispositivoId, comando];
        return this._fazerRequisicao('/api/dispositivos/acao', 'POST', dados);
    }

    /**
     * Lista todas as rotinas.
     * @returns {Promise<Array>} Lista de rotinas
     */
    async listarRotinas() {
        return this._fazerRequisicao('/api/rotinas', 'GET');
    }

    /**
     * Cria uma nova rotina.
     * @param {Object} rotina - Dados da rotina
     * @returns {Promise<Object>} Rotina criada
     */
    async criarRotina(rotina) {
        return this._fazerRequisicao('/api/rotinas/criar', 'POST', rotina);
    }

    /**
     * Lista todos os alertas.
     * @returns {Promise<Array>} Lista de alertas
     */
    async listarAlertas() {
        return this._fazerRequisicao('/api/alertas', 'GET');
    }

    /**
     * Obtém um cômodo por nome.
     * @param {string} nomeComodo - Nome do cômodo
     * @returns {Promise<Object>} Cômodo encontrado
     */
    async obterComodo(nomeComodo) {
        return this._fazerRequisicao('/api/comodos/obter', 'POST', nomeComodo);
    }
}

const readline = require('readline');

/**
 * Cria interface de leitura do terminal.
 */
function criarInterface() {
    return readline.createInterface({
        input: process.stdin,
        output: process.stdout
    });
}

/**
 * Faz uma pergunta ao usuário.
 */
function perguntar(rl, pergunta) {
    return new Promise((resolve) => {
        rl.question(pergunta, (resposta) => {
            resolve(resposta);
        });
    });
}

/**
 * Exibe o menu principal.
 */
function exibirMenu() {
    console.log('\n' + '='.repeat(40));
    console.log('MENU PRINCIPAL');
    console.log('='.repeat(40));
    console.log('1. Listar dispositivos');
    console.log('2. Obter dispositivo por ID');
    console.log('3. Atualizar dispositivo');
    console.log('4. Executar acao em dispositivo');
    console.log('5. Listar rotinas');
    console.log('6. Criar rotina');
    console.log('7. Listar alertas');
    console.log('8. Obter comodo por nome');
    console.log('0. Sair');
    console.log('='.repeat(40));
}

/**
 * Lista todos os dispositivos.
 */
async function listarDispositivosInterativo(cliente) {
    console.log('\n--- Listando Dispositivos ---');
    try {
        const dispositivos = await cliente.listarDispositivos();
        console.log(`Total: ${dispositivos.length} dispositivos\n`);
        
        if (dispositivos.length === 0) {
            console.log('Nenhum dispositivo encontrado.');
        } else {
            dispositivos.forEach((d, i) => {
                console.log(`${i + 1}. ${d.nome || 'N/A'}`);
                console.log(`   ID: ${d.id || 'N/A'}`);
                console.log(`   Tipo: ${d.tipo || 'N/A'}`);
                console.log(`   Online: ${d.online ? 'Sim' : 'Nao'}`);
                console.log(`   Comodo: ${d.comodo || 'N/A'}`);
                console.log();
            });
        }
    } catch (error) {
        console.log(`[ERRO] ${error.message}`);
    }
}

/**
 * Obtém um dispositivo por ID.
 */
async function obterDispositivoInterativo(cliente, rl) {
    console.log('\n--- Obter Dispositivo ---');
    const dispositivoId = await perguntar(rl, 'Digite o ID do dispositivo: ');
    
    if (!dispositivoId.trim()) {
        console.log('[ERRO] ID nao pode ser vazio!');
        return;
    }
    
    try {
        const dispositivo = await cliente.obterDispositivo(dispositivoId.trim());
        if (dispositivo) {
            console.log('\nDispositivo encontrado:');
            console.log(`  Nome: ${dispositivo.nome || 'N/A'}`);
            console.log(`  ID: ${dispositivo.id || 'N/A'}`);
            console.log(`  Tipo: ${dispositivo.tipo || 'N/A'}`);
            console.log(`  Online: ${dispositivo.online ? 'Sim' : 'Nao'}`);
            console.log(`  Comodo: ${dispositivo.comodo || 'N/A'}`);
        } else {
            console.log('\n[ERRO] Dispositivo nao encontrado!');
        }
    } catch (error) {
        console.log(`[ERRO] ${error.message}`);
    }
}

/**
 * Atualiza um dispositivo.
 */
async function atualizarDispositivoInterativo(cliente, rl) {
    console.log('\n--- Atualizar Dispositivo ---');
    const dispositivoId = await perguntar(rl, 'Digite o ID do dispositivo: ');
    
    if (!dispositivoId.trim()) {
        console.log('[ERRO] ID nao pode ser vazio!');
        return;
    }
    
    try {
        const dispositivo = await cliente.obterDispositivo(dispositivoId.trim());
        if (!dispositivo) {
            console.log('[ERRO] Dispositivo nao encontrado!');
            return;
        }
        
        console.log('\nDispositivo atual:');
        console.log(`  Nome: ${dispositivo.nome || 'N/A'}`);
        console.log(`  Online: ${dispositivo.online ? 'Sim' : 'Nao'}`);
        
        const novoNome = await perguntar(rl, '\nNovo nome (Enter para manter): ');
        if (novoNome.trim()) {
            dispositivo.nome = novoNome.trim();
        }
        
        const onlineStr = await perguntar(rl, 'Online? (s/n, Enter para manter): ');
        if (onlineStr.trim().toLowerCase() === 's') {
            dispositivo.online = true;
        } else if (onlineStr.trim().toLowerCase() === 'n') {
            dispositivo.online = false;
        }
        
        const atualizado = await cliente.atualizarDispositivo(dispositivoId.trim(), dispositivo);
        if (atualizado) {
            console.log('\n[SUCESSO] Dispositivo atualizado!');
            console.log(`  Nome: ${atualizado.nome || 'N/A'}`);
            console.log(`  Online: ${atualizado.online ? 'Sim' : 'Nao'}`);
        } else {
            console.log('\n[ERRO] Falha ao atualizar dispositivo!');
        }
    } catch (error) {
        console.log(`[ERRO] ${error.message}`);
    }
}

/**
 * Executa uma ação em um dispositivo.
 */
async function executarAcaoInterativo(cliente, rl) {
    console.log('\n--- Executar Acao ---');
    const dispositivoId = await perguntar(rl, 'Digite o ID do dispositivo: ');
    
    if (!dispositivoId.trim()) {
        console.log('[ERRO] ID nao pode ser vazio!');
        return;
    }
    
    const comando = await perguntar(rl, 'Digite o comando (ligar/desligar): ');
    
    if (!comando.trim()) {
        console.log('[ERRO] Comando nao pode ser vazio!');
        return;
    }
    
    try {
        const resultado = await cliente.executarAcao(dispositivoId.trim(), comando.trim());
        if (resultado) {
            console.log('\n[SUCESSO] Acao executada!');
            console.log(`  Dispositivo: ${resultado.nome || 'N/A'}`);
            console.log(`  Online: ${resultado.online ? 'Sim' : 'Nao'}`);
        } else {
            console.log('\n[ERRO] Falha ao executar acao!');
        }
    } catch (error) {
        console.log(`[ERRO] ${error.message}`);
    }
}

/**
 * Lista todas as rotinas.
 */
async function listarRotinasInterativo(cliente) {
    console.log('\n--- Listando Rotinas ---');
    try {
        const rotinas = await cliente.listarRotinas();
        console.log(`Total: ${rotinas.length} rotinas\n`);
        
        if (rotinas.length === 0) {
            console.log('Nenhuma rotina encontrada.');
        } else {
            rotinas.forEach((r, i) => {
                console.log(`${i + 1}. ${r.nome || 'N/A'}`);
                console.log(`   ID: ${r.id || 'N/A'}`);
                console.log(`   Acoes: ${r.acoes ? r.acoes.length : 0}`);
                console.log();
            });
        }
    } catch (error) {
        console.log(`[ERRO] ${error.message}`);
    }
}

/**
 * Cria uma nova rotina.
 */
async function criarRotinaInterativo(cliente, rl) {
    console.log('\n--- Criar Rotina ---');
    const nome = await perguntar(rl, 'Nome da rotina: ');
    
    if (!nome.trim()) {
        console.log('[ERRO] Nome nao pode ser vazio!');
        return;
    }
    
    try {
        const dispositivos = await cliente.listarDispositivos();
        if (dispositivos.length === 0) {
            console.log('[ERRO] Nenhum dispositivo disponivel!');
            return;
        }
        
        console.log('\nDispositivos disponiveis:');
        dispositivos.forEach((d, i) => {
            console.log(`${i + 1}. ${d.nome || 'N/A'} (${d.id || 'N/A'})`);
        });
        
        const escolha = await perguntar(rl, '\nEscolha o numero do dispositivo: ');
        const idx = parseInt(escolha) - 1;
        
        if (isNaN(idx) || idx < 0 || idx >= dispositivos.length) {
            console.log('[ERRO] Escolha invalida!');
            return;
        }
        
        const comando = await perguntar(rl, 'Comando (ligar/desligar): ');
        
        const rotina = {
            nome: nome.trim(),
            acoes: [{
                dispositivoId: dispositivos[idx].id,
                comando: comando.trim(),
                parametros: {}
            }]
        };
        
        const criada = await cliente.criarRotina(rotina);
        if (criada) {
            console.log('\n[SUCESSO] Rotina criada!');
            console.log(`  Nome: ${criada.nome || 'N/A'}`);
            console.log(`  ID: ${criada.id || 'N/A'}`);
        } else {
            console.log('\n[ERRO] Falha ao criar rotina!');
        }
    } catch (error) {
        console.log(`[ERRO] ${error.message}`);
    }
}

/**
 * Lista todos os alertas.
 */
async function listarAlertasInterativo(cliente) {
    console.log('\n--- Listando Alertas ---');
    try {
        const alertas = await cliente.listarAlertas();
        console.log(`Total: ${alertas.length} alertas\n`);
        
        if (alertas.length === 0) {
            console.log('Nenhum alerta encontrado.');
        } else {
            alertas.forEach((a, i) => {
                console.log(`${i + 1}. ${a.titulo || 'N/A'}`);
                console.log(`   Mensagem: ${a.mensagem || 'N/A'}`);
                console.log(`   Comodo: ${a.comodo || 'N/A'}`);
                console.log();
            });
        }
    } catch (error) {
        console.log(`[ERRO] ${error.message}`);
    }
}

/**
 * Obtém um cômodo por nome.
 */
async function obterComodoInterativo(cliente, rl) {
    console.log('\n--- Obter Comodo ---');
    const nome = await perguntar(rl, 'Digite o nome do comodo: ');
    
    if (!nome.trim()) {
        console.log('[ERRO] Nome nao pode ser vazio!');
        return;
    }
    
    try {
        const comodo = await cliente.obterComodo(nome.trim());
        if (comodo) {
            console.log('\nComodo encontrado:');
            console.log(`  Nome: ${comodo.nome || 'N/A'}`);
            if (comodo.dispositivos) {
                console.log(`  Dispositivos: ${comodo.dispositivos.length}`);
                comodo.dispositivos.forEach(d => {
                    console.log(`    - ${d.nome || 'N/A'}`);
                });
            }
        } else {
            console.log('\n[ERRO] Comodo nao encontrado!');
        }
    } catch (error) {
        console.log(`[ERRO] ${error.message}`);
    }
}

/**
 * Função principal com menu interativo.
 */
async function main() {
    const host = process.argv[2] || 'localhost';
    const porta = parseInt(process.argv[3]) || 8080;
    
    try {
        const cliente = new ClienteRemotoAPI(host, porta);
        const rl = criarInterface();
        
        console.log('='.repeat(40));
        console.log('Cliente Remoto Smart Home (JavaScript/Node.js)');
        console.log('='.repeat(40));
        console.log(`Conectado a: ${host}:${porta}`);
        
        let continuar = true;
        
        while (continuar) {
            exibirMenu();
            const opcao = await perguntar(rl, 'Escolha uma opcao: ');
            
            try {
                switch (opcao.trim()) {
                    case '1':
                        await listarDispositivosInterativo(cliente);
                        break;
                    case '2':
                        await obterDispositivoInterativo(cliente, rl);
                        break;
                    case '3':
                        await atualizarDispositivoInterativo(cliente, rl);
                        break;
                    case '4':
                        await executarAcaoInterativo(cliente, rl);
                        break;
                    case '5':
                        await listarRotinasInterativo(cliente);
                        break;
                    case '6':
                        await criarRotinaInterativo(cliente, rl);
                        break;
                    case '7':
                        await listarAlertasInterativo(cliente);
                        break;
                    case '8':
                        await obterComodoInterativo(cliente, rl);
                        break;
                    case '0':
                        continuar = false;
                        console.log('\nEncerrando cliente...');
                        break;
                    default:
                        console.log('\n[ERRO] Opcao invalida! Tente novamente.');
                }
            } catch (error) {
                console.log(`\n[ERRO] ${error.message}`);
            }
            
            if (continuar) {
                await perguntar(rl, '\nPressione Enter para continuar...');
            }
        }
        
        rl.close();
        
    } catch (error) {
        console.error(`[ERRO] ${error.message}`);
        console.error(error.stack);
    }
}

// Executa se for chamado diretamente
if (require.main === module) {
    main();
}

module.exports = ClienteRemotoAPI;
