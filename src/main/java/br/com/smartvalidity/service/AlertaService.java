package br.com.smartvalidity.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.AlertaDTO;
import br.com.smartvalidity.model.entity.Alerta;
import br.com.smartvalidity.model.entity.ItemProduto;
import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.enums.TipoAlerta;
import br.com.smartvalidity.model.mapper.AlertaMapper;
import br.com.smartvalidity.model.repository.AlertaRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AlertaService {

    @Autowired
    private AlertaRepository alertaRepository;
    
    @Autowired
    @org.springframework.context.annotation.Lazy
    private ProdutoService produtoService;
    
    @Autowired
    @org.springframework.context.annotation.Lazy
    private ItemProdutoService itemProdutoService;
    
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private NotificacaoService notificacaoService;



    public AlertaDTO.Response create(AlertaDTO.Request dto) {
        Alerta alerta = dto.toEntity();
        alerta = alertaRepository.save(alerta);
        return AlertaDTO.Response.fromEntity(alerta);
    }

    public List<AlertaDTO.Response> findAll() {
        return alertaRepository.findAllNotDeleted().stream()
                .map(AlertaDTO.Response::fromEntity)
                .toList();
    }

    public Optional<AlertaDTO.Response> findById(Integer id) {
        return alertaRepository.findByIdAndExcluidoFalse(id).map(AlertaDTO.Response::fromEntity);
    }

    public Optional<AlertaDTO.Response> update(Integer id, AlertaDTO.Request dto) {
        return alertaRepository.findByIdAndExcluidoFalse(id)
                .map(alerta -> {
                    alerta.setTitulo(dto.getTitulo());
                    alerta.setDescricao(dto.getDescricao());
                    alerta.setDataHoraDisparo(dto.getDataHoraDisparo());
                    alerta.setDisparoRecorrente(dto.isDisparoRecorrente());
                    if (dto.getFrequenciaDisparo() != null) {
                        alerta.setFrequenciaDisparo(dto.getFrequenciaDisparo().name());
                    }
                    return AlertaDTO.Response.fromEntity(alertaRepository.save(alerta));
                });
    }

    /**
     * Exclui um alerta logicamente (mantém notificações existentes)
     * RESPONSABILIDADE SERVICE: Lógica de negócio para exclusão lógica
     * PRINCÍPIO MVC: Preserva integridade das notificações já criadas
     */
    @org.springframework.transaction.annotation.Transactional
    public void delete(Integer id) throws SmartValidityException {
        log.info("Iniciando exclusão lógica do alerta ID: {}", id);
        
        try {
            // Verificar se o alerta existe e não está excluído
            Alerta alerta = alertaRepository.findByIdAndExcluidoFalse(id)
                .orElseThrow(() -> new SmartValidityException("Alerta não encontrado com ID: " + id));

            log.info("Alerta encontrado: {} (Tipo: {})", alerta.getTitulo(), alerta.getTipo());

            // Regra de negócio: só excluir se item-produto estiver inspecionado
            validarItemProdutoInspecionado(alerta.getItemProduto());

            // Exclusão lógica: marcar como excluído
            alerta.setExcluido(true);
            alerta.setAtivo(false); // Também desativar
            alertaRepository.save(alerta);
            
            log.info("Alerta {} excluído logicamente com sucesso. Notificações preservadas.", id);
            
        } catch (SmartValidityException e) {
            log.error("Erro de validação ao excluir alerta {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao excluir alerta {}: {}", id, e.getMessage(), e);
            throw new SmartValidityException("Erro interno ao excluir alerta: " + e.getMessage());
        }
    }
    
    public AlertaDTO.Listagem criarAlerta(AlertaDTO.Cadastro alertaDTO, String usuarioCriadorId) throws SmartValidityException {
        log.info("=== Criando alerta personalizado ===");
        log.info("Dados recebidos: {}", alertaDTO);
        log.info("Data/hora disparo recebida: {} (tipo: {})", 
            alertaDTO.getDataHoraDisparo(), 
            alertaDTO.getDataHoraDisparo() != null ? alertaDTO.getDataHoraDisparo().getClass().getSimpleName() : "null");
        
        try {
            log.info("Criando entidade Alerta...");
            Alerta alerta = new Alerta();
            alerta.setTitulo(alertaDTO.getTitulo());
            alerta.setDescricao(alertaDTO.getDescricao());
            alerta.setTipo(TipoAlerta.PERSONALIZADO);
            
            log.info("Definindo data/hora de disparo...");
            alerta.setDataHoraDisparo(alertaDTO.getDataHoraDisparo());
            log.info("Data/hora definida: {}", alerta.getDataHoraDisparo());
            
            alerta.setDiasAntecedencia(alertaDTO.getDiasAntecedencia());
            alerta.setRecorrente(alertaDTO.getRecorrente() != null ? alertaDTO.getRecorrente() : false);
            alerta.setConfiguracaoRecorrencia(alertaDTO.getConfiguracaoRecorrencia());
            alerta.setAtivo(true);
            
            // Definir campos de disparo recorrente com valores padrão
            alerta.setDisparoRecorrente(false);
            alerta.setFrequenciaDisparo(null);
            
            log.info("Processando usuário criador...");
            if (usuarioCriadorId != null) {
                Usuario usuarioCriador = usuarioService.buscarPorId(usuarioCriadorId);
                alerta.setUsuarioCriador(usuarioCriador);
            }
            
            log.info("Processando usuários do alerta...");
            Set<Usuario> usuariosAlerta = new HashSet<>();
            if (alertaDTO.getUsuariosIds() != null && !alertaDTO.getUsuariosIds().isEmpty()) {
                for (String usuarioId : alertaDTO.getUsuariosIds()) {
                    Usuario usuario = usuarioService.buscarPorId(usuarioId);
                    usuariosAlerta.add(usuario);
                }
            }
            if (alerta.getUsuarioCriador() != null) {
                usuariosAlerta.add(alerta.getUsuarioCriador());
            }
            alerta.setUsuariosAlerta(usuariosAlerta);
            
            log.info("Processando produtos do alerta...");
            Set<Produto> produtosAlerta = new HashSet<>();
            if (alertaDTO.getProdutosIds() != null && !alertaDTO.getProdutosIds().isEmpty()) {
                for (String produtoId : alertaDTO.getProdutosIds()) {
                    Produto produto = produtoService.buscarPorId(produtoId);
                    produtosAlerta.add(produto);
                    
                    log.info("Produto vinculado ao alerta: {} (ID: {})", produto.getDescricao(), produto.getId());
                    
                    List<ItemProduto> itensNaoInspecionados = itemProdutoService
                        .buscarItensProdutoNaoInspecionadosPorProduto(produtoId);
                    
                    log.info("Encontrados {} itens-produto não inspecionados para o produto {}", 
                        itensNaoInspecionados.size(), produto.getDescricao());
                }
            }
            alerta.setProdutosAlerta(produtosAlerta);
            
            log.info("Salvando alerta no banco de dados...");
            alerta = alertaRepository.save(alerta);
            log.info("Alerta salvo com ID: {}", alerta.getId());
            
            log.info("Criando notificações para o alerta...");
            notificacaoService.criarNotificacoesParaAlerta(alerta);
            
            log.info("Convertendo para DTO de resposta...");
            AlertaDTO.Listagem response = AlertaMapper.toListagemDTO(alerta);
            
            log.info("Alerta personalizado criado com sucesso: ID {}, Título: {}", 
                alerta.getId(), alerta.getTitulo());
            
            return response;
            
        } catch (Exception e) {
            log.error("Erro ao criar alerta personalizado: {}", e.getMessage(), e);
            throw new SmartValidityException("Erro ao criar alerta: " + e.getMessage());
        }
    }
    
    public AlertaDTO.Listagem buscarPorId(Integer id) throws SmartValidityException {
        Alerta alerta = alertaRepository.findByIdAndExcluidoFalse(id)
            .orElseThrow(() -> new SmartValidityException("Alerta não encontrado com ID: " + id));
        
        return AlertaMapper.toListagemDTO(alerta);
    }
    
    public AlertaDTO.Listagem atualizarAlerta(Integer id, AlertaDTO.Edicao alertaDTO) throws SmartValidityException {
        log.info("=== Atualizando alerta ID {} ===", id);
        
        try {
            Alerta alerta = alertaRepository.findByIdAndExcluidoFalse(id)
                .orElseThrow(() -> new SmartValidityException("Alerta não encontrado com ID: " + id));
            
            if (alerta.getTipo() != TipoAlerta.PERSONALIZADO) {
                throw new SmartValidityException("Apenas alertas personalizados podem ser editados");
            }
            
            alerta.setTitulo(alertaDTO.getTitulo());
            alerta.setDescricao(alertaDTO.getDescricao());
            alerta.setDataHoraDisparo(alertaDTO.getDataHoraDisparo());
            alerta.setDiasAntecedencia(alertaDTO.getDiasAntecedencia());
            alerta.setRecorrente(alertaDTO.getRecorrente() != null ? alertaDTO.getRecorrente() : false);
            alerta.setConfiguracaoRecorrencia(alertaDTO.getConfiguracaoRecorrencia());

            // Definir campos de disparo recorrente com valores padrão
            alerta.setDisparoRecorrente(false);
            alerta.setFrequenciaDisparo(null);

            if (alertaDTO.getAtivo() != null) {
                alerta.setAtivo(alertaDTO.getAtivo());
            }
            
            if (alertaDTO.getUsuariosIds() != null) {
                Set<Usuario> usuariosAlerta = new HashSet<>();
                for (String usuarioId : alertaDTO.getUsuariosIds()) {
                    Usuario usuario = usuarioService.buscarPorId(usuarioId);
                    usuariosAlerta.add(usuario);
                }
                alerta.setUsuariosAlerta(usuariosAlerta);
            }
            
            if (alertaDTO.getProdutosIds() != null) {
                Set<Produto> produtosAlerta = new HashSet<>();
                for (String produtoId : alertaDTO.getProdutosIds()) {
                    Produto produto = produtoService.buscarPorId(produtoId);
                    produtosAlerta.add(produto);
                }
                alerta.setProdutosAlerta(produtosAlerta);
            }
            
            alerta = alertaRepository.save(alerta);
            
            log.info("Alerta {} atualizado com sucesso", id);
            
            return AlertaMapper.toListagemDTO(alerta);
            
        } catch (Exception e) {
            log.error("Erro ao atualizar alerta {}: {}", id, e.getMessage(), e);
            throw new SmartValidityException("Erro ao atualizar alerta: " + e.getMessage());
        }
    }
    
    /**
     * Alterna o status ativo de um alerta
     * RESPONSABILIDADE SERVICE: Lógica de negócio para alteração de status
     * PRINCÍPIO MVC: Isola CONTROLLER da lógica de negócio e acesso a dados
     */
    public AlertaDTO.Listagem toggleAtivo(Integer id) throws SmartValidityException {
        try {
            Alerta alerta = alertaRepository.findByIdAndExcluidoFalse(id)
                    .orElseThrow(() -> new SmartValidityException("Alerta não encontrado com ID: " + id));

            // Aplicar lógica de negócio: inverter status ativo
            Boolean novoStatus = !Boolean.TRUE.equals(alerta.getAtivo());
            alerta.setAtivo(novoStatus);

            // Persistir alteração
            alerta = alertaRepository.save(alerta);

            log.info("Status do alerta {} alterado para: {}", id, novoStatus);
            return AlertaMapper.toListagemDTO(alerta);
            
        } catch (SmartValidityException e) {
            log.error("Erro ao alterar status do alerta {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao alterar status do alerta {}: {}", id, e.getMessage(), e);
            throw new SmartValidityException("Erro interno ao alterar status do alerta: " + e.getMessage());
        }
    }

    public List<AlertaDTO.Listagem> filtrarAlertas(AlertaDTO.Filtro filtro) {
        List<Alerta> todos = alertaRepository.findAllNotDeleted();

        var stream = todos.stream();

        if (filtro.getTitulo() != null && !filtro.getTitulo().isBlank()) {
            String termo = filtro.getTitulo().toLowerCase();
            stream = stream.filter(a ->
                (a.getTitulo() != null && a.getTitulo().toLowerCase().contains(termo)) ||
                (a.getDescricao() != null && a.getDescricao().toLowerCase().contains(termo))
            );
        }
        if (filtro.getTipo() != null) {
            stream = stream.filter(a -> filtro.getTipo().equals(a.getTipo()));
        }
        if (filtro.getAtivo() != null) {
            stream = stream.filter(a -> filtro.getAtivo().equals(a.getAtivo()));
        }
        if (filtro.getRecorrente() != null) {
            stream = stream.filter(a -> filtro.getRecorrente().equals(a.getRecorrente()));
        }
        if (filtro.getUsuarioCriador() != null && !filtro.getUsuarioCriador().isBlank()) {
            String termo = filtro.getUsuarioCriador().toLowerCase();
            stream = stream.filter(a -> a.getUsuarioCriador() != null && a.getUsuarioCriador().getNome() != null && a.getUsuarioCriador().getNome().toLowerCase().contains(termo));
        }
        if (filtro.getDataInicialDisparo() != null) {
            stream = stream.filter(a -> a.getDataHoraDisparo() != null && !a.getDataHoraDisparo().isBefore(filtro.getDataInicialDisparo()));
        }
        if (filtro.getDataFinalDisparo() != null) {
            stream = stream.filter(a -> a.getDataHoraDisparo() != null && !a.getDataHoraDisparo().isAfter(filtro.getDataFinalDisparo()));
        }

        List<Alerta> filtrados = stream.toList();

        String sortBy = filtro.getSortBy() != null ? filtro.getSortBy() : "dataCriacao";
        boolean desc = "desc".equalsIgnoreCase(filtro.getSortDirection());
        java.util.Comparator<Alerta> comparator;
        switch (sortBy) {
            case "dataDisparo", "dataHoraDisparo" -> comparator = java.util.Comparator.comparing(
                    Alerta::getDataHoraDisparo, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
            case "titulo" -> comparator = java.util.Comparator.comparing(
                    Alerta::getTitulo, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
            default -> comparator = java.util.Comparator.comparing(
                    Alerta::getDataHoraCriacao, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
        }
        if (desc) {
            comparator = comparator.reversed();
        }
        filtrados = filtrados.stream().sorted(comparator).toList();

        if (filtro.temPaginacao()) {
            int pagina = filtro.getPagina();
            int limite = filtro.getLimite();
            int from = (pagina - 1) * limite;
            int to = Math.min(from + limite, filtrados.size());
            if (from >= filtrados.size()) {
                filtrados = List.of();
            } else {
                filtrados = filtrados.subList(from, to);
            }
        }

        return filtrados.stream()
                .map(AlertaMapper::toListagemDTO)
                .toList();
    }

    public long contarAlertasFiltrados(AlertaDTO.Filtro filtro) {
        List<Alerta> todos = alertaRepository.findAllNotDeleted();

        var stream = todos.stream();

        if (filtro.getTitulo() != null && !filtro.getTitulo().isBlank()) {
            String termo = filtro.getTitulo().toLowerCase();
            stream = stream.filter(a ->
                (a.getTitulo() != null && a.getTitulo().toLowerCase().contains(termo)) ||
                (a.getDescricao() != null && a.getDescricao().toLowerCase().contains(termo))
            );
        }
        if (filtro.getTipo() != null) {
            stream = stream.filter(a -> filtro.getTipo().equals(a.getTipo()));
        }
        if (filtro.getAtivo() != null) {
            stream = stream.filter(a -> filtro.getAtivo().equals(a.getAtivo()));
        }
        if (filtro.getRecorrente() != null) {
            stream = stream.filter(a -> filtro.getRecorrente().equals(a.getRecorrente()));
        }
        if (filtro.getUsuarioCriador() != null && !filtro.getUsuarioCriador().isBlank()) {
            String termo = filtro.getUsuarioCriador().toLowerCase();
            stream = stream.filter(a -> a.getUsuarioCriador() != null && a.getUsuarioCriador().getNome() != null && a.getUsuarioCriador().getNome().toLowerCase().contains(termo));
        }
        if (filtro.getDataInicialDisparo() != null) {
            stream = stream.filter(a -> a.getDataHoraDisparo() != null && !a.getDataHoraDisparo().isBefore(filtro.getDataInicialDisparo()));
        }
        if (filtro.getDataFinalDisparo() != null) {
            stream = stream.filter(a -> a.getDataHoraDisparo() != null && !a.getDataHoraDisparo().isAfter(filtro.getDataFinalDisparo()));
        }

        return stream.count();
    }

    private void validarItemProdutoInspecionado(ItemProduto itemProduto) throws SmartValidityException {
        if (itemProduto != null && Boolean.FALSE.equals(itemProduto.getInspecionado())) {
            throw new SmartValidityException("Não é possível excluir o alerta: o item-produto associado ainda não foi inspecionado.");
        }
    }
} 