package smarthome.services;

import smarthome.interfaces.ISmartHomeService;
import smarthome.pojos.DispositivoIoT;
import smarthome.pojos.Lampada;
import smarthome.pojos.Sensor;
import smarthome.pojos.Termostato;
import smarthome.pojos.Fechadura;
import smarthome.pojos.Camera;
import smarthome.pojos.Rotina;
import smarthome.pojos.Alerta;
import smarthome.pojos.Comodo;
import smarthome.pojos.Acao;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação do serviço Smart Home.
 * Gerencia dispositivos IoT, rotinas, alertas e cômodos.
 */
public class SmartHomeServiceImpl implements ISmartHomeService {
    
    private Map<String, DispositivoIoT> dispositivos;
    private Map<String, Rotina> rotinas;
    private List<Alerta> alertas;
    private Map<String, Comodo> comodos;
    
    public SmartHomeServiceImpl() {
        this.dispositivos = new HashMap<>();
        this.rotinas = new HashMap<>();
        this.alertas = new ArrayList<>();
        this.comodos = new HashMap<>();
        inicializarDados();
    }
    
    private void inicializarDados() {
        // Inicializa alguns dispositivos
        Lampada l1 = new Lampada("Luz Sala", "Sala", true, false, 80, 3000);
        Termostato t1 = new Termostato("Ar Condicionado", "Quarto", true, 24.0, 22.0);
        Sensor s1 = new Sensor("Sensor Movimento", "Corredor", true, "Movimento", false, 0.0);
        Fechadura f1 = new Fechadura("Fechadura Principal", "Entrada", true, false, 85, true);
        Camera c1 = new Camera("Câmera Segurança", "Entrada", true, false, 1080, true);
        
        dispositivos.put(l1.getId(), l1);
        dispositivos.put(t1.getId(), t1);
        dispositivos.put(s1.getId(), s1);
        dispositivos.put(f1.getId(), f1);
        dispositivos.put(c1.getId(), c1);
        
        // Inicializa cômodos
        Comodo sala = new Comodo("Sala");
        sala.setDispositivos(new ArrayList<>());
        sala.adicionarDispositivo(l1);
        comodos.put("Sala", sala);
        
        Comodo quarto = new Comodo("Quarto");
        quarto.setDispositivos(new ArrayList<>());
        quarto.adicionarDispositivo(t1);
        comodos.put("Quarto", quarto);
        
        // Inicializa algumas rotinas
        List<Acao> acoesRotina1 = new ArrayList<>();
        acoesRotina1.add(new Acao(l1.getId(), "ligar", new HashMap<>()));
        Rotina rotina1 = new Rotina("Acordar", acoesRotina1, LocalDateTime.now().plusHours(1));
        rotinas.put(rotina1.getId(), rotina1);
        
        // Inicializa alguns alertas
        alertas.add(new Alerta("Temperatura Alta", "Temperatura acima de 30°C", "Quarto"));
    }
    
    @Override
    public List<DispositivoIoT> listarDispositivos() {
        return new ArrayList<>(dispositivos.values());
    }
    
    @Override
    public DispositivoIoT obterDispositivo(String dispositivoId) {
        return dispositivos.get(dispositivoId);
    }
    
    @Override
    public DispositivoIoT atualizarDispositivo(String dispositivoId, DispositivoIoT dispositivo) {
        if (dispositivoId == null || dispositivo == null) {
            return null;
        }
        
        DispositivoIoT existente = dispositivos.get(dispositivoId);
        if (existente == null) {
            return null;
        }
        
        // Atualiza campos (passagem por valor - objeto local)
        existente.setNome(dispositivo.getNome());
        existente.setDescricao(dispositivo.getDescricao());
        existente.setComodo(dispositivo.getComodo());
        existente.setOnline(dispositivo.getOnline());
        
        dispositivos.put(dispositivoId, existente);
        return existente;
    }
    
    @Override
    public DispositivoIoT executarAcao(String dispositivoId, String comando) {
        DispositivoIoT dispositivo = dispositivos.get(dispositivoId);
        if (dispositivo == null) {
            return null;
        }
        
        // Simula execução de ação baseada no comando
        if ("ligar".equals(comando)) {
            dispositivo.setOnline(true);
            if (dispositivo instanceof Lampada) {
                ((Lampada) dispositivo).setLigada(true);
            }
        } else if ("desligar".equals(comando)) {
            dispositivo.setOnline(false);
            if (dispositivo instanceof Lampada) {
                ((Lampada) dispositivo).setLigada(false);
            }
        }
        
        dispositivos.put(dispositivoId, dispositivo);
        return dispositivo;
    }
    
    @Override
    public List<Rotina> listarRotinas() {
        return new ArrayList<>(rotinas.values());
    }
    
    @Override
    public Rotina criarRotina(Rotina rotina) {
        if (rotina == null) {
            return null;
        }
        
        rotinas.put(rotina.getId(), rotina);
        return rotina;
    }
    
    @Override
    public List<Alerta> listarAlertas() {
        return new ArrayList<>(alertas);
    }
    
    @Override
    public Comodo obterComodo(String nomeComodo) {
        return comodos.get(nomeComodo);
    }
}

