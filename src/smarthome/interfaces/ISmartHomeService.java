package smarthome.interfaces;

import smarthome.pojos.DispositivoIoT;
import smarthome.pojos.Rotina;
import smarthome.pojos.Alerta;
import smarthome.pojos.Comodo;

import java.io.Serializable;
import java.util.List;

/**
 * Interface remota para o serviço Smart Home.
 * Define os métodos que podem ser invocados remotamente.
 */
public interface ISmartHomeService extends Serializable {
    
    // Constantes para methodId
    int METHOD_LISTAR_DISPOSITIVOS = 1;
    int METHOD_OBTER_DISPOSITIVO = 2;
    int METHOD_ATUALIZAR_DISPOSITIVO = 3;
    int METHOD_EXECUTAR_ACAO = 4;
    int METHOD_LISTAR_ROTINAS = 5;
    int METHOD_CRIAR_ROTINA = 6;
    int METHOD_LISTAR_ALERTAS = 7;
    int METHOD_OBTER_COMODO = 8;
    
    /**
     * Lista todos os dispositivos IoT.
     * @return Lista de dispositivos
     */
    List<DispositivoIoT> listarDispositivos();
    
    /**
     * Obtém um dispositivo por ID.
     * @param dispositivoId ID do dispositivo
     * @return Dispositivo encontrado ou null
     */
    DispositivoIoT obterDispositivo(String dispositivoId);
    
    /**
     * Atualiza um dispositivo.
     * @param dispositivoId ID do dispositivo
     * @param dispositivo Dispositivo atualizado (passagem por valor)
     * @return Dispositivo atualizado
     */
    DispositivoIoT atualizarDispositivo(String dispositivoId, DispositivoIoT dispositivo);
    
    /**
     * Executa uma ação em um dispositivo.
     * @param dispositivoId ID do dispositivo
     * @param comando Comando a ser executado (passagem por valor)
     * @return Dispositivo após execução
     */
    DispositivoIoT executarAcao(String dispositivoId, String comando);
    
    /**
     * Lista todas as rotinas.
     * @return Lista de rotinas
     */
    List<Rotina> listarRotinas();
    
    /**
     * Cria uma nova rotina.
     * @param rotina Rotina a ser criada (passagem por valor)
     * @return Rotina criada
     */
    Rotina criarRotina(Rotina rotina);
    
    /**
     * Lista todos os alertas.
     * @return Lista de alertas
     */
    List<Alerta> listarAlertas();
    
    /**
     * Obtém um cômodo por nome.
     * @param nomeComodo Nome do cômodo
     * @return Cômodo encontrado ou null
     */
    Comodo obterComodo(String nomeComodo);
}

