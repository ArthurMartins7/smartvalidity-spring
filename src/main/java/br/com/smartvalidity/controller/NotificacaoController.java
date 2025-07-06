package br.com.smartvalidity.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/pendentes")
    public ResponseEntity<List<AlertaDTO.Listagem>> buscarNotificacoesPendentes() throws SmartValidityException {
        List<AlertaDTO.Listagem> notificacoes = notificacaoService.buscarNotificacoesPendentesDoUsuarioAutenticado();
        return ResponseEntity.ok(notificacoes);
    }

    @GetMapping("/ja-resolvidas")
    public ResponseEntity<List<AlertaDTO.Listagem>> buscarNotificacoesJaResolvidas() throws SmartValidityException {
        List<AlertaDTO.Listagem> notificacoes = notificacaoService.buscarNotificacoesProdutosInspecionadosDoUsuarioAutenticado();
        return ResponseEntity.ok(notificacoes);
    }

    @GetMapping("/count-pendentes")
    public ResponseEntity<Long> contarNotificacoesPendentes() throws SmartValidityException {
        Long count = notificacaoService.contarNotificacoesPendentesDoUsuarioAutenticado();
        return ResponseEntity.ok(count);
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