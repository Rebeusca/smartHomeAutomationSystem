#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Cliente Remoto Smart Home - Python
Comunica-se com o servidor Java via API REST HTTP usando JSON.
"""

import json
import urllib.request
import urllib.parse
from typing import List, Dict, Optional, Any


class ClienteRemotoAPI:
    """Cliente remoto que se comunica com o servidor usando API REST HTTP."""
    
    def __init__(self, host: str = "localhost", porta: int = 8080):
        """
        Inicializa o cliente.
        
        Args:
            host: Endereço do servidor
            porta: Porta do servidor
        """
        self.base_url = f"http://{host}:{porta}"
    
    def _fazer_requisicao(self, endpoint: str, method: str = "GET", 
                         dados: Optional[Any] = None) -> Any:
        """
        Executa uma requisição HTTP e retorna a resposta.
        
        Args:
            endpoint: Endpoint da API
            method: Método HTTP (GET, POST)
            dados: Dados para enviar no corpo da requisição
            
        Returns:
            Resposta deserializada
        """
        url = f"{self.base_url}{endpoint}"
        
        # Prepara a requisição
        if dados is not None:
            # Serializa dados para JSON
            json_data = json.dumps(dados, default=str).encode('utf-8')
            req = urllib.request.Request(url, data=json_data, method=method)
            req.add_header('Content-Type', 'application/json')
            req.add_header('Accept', 'application/json')
        else:
            req = urllib.request.Request(url, method=method)
            req.add_header('Accept', 'application/json')
        
        try:
            print(f"[CLIENTE] {method} {endpoint}")
            with urllib.request.urlopen(req) as response:
                # Verifica Content-Type da resposta
                content_type = response.headers.get('Content-Type', '')
                
                # Lê a resposta
                response_data = response.read()
                
                # Se for JSON, deserializa
                if 'application/json' in content_type:
                    try:
                        return json.loads(response_data.decode('utf-8'))
                    except json.JSONDecodeError as e:
                        raise Exception(f"Erro ao decodificar JSON: {e}")
                else:
                    # Se não for JSON, pode ser erro ou dados binários
                    # Tenta decodificar como UTF-8 (pode ser texto de erro)
                    try:
                        texto = response_data.decode('utf-8')
                        # Se conseguir decodificar, pode ser mensagem de erro
                        raise Exception(f"Servidor retornou texto em vez de JSON: {texto}")
                    except UnicodeDecodeError:
                        # Se não conseguir decodificar, são dados binários
                        raise Exception("Servidor retornou dados binarios (serializacao Java) em vez de JSON. " +
                                      "Certifique-se de que o servidor esta configurado para retornar JSON quando " +
                                      "o header Accept: application/json e enviado.")
        except urllib.error.HTTPError as e:
            try:
                error_msg = e.read().decode('utf-8')
            except:
                error_msg = f"Erro HTTP {e.code}"
            raise Exception(f"Erro HTTP {e.code}: {error_msg}")
    
    def listar_dispositivos(self) -> List[Dict]:
        """Lista todos os dispositivos IoT."""
        return self._fazer_requisicao("/api/dispositivos", "GET")
    
    def obter_dispositivo(self, dispositivo_id: str) -> Optional[Dict]:
        """Obtém um dispositivo por ID."""
        return self._fazer_requisicao("/api/dispositivos/obter", "POST", dispositivo_id)
    
    def atualizar_dispositivo(self, dispositivo_id: str, dispositivo: Dict) -> Optional[Dict]:
        """Atualiza um dispositivo."""
        dados = [dispositivo_id, dispositivo]
        return self._fazer_requisicao("/api/dispositivos/atualizar", "POST", dados)
    
    def executar_acao(self, dispositivo_id: str, comando: str) -> Optional[Dict]:
        """Executa uma ação em um dispositivo."""
        dados = [dispositivo_id, comando]
        return self._fazer_requisicao("/api/dispositivos/acao", "POST", dados)
    
    def listar_rotinas(self) -> List[Dict]:
        """Lista todas as rotinas."""
        return self._fazer_requisicao("/api/rotinas", "GET")
    
    def criar_rotina(self, rotina: Dict) -> Optional[Dict]:
        """Cria uma nova rotina."""
        return self._fazer_requisicao("/api/rotinas/criar", "POST", rotina)
    
    def listar_alertas(self) -> List[Dict]:
        """Lista todos os alertas."""
        return self._fazer_requisicao("/api/alertas", "GET")
    
    def obter_comodo(self, nome_comodo: str) -> Optional[Dict]:
        """Obtém um cômodo por nome."""
        return self._fazer_requisicao("/api/comodos/obter", "POST", nome_comodo)


def exibir_menu():
    """Exibe o menu principal."""
    print("\n" + "=" * 40)
    print("MENU PRINCIPAL")
    print("=" * 40)
    print("1. Listar dispositivos")
    print("2. Obter dispositivo por ID")
    print("3. Atualizar dispositivo")
    print("4. Executar acao em dispositivo")
    print("5. Listar rotinas")
    print("6. Criar rotina")
    print("7. Listar alertas")
    print("8. Obter comodo por nome")
    print("0. Sair")
    print("=" * 40)


def listar_dispositivos_interativo(cliente):
    """Lista todos os dispositivos."""
    print("\n--- Listando Dispositivos ---")
    try:
        dispositivos = cliente.listar_dispositivos()
        print(f"Total: {len(dispositivos)} dispositivos\n")
        
        if not dispositivos:
            print("Nenhum dispositivo encontrado.")
        else:
            for i, d in enumerate(dispositivos, 1):
                print(f"{i}. {d.get('nome', 'N/A')}")
                print(f"   ID: {d.get('id', 'N/A')}")
                print(f"   Tipo: {d.get('tipo', 'N/A')}")
                print(f"   Online: {'Sim' if d.get('online') else 'Nao'}")
                print(f"   Comodo: {d.get('comodo', 'N/A')}")
                print()
    except Exception as e:
        print(f"[ERRO] {e}")


def obter_dispositivo_interativo(cliente):
    """Obtém um dispositivo por ID."""
    print("\n--- Obter Dispositivo ---")
    dispositivo_id = input("Digite o ID do dispositivo: ").strip()
    
    if not dispositivo_id:
        print("[ERRO] ID nao pode ser vazio!")
        return
    
    try:
        dispositivo = cliente.obter_dispositivo(dispositivo_id)
        if dispositivo:
            print("\nDispositivo encontrado:")
            print(f"  Nome: {dispositivo.get('nome', 'N/A')}")
            print(f"  ID: {dispositivo.get('id', 'N/A')}")
            print(f"  Tipo: {dispositivo.get('tipo', 'N/A')}")
            print(f"  Online: {'Sim' if dispositivo.get('online') else 'Nao'}")
            print(f"  Comodo: {dispositivo.get('comodo', 'N/A')}")
        else:
            print("\n[ERRO] Dispositivo nao encontrado!")
    except Exception as e:
        print(f"[ERRO] {e}")


def atualizar_dispositivo_interativo(cliente):
    """Atualiza um dispositivo."""
    print("\n--- Atualizar Dispositivo ---")
    dispositivo_id = input("Digite o ID do dispositivo: ").strip()
    
    if not dispositivo_id:
        print("[ERRO] ID nao pode ser vazio!")
        return
    
    try:
        dispositivo = cliente.obter_dispositivo(dispositivo_id)
        if not dispositivo:
            print("[ERRO] Dispositivo nao encontrado!")
            return
        
        print(f"\nDispositivo atual:")
        print(f"  Nome: {dispositivo.get('nome', 'N/A')}")
        print(f"  Online: {'Sim' if dispositivo.get('online') else 'Nao'}")
        
        novo_nome = input("\nNovo nome (Enter para manter): ").strip()
        if novo_nome:
            dispositivo['nome'] = novo_nome
        
        online_str = input("Online? (s/n, Enter para manter): ").strip().lower()
        if online_str == 's':
            dispositivo['online'] = True
        elif online_str == 'n':
            dispositivo['online'] = False
        
        atualizado = cliente.atualizar_dispositivo(dispositivo_id, dispositivo)
        if atualizado:
            print("\n[SUCESSO] Dispositivo atualizado!")
            print(f"  Nome: {atualizado.get('nome', 'N/A')}")
            print(f"  Online: {'Sim' if atualizado.get('online') else 'Nao'}")
        else:
            print("\n[ERRO] Falha ao atualizar dispositivo!")
    except Exception as e:
        print(f"[ERRO] {e}")


def executar_acao_interativo(cliente):
    """Executa uma ação em um dispositivo."""
    print("\n--- Executar Acao ---")
    dispositivo_id = input("Digite o ID do dispositivo: ").strip()
    
    if not dispositivo_id:
        print("[ERRO] ID nao pode ser vazio!")
        return
    
    comando = input("Digite o comando (ligar/desligar): ").strip()
    
    if not comando:
        print("[ERRO] Comando nao pode ser vazio!")
        return
    
    try:
        resultado = cliente.executar_acao(dispositivo_id, comando)
        if resultado:
            print("\n[SUCESSO] Acao executada!")
            print(f"  Dispositivo: {resultado.get('nome', 'N/A')}")
            print(f"  Online: {'Sim' if resultado.get('online') else 'Nao'}")
        else:
            print("\n[ERRO] Falha ao executar acao!")
    except Exception as e:
        print(f"[ERRO] {e}")


def listar_rotinas_interativo(cliente):
    """Lista todas as rotinas."""
    print("\n--- Listando Rotinas ---")
    try:
        rotinas = cliente.listar_rotinas()
        print(f"Total: {len(rotinas)} rotinas\n")
        
        if not rotinas:
            print("Nenhuma rotina encontrada.")
        else:
            for i, r in enumerate(rotinas, 1):
                print(f"{i}. {r.get('nome', 'N/A')}")
                print(f"   ID: {r.get('id', 'N/A')}")
                print(f"   Acoes: {len(r.get('acoes', []))}")
                print()
    except Exception as e:
        print(f"[ERRO] {e}")


def criar_rotina_interativo(cliente):
    """Cria uma nova rotina."""
    print("\n--- Criar Rotina ---")
    nome = input("Nome da rotina: ").strip()
    
    if not nome:
        print("[ERRO] Nome nao pode ser vazio!")
        return
    
    try:
        dispositivos = cliente.listar_dispositivos()
        if not dispositivos:
            print("[ERRO] Nenhum dispositivo disponivel!")
            return
        
        print("\nDispositivos disponiveis:")
        for i, d in enumerate(dispositivos, 1):
            print(f"{i}. {d.get('nome', 'N/A')} ({d.get('id', 'N/A')})")
        
        escolha = input("\nEscolha o numero do dispositivo: ").strip()
        try:
            idx = int(escolha) - 1
            if idx < 0 or idx >= len(dispositivos):
                print("[ERRO] Escolha invalida!")
                return
            dispositivo_id = dispositivos[idx].get('id')
        except ValueError:
            print("[ERRO] Numero invalido!")
            return
        
        comando = input("Comando (ligar/desligar): ").strip()
        
        rotina = {
            'nome': nome,
            'acoes': [{
                'dispositivoId': dispositivo_id,
                'comando': comando,
                'parametros': {}
            }]
        }
        
        criada = cliente.criar_rotina(rotina)
        if criada:
            print("\n[SUCESSO] Rotina criada!")
            print(f"  Nome: {criada.get('nome', 'N/A')}")
            print(f"  ID: {criada.get('id', 'N/A')}")
        else:
            print("\n[ERRO] Falha ao criar rotina!")
    except Exception as e:
        print(f"[ERRO] {e}")


def listar_alertas_interativo(cliente):
    """Lista todos os alertas."""
    print("\n--- Listando Alertas ---")
    try:
        alertas = cliente.listar_alertas()
        print(f"Total: {len(alertas)} alertas\n")
        
        if not alertas:
            print("Nenhum alerta encontrado.")
        else:
            for i, a in enumerate(alertas, 1):
                print(f"{i}. {a.get('titulo', 'N/A')}")
                print(f"   Mensagem: {a.get('mensagem', 'N/A')}")
                print(f"   Comodo: {a.get('comodo', 'N/A')}")
                print()
    except Exception as e:
        print(f"[ERRO] {e}")


def obter_comodo_interativo(cliente):
    """Obtém um cômodo por nome."""
    print("\n--- Obter Comodo ---")
    nome = input("Digite o nome do comodo: ").strip()
    
    if not nome:
        print("[ERRO] Nome nao pode ser vazio!")
        return
    
    try:
        comodo = cliente.obter_comodo(nome)
        if comodo:
            print("\nComodo encontrado:")
            print(f"  Nome: {comodo.get('nome', 'N/A')}")
            dispositivos = comodo.get('dispositivos', [])
            if dispositivos:
                print(f"  Dispositivos: {len(dispositivos)}")
                for d in dispositivos:
                    print(f"    - {d.get('nome', 'N/A')}")
        else:
            print("\n[ERRO] Comodo nao encontrado!")
    except Exception as e:
        print(f"[ERRO] {e}")


def main():
    """Função principal com menu interativo."""
    import sys
    
    host = "localhost"
    porta = 8080
    
    # Permite passar host e porta como argumentos
    if len(sys.argv) >= 2:
        host = sys.argv[1]
    if len(sys.argv) >= 3:
        porta = int(sys.argv[2])
    
    try:
        cliente = ClienteRemotoAPI(host, porta)
        
        print("=" * 40)
        print("Cliente Remoto Smart Home (Python)")
        print("=" * 40)
        print(f"Conectado a: {host}:{porta}")
        
        continuar = True
        
        while continuar:
            exibir_menu()
            opcao = input("Escolha uma opcao: ").strip()
            
            if opcao == "1":
                listar_dispositivos_interativo(cliente)
            elif opcao == "2":
                obter_dispositivo_interativo(cliente)
            elif opcao == "3":
                atualizar_dispositivo_interativo(cliente)
            elif opcao == "4":
                executar_acao_interativo(cliente)
            elif opcao == "5":
                listar_rotinas_interativo(cliente)
            elif opcao == "6":
                criar_rotina_interativo(cliente)
            elif opcao == "7":
                listar_alertas_interativo(cliente)
            elif opcao == "8":
                obter_comodo_interativo(cliente)
            elif opcao == "0":
                continuar = False
                print("\nEncerrando cliente...")
            else:
                print("\n[ERRO] Opcao invalida! Tente novamente.")
            
            if continuar:
                input("\nPressione Enter para continuar...")
                
    except Exception as e:
        print(f"[ERRO] {e}")
        import traceback
        traceback.print_exc()


if __name__ == "__main__":
    main()
