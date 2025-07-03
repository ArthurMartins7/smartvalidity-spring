package br.com.smartvalidity.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.AlertaDTO;
import br.com.smartvalidity.service.NotificacaoService;

@RestController
@RequestMapping("/api/notificacoes")
@CrossOrigin(origins = "http://localhost:4200")
public class NotificacaoController {

    @Autowired
    private NotificacaoService notificacaoService;

    @GetMapping
    public ResponseEntity<List<AlertaDTO.Listagem>> buscarNotificacoes() throws SmartValidityException {
        List<AlertaDTO.Listagem> notificacoes = notificacaoService.buscarNotificacoesDoUsuarioAutenticado();
        return ResponseEntity.ok(notificacoes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertaDTO.Listagem> buscarNotificacaoPorId(@PathVariable Long id) throws SmartValidityException {
        AlertaDTO.Listagem notificacao = notificacaoService.buscarNotificacaoPorIdDoUsuarioAutenticado(id);
        if (notificacao != null) {
            return ResponseEntity.ok(notificacao);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/nao-lidas")
    public ResponseEntity<List<AlertaDTO.Listagem>> buscarNotificacaoNaoLidas() throws SmartValidityException {
        List<AlertaDTO.Listagem> notificacoes = notificacaoService.buscarNotificacaoNaoLidasDoUsuarioAutenticado();
        return ResponseEntity.ok(notificacoes);
    }

    @GetMapping("/count-nao-lidas")
    public ResponseEntity<Long> contarNotificacaoNaoLidas() throws SmartValidityException {
        Long count = notificacaoService.contarNotificacaoNaoLidasDoUsuarioAutenticado();
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/marcar-lida")
    public ResponseEntity<Void> marcarNotificacaoComoLida(@PathVariable Long id) throws SmartValidityException {
        boolean sucesso = notificacaoService.marcarComoLidaDoUsuarioAutenticado(id);
        
        if (sucesso) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/marcar-todas-lidas")
    public ResponseEntity<Void> marcarTodasNotificacoesComoLidas() throws SmartValidityException {
        notificacaoService.marcarTodasComoLidasDoUsuarioAutenticado();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirNotificacao(@PathVariable Long id) {
        try {
            boolean sucesso = notificacaoService.excluirNotificacaoDoUsuarioAutenticado(id);
            if (sucesso) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (SmartValidityException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "error", "Exclusão negada",
                "message", e.getMessage()));
        }
    }
} 