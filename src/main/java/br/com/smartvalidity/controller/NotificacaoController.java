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

    @GetMapping("/personalizadas")
    public ResponseEntity<List<AlertaDTO.Listagem>> buscarNotificacoesPersonalizadas() throws SmartValidityException {
        List<AlertaDTO.Listagem> notificacoes = notificacaoService.buscarNotificacoesPersonalizadasDoUsuarioAutenticado();
        return ResponseEntity.ok(notificacoes);
    }

    @GetMapping("/count-pendentes")
    public ResponseEntity<Long> contarNotificacoesPendentes() throws SmartValidityException {
        Long count = notificacaoService.contarNotificacoesPendentesDoUsuarioAutenticado();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count-nao-lidas")
    public ResponseEntity<Long> contarNotificacoesNaoLidas() throws SmartValidityException {
        Long count = notificacaoService.contarNotificacoesNaoLidasTotalDoUsuarioAutenticado();
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/marcar-como-lida")
    public ResponseEntity<?> marcarComoLida(@PathVariable Long id) {
        try {
            boolean sucesso = notificacaoService.marcarComoLidaDoUsuarioAutenticado(id);
            if (sucesso) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (SmartValidityException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "error", "Erro ao marcar como lida",
                "message", e.getMessage()));
        }
    }

    @GetMapping("/debug/status")
    public ResponseEntity<java.util.Map<String, Object>> debugStatus() throws SmartValidityException {
        try {
            List<AlertaDTO.Listagem> todasNotificacoes = notificacaoService.buscarNotificacoesDoUsuarioAutenticado();
            List<AlertaDTO.Listagem> pendentes = notificacaoService.buscarNotificacoesPendentesDoUsuarioAutenticado();
            List<AlertaDTO.Listagem> resolvidas = notificacaoService.buscarNotificacoesProdutosInspecionadosDoUsuarioAutenticado();
            Long countPendentes = notificacaoService.contarNotificacoesPendentesDoUsuarioAutenticado();
            
            java.util.Map<String, Object> debug = new java.util.HashMap<>();
            debug.put("totalNotificacoes", todasNotificacoes.size());
            debug.put("notificacoesPendentes", pendentes.size());
            debug.put("notificacoesResolvidas", resolvidas.size());
            debug.put("countPendentesMetodo", countPendentes);
            debug.put("ultimasNotificacoes", todasNotificacoes.stream()
                .limit(5)
                .map(n -> java.util.Map.of(
                    "id", n.getId(),
                    "titulo", n.getTitulo(),
                    "tipo", n.getTipo(),
                    "dataCriacao", n.getDataCriacao(),
                    "itemInspecionado", n.getItemInspecionado(),
                    "lida", n.getLida()
                ))
                .collect(java.util.stream.Collectors.toList())
            );
            
            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            java.util.Map<String, Object> error = java.util.Map.of(
                "error", e.getMessage(),
                "stackTrace", java.util.Arrays.toString(e.getStackTrace())
            );
            return ResponseEntity.status(500).body(error);
        }
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