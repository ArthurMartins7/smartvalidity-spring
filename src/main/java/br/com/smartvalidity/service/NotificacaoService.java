package br.com.smartvalidity.service;

import java.sql.Timestamp;
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
import br.com.smartvalidity.model.entity.ItemProduto;
import br.com.smartvalidity.model.entity.Notificacao;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.enums.TipoAlerta;
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
        log.info("=== Iniciando criação de notificações ===");
        log.info("Alerta ID: {}, Título: {}", alerta.getId(), alerta.getTitulo());
        
        if (alerta.getUsuariosAlerta() == null || alerta.getUsuariosAlerta().isEmpty()) {
            log.warn("Alerta {} não possui usuários associados", alerta.getId());
            return;
        }

        log.info("Usuários associados ao alerta: {}", alerta.getUsuariosAlerta().size());
        int notificacoesCriadas = 0;
        int notificacoesJaExistentes = 0;
        
        for (Usuario usuario : alerta.getUsuariosAlerta()) {
            log.info("Processando usuário: {} (ID: {})", usuario.getNome(), usuario.getId());
            
            boolean jaExiste = notificacaoRepository.existsByAlertaIdAndUsuarioId(alerta.getId(), usuario.getId());
            log.info("Notificação já existe para este usuário/alerta? {}", jaExiste);
            
            if (!jaExiste) {
                try {
                    Notificacao notificacao = new Notificacao();
                    notificacao.setAlerta(alerta);
                    notificacao.setUsuario(usuario);
                    notificacao.setLida(false);
                    
                    Notificacao notificacaoSalva = notificacaoRepository.save(notificacao);
                    log.info("Notificação criada com ID: {} para usuário {}", 
                        notificacaoSalva.getId(), usuario.getNome());
                    notificacoesCriadas++;
                } catch (Exception e) {
                    log.error("Erro ao criar notificação para usuário {}: {}", usuario.getNome(), e.getMessage(), e);
                }
            } else {
                notificacoesJaExistentes++;
                log.info("Notificação já existe para usuário {}", usuario.getNome());
            }
        }
        
        log.info("=== Resultado da criação de notificações ===");
        log.info("Criadas: {} notificações", notificacoesCriadas);
        log.info("Já existentes: {} notificações", notificacoesJaExistentes);
        log.info("Total de usuários processados: {}", alerta.getUsuariosAlerta().size());
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
        
        // usa o id da notificação e não o do alerta
        dto.setId(notificacao.getId().intValue());
        dto.setDataCriacao(notificacao.getDataHoraCriacao() != null ? 
            Timestamp.valueOf(notificacao.getDataHoraCriacao()) : null);
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
    public boolean excluirNotificacao(Long notificacaoId, Usuario usuario) throws SmartValidityException {
        try {
            Notificacao notificacao = notificacaoRepository.findByIdAndUsuario(notificacaoId, usuario)
                                             .orElse(null);

            if (notificacao == null) {
                return false;
            }

            // verifica se oitem-produto foi inspecionado
            ItemProduto itemProduto = null;
            if (notificacao.getAlerta() != null) {
                itemProduto = notificacao.getAlerta().getItemProduto();
            }
            if (itemProduto != null && Boolean.FALSE.equals(itemProduto.getInspecionado())) {
                throw new SmartValidityException("Não é possível excluir a notificação: o item-produto associado ainda não foi inspecionado.");
            }

            notificacaoRepository.delete(notificacao);
            log.info("Notificação {} excluída pelo usuário {}", notificacaoId, usuario.getId());
            return true;

        } catch (SmartValidityException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao excluir notificação {} do usuário {}: {}", 
                notificacaoId, usuario.getId(), e.getMessage(), e);
            return false;
        }
    }
    
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

    /**
     * Busca notificações de alertas relacionados a produtos já inspecionados.
     * Usado para o histórico de notificações de produtos inspecionados.
     * 
     * @param usuario O usuário
     * @return Lista de notificações de produtos inspecionados
     */
    public List<AlertaDTO.Listagem> buscarNotificacoesProdutosInspecionados(Usuario usuario) {
        try {
            List<Notificacao> notificacoes = notificacaoRepository.findByUsuarioAndProdutoInspecionado(usuario);
            return notificacoes.stream()
                    .map(this::convertNotificacaoToAlertaDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Erro ao buscar notificações de produtos inspecionados do usuário {}: {}", usuario.getId(), e.getMessage());
            return List.of();
        }
    }

    public List<AlertaDTO.Listagem> buscarNotificacoesProdutosInspecionadosDoUsuarioAutenticado() throws SmartValidityException {
        Usuario usuario = authenticationService.getUsuarioAutenticado();
        if (usuario == null) {
            throw new SmartValidityException("Usuário não autenticado");
        }
        return buscarNotificacoesProdutosInspecionados(usuario);
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

    /**
     * Exclui todas as notificações relacionadas a um alerta específico.
     * Este método é chamado automaticamente quando um alerta é excluído
     * logicamente após a inspeção do produto.
     * 
     * @param alerta O alerta cujas notificações devem ser excluídas
     * @throws SmartValidityException Se houver erro durante a exclusão
     */
    @Transactional
    public void excluirNotificacoesPorAlerta(Alerta alerta) throws SmartValidityException {
        if (alerta == null) {
            log.warn("Alerta é null, não há notificações para excluir");
            return;
        }

        log.info("Iniciando exclusão de notificações para o Alerta ID: {}", alerta.getId());

        try {
            // Busca todas as notificações relacionadas ao alerta
            List<Notificacao> notificacoesRelacionadas = notificacaoRepository.findByAlerta(alerta);

            if (notificacoesRelacionadas.isEmpty()) {
                log.info("Nenhuma notificação encontrada para o Alerta ID: {}", alerta.getId());
                return;
            }

            int notificacoesExcluidas = 0;
            for (Notificacao notificacao : notificacoesRelacionadas) {
                log.info("Excluindo notificação ID: {} do usuário: {}", 
                    notificacao.getId(), 
                    notificacao.getUsuario() != null ? notificacao.getUsuario().getNome() : "Desconhecido");
                
                // Remove a notificação do banco de dados
                notificacaoRepository.delete(notificacao);
                notificacoesExcluidas++;
            }

            log.info("Excluídas {} notificações para o Alerta ID: {}", notificacoesExcluidas, alerta.getId());

        } catch (Exception e) {
            log.error("Erro ao excluir notificações para Alerta ID {}: {}", alerta.getId(), e.getMessage(), e);
            throw new SmartValidityException("Erro ao excluir notificações relacionadas ao alerta: " + e.getMessage());
        }
    }

    /**
     * Busca notificações pendentes (não resolvidas) do usuário autenticado.
     * Uma notificação é considerada pendente quando o item-produto associado ainda não foi inspecionado.
     */
    public List<AlertaDTO.Listagem> buscarNotificacoesPendentesDoUsuarioAutenticado() throws SmartValidityException {
        Usuario usuario = authenticationService.getUsuarioAutenticado();
        if (usuario == null) {
            throw new SmartValidityException("Usuário não autenticado");
        }
        return buscarNotificacoesPendentes(usuario);
    }

    /**
     * Busca notificações pendentes (não resolvidas) de um usuário.
     * Uma notificação é considerada pendente quando o item-produto associado ainda não foi inspecionado.
     */
    public List<AlertaDTO.Listagem> buscarNotificacoesPendentes(Usuario usuario) {
        try {
            List<Notificacao> notificacoes = notificacaoRepository.findByUsuarioOrderByDataHoraCriacaoDesc(usuario);
            return notificacoes.stream()
                    .map(this::convertNotificacaoToAlertaDTO)
                    .filter(dto -> dto.getItemInspecionado() == Boolean.FALSE) // Apenas não inspecionados (pendentes)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Erro ao buscar notificações pendentes do usuário {}: {}", usuario.getId(), e.getMessage());
            return List.of();
        }
    }

    /**
     * Conta notificações pendentes (não resolvidas) do usuário autenticado.
     */
    public Long contarNotificacoesPendentesDoUsuarioAutenticado() throws SmartValidityException {
        Usuario usuario = authenticationService.getUsuarioAutenticado();
        if (usuario == null) {
            throw new SmartValidityException("Usuário não autenticado");
        }
        return contarNotificacoesPendentes(usuario);
    }

    /**
     * Conta notificações pendentes (não resolvidas) de um usuário.
     */
    public Long contarNotificacoesPendentes(Usuario usuario) {
        try {
            List<Notificacao> notificacoes = notificacaoRepository.findByUsuarioOrderByDataHoraCriacaoDesc(usuario);
            long count = notificacoes.stream()
                    .map(this::convertNotificacaoToAlertaDTO)
                    .filter(dto -> dto.getItemInspecionado() == Boolean.FALSE) // Apenas não inspecionados (pendentes)
                    .count();
            return count;
        } catch (Exception e) {
            log.warn("Erro ao contar notificações pendentes do usuário {}: {}", usuario.getId(), e.getMessage());
            return 0L;
        }
    }

    public List<AlertaDTO.Listagem> buscarNotificacoesPersonalizadasDoUsuarioAutenticado() throws SmartValidityException {
        Usuario usuario = authenticationService.getUsuarioAutenticado();
        if (usuario == null) {
            throw new SmartValidityException("Usuário não autenticado");
        }
        return buscarNotificacoesPersonalizadas(usuario);
    }

    /**
     * Busca notificações de alertas personalizados de um usuário.
     * 
     * @param usuario O usuário
     * @return Lista de notificações de alertas personalizados
     */
    public List<AlertaDTO.Listagem> buscarNotificacoesPersonalizadas(Usuario usuario) {
        try {
            List<Notificacao> notificacoes = notificacaoRepository.findByUsuarioAndAlertaTipoOrderByDataHoraCriacaoDesc(usuario, TipoAlerta.PERSONALIZADO);
            return notificacoes.stream()
                    .map(this::convertNotificacaoToAlertaDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Erro ao buscar notificações personalizadas do usuário {}: {}", usuario.getId(), e.getMessage());
            return List.of();
        }
    }

    /**
     * Conta todas as notificações não lidas do usuário (pendentes + personalizadas não lidas).
     * Usado para o contador do sininho no cabeçalho.
     */
    public Long contarNotificacoesNaoLidasTotalDoUsuarioAutenticado() throws SmartValidityException {
        Usuario usuario = authenticationService.getUsuarioAutenticado();
        if (usuario == null) {
            throw new SmartValidityException("Usuário não autenticado");
        }
        return contarNotificacoesNaoLidasTotal(usuario);
    }

    /**
     * Conta todas as notificações não lidas do usuário (pendentes + personalizadas não lidas).
     */
    public Long contarNotificacoesNaoLidasTotal(Usuario usuario) {
        try {
            // Contar notificações pendentes (produtos não inspecionados)
            Long pendentes = contarNotificacoesPendentes(usuario);
            
            // Contar notificações personalizadas não lidas
            Long personalizadasNaoLidas = notificacaoRepository.countByUsuarioAndLidaFalse(usuario);
            List<Notificacao> notificacoesPersonalizadas = notificacaoRepository.findByUsuarioAndAlertaTipoOrderByDataHoraCriacaoDesc(usuario, TipoAlerta.PERSONALIZADO);
            Long personalizadasNaoLidasFiltradas = notificacoesPersonalizadas.stream()
                    .filter(n -> !n.getLida())
                    .count();
            
            log.debug("Contador sininho - Usuário {}: {} pendentes + {} personalizadas não lidas = {} total", 
                usuario.getId(), pendentes, personalizadasNaoLidasFiltradas, pendentes + personalizadasNaoLidasFiltradas);
            
            return pendentes + personalizadasNaoLidasFiltradas;
        } catch (Exception e) {
            log.warn("Erro ao contar notificações não lidas total do usuário {}: {}", usuario.getId(), e.getMessage());
            return 0L;
        }
    }
} 