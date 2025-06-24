package br.com.smartvalidity.scheduler;

<<<<<<< HEAD
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
=======
import org.springframework.stereotype.Component;

@Component
public class AlertaScheduler {
}
>>>>>>> ac60f2e9298f0c29c567180cb212ef149affd74d
