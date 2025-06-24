package br.com.smartvalidity.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.smartvalidity.model.dto.AlertaDTO;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.service.NotificacaoService;

@RestController
@RequestMapping("/api/notificacoes")
@CrossOrigin(origins = "http://localhost:4200")
public class NotificacaoController {

    @Autowired
    private NotificacaoService notificacaoService;

    /**
     * Buscar todas as notificações do usuário atual
     */
    @GetMapping
    public ResponseEntity<List<AlertaDTO.Listagem>> buscarNotificacoes(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        Usuario usuario = (Usuario) authentication.getPrincipal();
        List<AlertaDTO.Listagem> notificacoes = notificacaoService.buscarNotificacoesDoUsuario(usuario);
        return ResponseEntity.ok(notificacoes);
    }

    /**
     * Buscar apenas notificações não lidas do usuário atual
     */
    @GetMapping("/nao-lidas")
    public ResponseEntity<List<AlertaDTO.Listagem>> buscarNotificacaoNaoLidas(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        Usuario usuario = (Usuario) authentication.getPrincipal();
        List<AlertaDTO.Listagem> notificacoes = notificacaoService.buscarNotificacaoNaoLidasDoUsuario(usuario);
        return ResponseEntity.ok(notificacoes);
    }

    /**
     * Contar notificações não lidas do usuário atual
     */
    @GetMapping("/count-nao-lidas")
    public ResponseEntity<Long> contarNotificacaoNaoLidas(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        Usuario usuario = (Usuario) authentication.getPrincipal();
        Long count = notificacaoService.contarNotificacaoNaoLidasDoUsuario(usuario);
        return ResponseEntity.ok(count);
    }

    /**
     * Marcar uma notificação como lida
     */
    @PutMapping("/{id}/marcar-lida")
    public ResponseEntity<Void> marcarNotificacaoComoLida(@PathVariable Integer id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        Usuario usuario = (Usuario) authentication.getPrincipal();
        boolean sucesso = notificacaoService.marcarComoLida(id, usuario);
        
        if (sucesso) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Marcar todas as notificações do usuário como lidas
     */
    @PutMapping("/marcar-todas-lidas")
    public ResponseEntity<Void> marcarTodasNotificacoesComoLidas(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        Usuario usuario = (Usuario) authentication.getPrincipal();
        notificacaoService.marcarTodasComoLidas(usuario);
        return ResponseEntity.ok().build();
    }
} 