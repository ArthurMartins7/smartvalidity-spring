package br.com.smartvalidity.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.smartvalidity.auth.AuthenticationService;
import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.AlertaDTO;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.service.NotificacaoService;

@RestController
@RequestMapping("/api/notificacoes")
@CrossOrigin(origins = "http://localhost:4200")
public class NotificacaoController {

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * Buscar todas as notificações do usuário atual
     */
    @GetMapping
    public ResponseEntity<List<AlertaDTO.Listagem>> buscarNotificacoes() {
        try {
            Usuario usuario = authenticationService.getUsuarioAutenticado();
            if (usuario == null) {
                return ResponseEntity.status(401).build();
            }
            
            List<AlertaDTO.Listagem> notificacoes = notificacaoService.buscarNotificacoesDoUsuario(usuario);
            return ResponseEntity.ok(notificacoes);
        } catch (SmartValidityException e) {
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * Buscar apenas notificações não lidas do usuário atual
     */
    @GetMapping("/nao-lidas")
    public ResponseEntity<List<AlertaDTO.Listagem>> buscarNotificacaoNaoLidas() {
        try {
            Usuario usuario = authenticationService.getUsuarioAutenticado();
            if (usuario == null) {
                return ResponseEntity.status(401).build();
            }
            
            List<AlertaDTO.Listagem> notificacoes = notificacaoService.buscarNotificacaoNaoLidasDoUsuario(usuario);
            return ResponseEntity.ok(notificacoes);
        } catch (SmartValidityException e) {
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * Contar notificações não lidas do usuário atual
     */
    @GetMapping("/count-nao-lidas")
    public ResponseEntity<Long> contarNotificacaoNaoLidas() {
        try {
            Usuario usuario = authenticationService.getUsuarioAutenticado();
            if (usuario == null) {
                return ResponseEntity.status(401).build();
            }
            
            Long count = notificacaoService.contarNotificacaoNaoLidasDoUsuario(usuario);
            return ResponseEntity.ok(count);
        } catch (SmartValidityException e) {
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * Marcar uma notificação como lida
     */
    @PutMapping("/{id}/marcar-lida")
    public ResponseEntity<Void> marcarNotificacaoComoLida(@PathVariable Long id) {
        try {
            Usuario usuario = authenticationService.getUsuarioAutenticado();
            if (usuario == null) {
                return ResponseEntity.status(401).build();
            }
            
            boolean sucesso = notificacaoService.marcarComoLida(id, usuario);
            
            if (sucesso) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (SmartValidityException e) {
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * Marcar todas as notificações do usuário como lidas
     */
    @PutMapping("/marcar-todas-lidas")
    public ResponseEntity<Void> marcarTodasNotificacoesComoLidas() {
        try {
            Usuario usuario = authenticationService.getUsuarioAutenticado();
            if (usuario == null) {
                return ResponseEntity.status(401).build();
            }
            
            notificacaoService.marcarTodasComoLidas(usuario);
            return ResponseEntity.ok().build();
        } catch (SmartValidityException e) {
            return ResponseEntity.status(401).build();
        }
    }
} 