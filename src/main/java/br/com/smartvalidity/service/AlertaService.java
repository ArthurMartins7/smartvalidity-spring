package br.com.smartvalidity.service;

import java.time.LocalDateTime;
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

    public List<AlertaDTO.Listagem> buscarAlertasAtivos() {
        List<Alerta> alertasAtivos = alertaRepository.findAllNotDeleted().stream()
                .filter(alerta -> alerta.getItemProduto() == null || !alerta.getItemProduto().getInspecionado())
                .toList();
        
        return alertasAtivos.stream()
                .map(AlertaMapper::toListagemDTO)
                .toList();
    }

    public List<AlertaDTO.Listagem> buscarAlertasJaResolvidos() {
        List<Alerta> alertasResolvidos = alertaRepository.findAlertasJaResolvidos();
        return alertasResolvidos.stream()
                .map(AlertaMapper::toListagemDTO)
                .toList();
    }

    public List<AlertaDTO.Listagem> buscarAlertasPersonalizados() {
        List<Alerta> alertasPersonalizados = alertaRepository.findByTipoAndExcluidoFalse(TipoAlerta.PERSONALIZADO);
        return alertasPersonalizados.stream()
                .map(AlertaMapper::toListagemDTO)
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
                    return AlertaDTO.Response.fromEntity(alertaRepository.save(alerta));
                });
    }


    @org.springframework.transaction.annotation.Transactional
    public void delete(Integer id) throws SmartValidityException {
        log.info("Iniciando exclusão lógica do alerta ID: {}", id);
        
        try {
            
            Alerta alerta = alertaRepository.findByIdAndExcluidoFalse(id)
                .orElseThrow(() -> new SmartValidityException("Alerta não encontrado com ID: " + id));
                
            log.info("Alerta encontrado: {} (Tipo: {})", alerta.getTitulo(), alerta.getTipo());

            validarItemProdutoInspecionado(alerta.getItemProduto());

            alerta.setExcluido(true);

            alertaRepository.save(alerta);

            // Para alertas personalizados, excluir também as notificações relacionadas
            if (alerta.getTipo() == TipoAlerta.PERSONALIZADO) {
                log.info("Excluindo notificações relacionadas ao alerta personalizado ID: {}", id);
                notificacaoService.excluirNotificacoesPorAlerta(alerta);
                log.info("Alerta personalizado {} excluído logicamente com notificações removidas.", id);
            } else {
                log.info("Alerta {} excluído logicamente com sucesso. Notificações preservadas.", id);
            }
            
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
        // Data/hora de disparo removida - alertas personalizados são disparados imediatamente
        
        // Validação: pelo menos um colaborador deve ser selecionado
        if (alertaDTO.getUsuariosIds() == null || alertaDTO.getUsuariosIds().isEmpty()) {
            throw new SmartValidityException("Pelo menos um colaborador deve ser selecionado para o alerta");
        }
        
        // Validação de recorrência removida - alertas personalizados são mais simples
        
        try {
            log.info("Criando entidade Alerta...");
            Alerta alerta = new Alerta();
            alerta.setTitulo(alertaDTO.getTitulo());
            alerta.setDescricao(alertaDTO.getDescricao());
            alerta.setTipo(TipoAlerta.PERSONALIZADO);
            
            log.info("Definindo data/hora de disparo como agora (alertas personalizados são disparados imediatamente)...");
            alerta.setDataHoraDisparo(LocalDateTime.now());
            log.info("Data/hora definida: {}", alerta.getDataHoraDisparo());
            
            // Configuração de recorrência removida - alertas personalizados são mais simples
            
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
                    try {
                        Produto produto = produtoService.buscarPorId(produtoId);
                        produtosAlerta.add(produto);
                        
                        log.info("Produto vinculado ao alerta: {} (ID: {})", produto.getDescricao(), produto.getId());
                        
                        List<ItemProduto> itensNaoInspecionados = itemProdutoService
                            .buscarItensProdutoNaoInspecionadosPorProduto(produto.getId());
                        
                        log.info("Encontrados {} itens-produto não inspecionados para o produto {}", 
                            itensNaoInspecionados.size(), produto.getDescricao());
                    } catch (Exception e) {
                        log.error("Erro ao buscar produto com ID '{}': {}", produtoId, e.getMessage());
                        // Tentar buscar por outros critérios se o ID não for válido
                        List<Produto> produtosEncontrados = produtoService.buscarPorTermoComItensNaoInspecionados(produtoId, 1);
                        if (!produtosEncontrados.isEmpty()) {
                            Produto produto = produtosEncontrados.get(0);
                            produtosAlerta.add(produto);
                            log.info("Produto encontrado por termo: {} -> {} (ID: {})", 
                                produtoId, produto.getDescricao(), produto.getId());
                            
                            List<ItemProduto> itensNaoInspecionados = itemProdutoService
                                .buscarItensProdutoNaoInspecionadosPorProduto(produto.getId());
                            
                            log.info("Encontrados {} itens-produto não inspecionados para o produto {}", 
                                itensNaoInspecionados.size(), produto.getDescricao());
                        } else {
                            log.warn("Produto não encontrado com ID ou termo: {}", produtoId);
                        }
                    }
                }
            }
            alerta.setProdutosAlerta(produtosAlerta);
            
            log.info("Salvando alerta no banco de dados...");
            alerta = alertaRepository.save(alerta);
            log.info("Alerta salvo com ID: {}", alerta.getId());
            
            // Notificações são criadas imediatamente para alertas personalizados
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
            
            // Validação de recorrência removida - alertas personalizados são mais simples
            
            // Atualiza campos básicos
            if (alertaDTO.getTitulo() != null) {
                alerta.setTitulo(alertaDTO.getTitulo());
            }
            if (alertaDTO.getDescricao() != null) {
                alerta.setDescricao(alertaDTO.getDescricao());
            }
            // dataHoraDisparo não é alterado em alertas personalizados - sempre mantém o original
            
            // Lógica de recorrência removida - alertas personalizados são mais simples
            
            // Atualiza usuários apenas se fornecidos
            if (alertaDTO.getUsuariosIds() != null) {
                Set<Usuario> usuariosAlerta = new HashSet<>();
                if (!alertaDTO.getUsuariosIds().isEmpty()) {
                    for (String usuarioId : alertaDTO.getUsuariosIds()) {
                        Usuario usuario = usuarioService.buscarPorId(usuarioId);
                        usuariosAlerta.add(usuario);
                    }
                }
                alerta.setUsuariosAlerta(usuariosAlerta);
            }
            
            // Atualiza produtos apenas se fornecidos
            if (alertaDTO.getProdutosIds() != null) {
                Set<Produto> produtosAlerta = new HashSet<>();
                if (!alertaDTO.getProdutosIds().isEmpty()) {
                    for (String produtoId : alertaDTO.getProdutosIds()) {
                        try {
                            Produto produto = produtoService.buscarPorId(produtoId);
                            produtosAlerta.add(produto);
                        } catch (Exception e) {
                            log.error("Erro ao buscar produto com ID '{}': {}", produtoId, e.getMessage());
                            // Tentar buscar por outros critérios se o ID não for válido
                            List<Produto> produtosEncontrados = produtoService.buscarPorTermoComItensNaoInspecionados(produtoId, 1);
                            if (!produtosEncontrados.isEmpty()) {
                                produtosAlerta.add(produtosEncontrados.get(0));
                                log.info("Produto encontrado por termo: {} -> {}", produtoId, produtosEncontrados.get(0).getId());
                            } else {
                                log.warn("Produto não encontrado com ID ou termo: {}", produtoId);
                            }
                        }
                    }
                }
                alerta.setProdutosAlerta(produtosAlerta);
            }
            
            alerta = alertaRepository.save(alerta);
            
            // Criar notificações para usuários (novos e existentes)
            // O método criarNotificacoesParaAlerta já possui proteção contra duplicação
            log.info("Criando/atualizando notificações para o alerta...");
            notificacaoService.criarNotificacoesParaAlerta(alerta);
            
            log.info("Alerta {} atualizado com sucesso", id);
            
            return AlertaMapper.toListagemDTO(alerta);
            
        } catch (Exception e) {
            log.error("Erro ao atualizar alerta {}: {}", id, e.getMessage(), e);
            throw new SmartValidityException("Erro ao atualizar alerta: " + e.getMessage());
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

    /**
     * Exclui logicamente todos os alertas relacionados a um ItemProduto específico
     * após a inspeção do produto. Este método é chamado automaticamente quando
     * um produto é inspecionado no mural.
     * 
     * @param itemProduto O item produto que foi inspecionado
     * @throws SmartValidityException Se houver erro durante a exclusão
     */
    @org.springframework.transaction.annotation.Transactional
    public void excluirAlertasPorItemProdutoInspecionado(ItemProduto itemProduto) throws SmartValidityException {
        if (itemProduto == null) {
            log.warn("ItemProduto é null, não há alertas para excluir");
            return;
        }

        log.info("Iniciando exclusão lógica de alertas para o ItemProduto ID: {}", itemProduto.getId());

        try {
            // Busca todos os alertas não excluídos relacionados ao item produto
            List<Alerta> alertasRelacionados = alertaRepository.findByItemProdutoAndExcluidoFalse(itemProduto);

            if (alertasRelacionados.isEmpty()) {
                log.info("Nenhum alerta encontrado para o ItemProduto ID: {}", itemProduto.getId());
                return;
            }

            int alertasExcluidos = 0;
            for (Alerta alerta : alertasRelacionados) {
                log.info("Excluindo logicamente alerta ID: {} ({})", alerta.getId(), alerta.getTitulo());
                
                // Marca o alerta como excluído
                alerta.setExcluido(true);
                alertaRepository.save(alerta);
                
                // Remove as notificações relacionadas a este alerta
                notificacaoService.excluirNotificacoesPorAlerta(alerta);
                
                alertasExcluidos++;
            }

            log.info("Excluídos logicamente {} alertas para o ItemProduto ID: {}", alertasExcluidos, itemProduto.getId());

        } catch (Exception e) {
            log.error("Erro ao excluir alertas para ItemProduto ID {}: {}", itemProduto.getId(), e.getMessage(), e);
            throw new SmartValidityException("Erro ao excluir alertas relacionados ao produto inspecionado: " + e.getMessage());
        }
    }

    /**
     * Métodos de recorrência removidos - alertas personalizados são mais simples
     */
} 