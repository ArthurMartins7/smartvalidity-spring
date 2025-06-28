package br.com.smartvalidity.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.smartvalidity.model.dto.AlertaDTO;
import br.com.smartvalidity.model.entity.Alerta;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.mapper.AlertaMapper;
import br.com.smartvalidity.model.repository.AlertaRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificacaoService {

    @Autowired
    private AlertaRepository alertaRepository;

    /**
     * Buscar todas as notificações do usuário (alertas ativos)
     */
    public List<AlertaDTO.Listagem> buscarNotificacoesDoUsuario(Usuario usuario) {
        try {
            List<Alerta> alertas = alertaRepository.findByUsuarioAndAtivoTrue(usuario);
            if (alertas == null || alertas.isEmpty()) {
                return List.of(); // Retorna lista vazia sem erro
            }
            return alertas.stream()
                    .map(AlertaMapper::toListagemDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Aviso ao buscar notificações do usuário {}: {}", usuario.getId(), e.getMessage());
            return List.of(); // Retorna lista vazia em caso de erro
        }
    }

    /**
     * Buscar apenas notificações não lidas do usuário
     */
    public List<AlertaDTO.Listagem> buscarNotificacaoNaoLidasDoUsuario(Usuario usuario) {
        try {
            List<Alerta> alertas = alertaRepository.findByUsuarioAndAtivoTrueAndLidoFalse(usuario);
            if (alertas == null || alertas.isEmpty()) {
                return List.of(); // Retorna lista vazia sem erro
            }
            return alertas.stream()
                    .map(AlertaMapper::toListagemDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Aviso ao buscar notificações não lidas do usuário {}: {}", usuario.getId(), e.getMessage());
            return List.of(); // Retorna lista vazia em caso de erro
        }
    }

    /**
     * Contar notificações não lidas do usuário
     */
    public Long contarNotificacaoNaoLidasDoUsuario(Usuario usuario) {
        try {
            Long count = alertaRepository.countByUsuarioAndAtivoTrueAndLidoFalse(usuario);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.warn("Aviso ao contar notificações não lidas do usuário {}: {}", usuario.getId(), e.getMessage());
            return 0L; // Retorna 0 em caso de erro
        }
    }

    /**
     * Marcar uma notificação como lida
     */
    public boolean marcarComoLida(Integer alertaId, Usuario usuario) {
        try {
            return alertaRepository.findById(alertaId)
                    .filter(alerta -> alerta.getUsuariosAlerta().contains(usuario))
                    .map(alerta -> {
                        alerta.setLido(true);
                        alertaRepository.save(alerta);
                        log.info("Notificação {} marcada como lida pelo usuário {}", alertaId, usuario.getId());
                        return true;
                    })
                    .orElse(false);
        } catch (Exception e) {
            log.error("Erro ao marcar notificação {} como lida: {}", alertaId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Marcar todas as notificações do usuário como lidas
     */
    public void marcarTodasComoLidas(Usuario usuario) {
        try {
            List<Alerta> alertasNaoLidos = alertaRepository.findByUsuarioAndAtivoTrueAndLidoFalse(usuario);
            
            for (Alerta alerta : alertasNaoLidos) {
                alerta.setLido(true);
            }
            
            alertaRepository.saveAll(alertasNaoLidos);
            
            log.info("Todas as {} notificações do usuário {} foram marcadas como lidas", 
                alertasNaoLidos.size(), usuario.getId());
        } catch (Exception e) {
            log.error("Erro ao marcar todas as notificações como lidas para usuário {}: {}", 
                usuario.getId(), e.getMessage(), e);
        }
    }
} 