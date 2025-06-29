package br.com.smartvalidity.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.smartvalidity.model.dto.AlertaDTO;
import br.com.smartvalidity.model.entity.Alerta;
import br.com.smartvalidity.model.entity.Notificacao;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.mapper.AlertaMapper;
import br.com.smartvalidity.model.repository.NotificacaoRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificacaoService {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    /**
     * Criar notificações individuais para todos os usuários de um alerta
     */
    @Transactional
    public void criarNotificacoesParaAlerta(Alerta alerta) {
        if (alerta.getUsuariosAlerta() == null || alerta.getUsuariosAlerta().isEmpty()) {
            log.warn("Alerta {} não possui usuários associados", alerta.getId());
            return;
        }

        int notificacoesCriadas = 0;
        for (Usuario usuario : alerta.getUsuariosAlerta()) {
            // Verificar se já existe notificação para evitar duplicatas
            if (!notificacaoRepository.existsByAlertaIdAndUsuarioId(alerta.getId(), usuario.getId())) {
                Notificacao notificacao = new Notificacao();
                notificacao.setAlerta(alerta);
                notificacao.setUsuario(usuario);
                notificacao.setLida(false);
                
                notificacaoRepository.save(notificacao);
                notificacoesCriadas++;
            }
        }
        
        log.info("Criadas {} notificações para o alerta {}", notificacoesCriadas, alerta.getId());
    }

    /**
     * Buscar todas as notificações do usuário
     */
    public List<AlertaDTO.Listagem> buscarNotificacoesDoUsuario(Usuario usuario) {
        try {
            List<Notificacao> notificacoes = notificacaoRepository.findByUsuarioOrderByDataHoraCriacaoDesc(usuario);
            return notificacoes.stream()
                    .map(notificacao -> {
                        AlertaDTO.Listagem dto = AlertaMapper.toListagemDTO(notificacao.getAlerta());
                        // Adicionar informações específicas da notificação
                        dto.setLida(notificacao.getLida());
                        dto.setDataCriacao(notificacao.getDataHoraCriacao());
                        return dto;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Erro ao buscar notificações do usuário {}: {}", usuario.getId(), e.getMessage());
            return List.of();
        }
    }

    /**
     * Buscar apenas notificações não lidas do usuário
     */
    public List<AlertaDTO.Listagem> buscarNotificacaoNaoLidasDoUsuario(Usuario usuario) {
        try {
            List<Notificacao> notificacoes = notificacaoRepository.findByUsuarioAndLidaFalseOrderByDataHoraCriacaoDesc(usuario);
            return notificacoes.stream()
                    .map(notificacao -> {
                        AlertaDTO.Listagem dto = AlertaMapper.toListagemDTO(notificacao.getAlerta());
                        dto.setLida(false); // Sempre false nesta consulta
                        dto.setDataCriacao(notificacao.getDataHoraCriacao());
                        return dto;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Erro ao buscar notificações não lidas do usuário {}: {}", usuario.getId(), e.getMessage());
            return List.of();
        }
    }

    /**
     * Contar notificações não lidas do usuário
     */
    public Long contarNotificacaoNaoLidasDoUsuario(Usuario usuario) {
        try {
            Long count = notificacaoRepository.countByUsuarioAndLidaFalse(usuario);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.warn("Erro ao contar notificações não lidas do usuário {}: {}", usuario.getId(), e.getMessage());
            return 0L;
        }
    }

    /**
     * Marcar uma notificação como lida
     */
    @Transactional
    public boolean marcarComoLida(Long notificacaoId, Usuario usuario) {
        try {
            return notificacaoRepository.findByIdAndUsuario(notificacaoId, usuario)
                    .map(notificacao -> {
                        if (!notificacao.getLida()) {
                            notificacao.marcarComoLida();
                            notificacaoRepository.save(notificacao);
                            log.info("Notificação {} marcada como lida pelo usuário {}", notificacaoId, usuario.getId());
                        }
                        return true;
                    })
                    .orElse(false);
        } catch (Exception e) {
            log.error("Erro ao marcar notificação {} como lida: {}", notificacaoId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Marcar todas as notificações do usuário como lidas
     */
    @Transactional
    public void marcarTodasComoLidas(Usuario usuario) {
        try {
            int atualizadas = notificacaoRepository.marcarTodasComoLidasPorUsuario(usuario);
            log.info("Marcadas {} notificações como lidas para o usuário {}", atualizadas, usuario.getId());
        } catch (Exception e) {
            log.error("Erro ao marcar todas as notificações como lidas para usuário {}: {}", 
                usuario.getId(), e.getMessage(), e);
        }
    }
} 