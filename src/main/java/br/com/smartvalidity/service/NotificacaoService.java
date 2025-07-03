package br.com.smartvalidity.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.smartvalidity.auth.AuthenticationService;
import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.AlertaDTO;
import br.com.smartvalidity.model.dto.NotificacaoDTO;
import br.com.smartvalidity.model.entity.Alerta;
import br.com.smartvalidity.model.entity.Notificacao;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.mapper.AlertaMapper;
import br.com.smartvalidity.model.mapper.NotificacaoMapper;
import br.com.smartvalidity.model.repository.NotificacaoRepository;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class NotificacaoService {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private AuthenticationService authenticationService;


    @Transactional
    public void criarNotificacoesParaAlerta(Alerta alerta) {
        if (alerta.getUsuariosAlerta() == null || alerta.getUsuariosAlerta().isEmpty()) {
            log.warn("Alerta {} não possui usuários associados", alerta.getId());
            return;
        }

        int notificacoesCriadas = 0;
        for (Usuario usuario : alerta.getUsuariosAlerta()) {

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


    public List<AlertaDTO.Listagem> buscarNotificacoesDoUsuario(Usuario usuario) {
        try {
            List<Notificacao> notificacoes = notificacaoRepository.findByUsuarioOrderByDataHoraCriacaoDesc(usuario);
            return notificacoes.stream()
                    .map(this::convertNotificacaoToAlertaDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Erro ao buscar notificações do usuário {}: {}", usuario.getId(), e.getMessage());
            return List.of();
        }
    }


    public List<AlertaDTO.Listagem> buscarNotificacaoNaoLidasDoUsuario(Usuario usuario) {
        try {
            List<Notificacao> notificacoes = notificacaoRepository.findByUsuarioAndLidaFalseOrderByDataHoraCriacaoDesc(usuario);
            return notificacoes.stream()
                    .map(this::convertNotificacaoToAlertaDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Erro ao buscar notificações não lidas do usuário {}: {}", usuario.getId(), e.getMessage());
            return List.of();
        }
    }


    public AlertaDTO.Listagem buscarNotificacaoPorId(Long notificacaoId, Usuario usuario) {
        try {
            return notificacaoRepository.findByIdAndUsuario(notificacaoId, usuario)
                    .map(this::convertNotificacaoToAlertaDTO)
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Erro ao buscar notificação {} do usuário {}: {}", notificacaoId, usuario.getId(), e.getMessage());
            return null;
        }
    }


    public Long contarNotificacaoNaoLidasDoUsuario(Usuario usuario) {
        try {
            Long count = notificacaoRepository.countByUsuarioAndLidaFalse(usuario);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.warn("Erro ao contar notificações não lidas do usuário {}: {}", usuario.getId(), e.getMessage());
            return 0L;
        }
    }


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


    public List<NotificacaoDTO.Listagem> buscarNotificacoesComNotificacaoDTO(Usuario usuario) {
        try {
            List<Notificacao> notificacoes = notificacaoRepository.findByUsuarioOrderByDataHoraCriacaoDesc(usuario);
            return notificacoes.stream()
                    .map(NotificacaoMapper::toListagemDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Erro ao buscar notificações do usuário {}: {}", usuario.getId(), e.getMessage());
            return List.of();
        }
    }


    public List<NotificacaoDTO.Listagem> buscarNotificacaoNaoLidasComNotificacaoDTO(Usuario usuario) {
        try {
            List<Notificacao> notificacoes = notificacaoRepository.findByUsuarioAndLidaFalseOrderByDataHoraCriacaoDesc(usuario);
            return notificacoes.stream()
                    .map(NotificacaoMapper::toListagemDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Erro ao buscar notificações não lidas do usuário {}: {}", usuario.getId(), e.getMessage());
            return List.of();
        }
    }


    private AlertaDTO.Listagem convertNotificacaoToAlertaDTO(Notificacao notificacao) {
        if (notificacao == null || notificacao.getAlerta() == null) {
            return null;
        }
        
        AlertaDTO.Listagem dto = AlertaMapper.toListagemDTO(notificacao.getAlerta());
        
        // Usar ID da notificação, não do alerta
        dto.setId(notificacao.getId().intValue());
        dto.setDataCriacao(notificacao.getDataHoraCriacao());
        dto.setLida(notificacao.getLida());
        
        return dto;
    }
    

    public NotificacaoDTO.Listagem buscarNotificacaoPorIdComNotificacaoDTO(Long notificacaoId, Usuario usuario) {
        try {
            return notificacaoRepository.findByIdAndUsuario(notificacaoId, usuario)
                    .map(NotificacaoMapper::toListagemDTO)
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Erro ao buscar notificação {} do usuário {}: {}", notificacaoId, usuario.getId(), e.getMessage());
            return null;
        }
    }


    @Transactional
    public boolean excluirNotificacao(Long notificacaoId, Usuario usuario) {
        try {
            return notificacaoRepository.findByIdAndUsuario(notificacaoId, usuario)
                    .map(notificacao -> {
                        notificacaoRepository.delete(notificacao);
                        log.info("Notificação {} excluída pelo usuário {}", notificacaoId, usuario.getId());
                        return true;
                    })
                    .orElse(false);
        } catch (Exception e) {
            log.error("Erro ao excluir notificação {} do usuário {}: {}", 
                notificacaoId, usuario.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * MÉTODOS CONVENIENTES PARA ADERÊNCIA MVC
     * Encapsulam autenticação para evitar lógica de negócio no Controller
     */
    
    public List<AlertaDTO.Listagem> buscarNotificacoesDoUsuarioAutenticado() throws SmartValidityException {
        Usuario usuario = authenticationService.getUsuarioAutenticado();
        if (usuario == null) {
            throw new SmartValidityException("Usuário não autenticado");
        }
        return buscarNotificacoesDoUsuario(usuario);
    }
    
    public AlertaDTO.Listagem buscarNotificacaoPorIdDoUsuarioAutenticado(Long id) throws SmartValidityException {
        Usuario usuario = authenticationService.getUsuarioAutenticado();
        if (usuario == null) {
            throw new SmartValidityException("Usuário não autenticado");
        }
        return buscarNotificacaoPorId(id, usuario);
    }
    
    public List<AlertaDTO.Listagem> buscarNotificacaoNaoLidasDoUsuarioAutenticado() throws SmartValidityException {
        Usuario usuario = authenticationService.getUsuarioAutenticado();
        if (usuario == null) {
            throw new SmartValidityException("Usuário não autenticado");
        }
        return buscarNotificacaoNaoLidasDoUsuario(usuario);
    }
    
    public Long contarNotificacaoNaoLidasDoUsuarioAutenticado() throws SmartValidityException {
        Usuario usuario = authenticationService.getUsuarioAutenticado();
        if (usuario == null) {
            throw new SmartValidityException("Usuário não autenticado");
        }
        return contarNotificacaoNaoLidasDoUsuario(usuario);
    }
    
    public boolean marcarComoLidaDoUsuarioAutenticado(Long id) throws SmartValidityException {
        Usuario usuario = authenticationService.getUsuarioAutenticado();
        if (usuario == null) {
            throw new SmartValidityException("Usuário não autenticado");
        }
        return marcarComoLida(id, usuario);
    }
    
    public void marcarTodasComoLidasDoUsuarioAutenticado() throws SmartValidityException {
        Usuario usuario = authenticationService.getUsuarioAutenticado();
        if (usuario == null) {
            throw new SmartValidityException("Usuário não autenticado");
        }
        marcarTodasComoLidas(usuario);
    }
    
    public boolean excluirNotificacaoDoUsuarioAutenticado(Long id) throws SmartValidityException {
        Usuario usuario = authenticationService.getUsuarioAutenticado();
        if (usuario == null) {
            throw new SmartValidityException("Usuário não autenticado");
        }
        return excluirNotificacao(id, usuario);
    }
} 