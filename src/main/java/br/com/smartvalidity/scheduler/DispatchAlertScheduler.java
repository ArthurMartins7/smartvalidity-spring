package br.com.smartvalidity.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.smartvalidity.model.entity.Alerta;
import br.com.smartvalidity.model.repository.AlertaRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler responsável por ativar os alertas personalizados (ou quaisquer
 * outros) no horário configurado de disparo. Quando o horário é alcançado,
 * o alerta deixa de ficar "inativo" e passa a "ativo", tornando-se elegível
 * para aparecer como notificação aos usuários relacionados.
 */
@Component
@Slf4j
public class DispatchAlertScheduler {

    @Autowired
    private AlertaRepository alertaRepository;

    /**
     * Executa a cada minuto e ativa alertas cujo dataHoraDisparo já passou.
     */
    @Scheduled(fixedRate = 60000) // 1 minuto
    @Transactional
    public void ativarAlertasProntosParaDisparo() {
        LocalDateTime agora = LocalDateTime.now();
        List<Alerta> pendentes = alertaRepository.findByAtivoFalseAndDataHoraDisparoLessThanEqual(agora);
        if (pendentes.isEmpty()) {
            return;
        }
        int ativados = 0;
        for (Alerta alerta : pendentes) {
            alerta.setAtivo(true);
            alerta.setLido(false); // garante que aparecerá como não lido
            alertaRepository.save(alerta);
            ativados++;
        }
        log.info("{} alerta(s) ativados para notificação (horário de disparo alcançado)", ativados);
    }
} 