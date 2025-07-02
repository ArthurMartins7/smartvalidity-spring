package br.com.smartvalidity.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.smartvalidity.model.entity.Alerta;
import br.com.smartvalidity.model.repository.AlertaRepository;
import br.com.smartvalidity.service.NotificacaoService;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
public class DispatchAlertScheduler {

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void ativarAlertasProntosParaDisparo() {
        LocalDateTime agora = LocalDateTime.now();
        List<Alerta> pendentes = alertaRepository.findByAtivoFalseAndDataHoraDisparoLessThanEqualAndExcluidoFalse(agora);
        if (pendentes.isEmpty()) {
            return;
        }
        int ativados = 0;
        for (Alerta alerta : pendentes) {
            alerta.setAtivo(true);
            alertaRepository.save(alerta);
            notificacaoService.criarNotificacoesParaAlerta(alerta);
            
            ativados++;
        }
        log.info("{} alerta(s) ativados para notificação (horário de disparo alcançado)", ativados);
    }
} 