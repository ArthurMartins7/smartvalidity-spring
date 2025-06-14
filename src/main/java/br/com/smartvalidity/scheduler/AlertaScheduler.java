package br.com.smartvalidity.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.smartvalidity.service.AlertaService;

@Component
public class AlertaScheduler {

    @Autowired
    private AlertaService alertaService;

    @Scheduled(cron = "0 0 8 * * *")
    public void gerarAlertasAutomaticos() {
        alertaService.gerarAlertasAutomaticos();
    }
} 