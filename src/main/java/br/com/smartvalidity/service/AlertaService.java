package br.com.smartvalidity.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.AlertaDTO;
import br.com.smartvalidity.model.dto.AlertaRequestDTO;
import br.com.smartvalidity.model.dto.AlertaResponseDTO;
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

    // ===== MÉTODOS LEGACY (compatibilidade com DTOs antigos) =====

    public AlertaResponseDTO create(AlertaRequestDTO dto) {
        Alerta alerta = dto.toEntity();
        alerta = alertaRepository.save(alerta);
        return AlertaResponseDTO.fromEntity(alerta);
    }

    public List<AlertaResponseDTO> findAll() {
        return alertaRepository.findAll().stream()
                .map(AlertaResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<AlertaResponseDTO> findById(Integer id) {
        return alertaRepository.findById(id).map(AlertaResponseDTO::fromEntity);
    }

    public Optional<AlertaResponseDTO> update(Integer id, AlertaRequestDTO dto) {
        return alertaRepository.findById(id).map(alerta -> {
            alerta.setTitulo(dto.getTitulo());
            alerta.setDescricao(dto.getDescricao());
            alerta.setDataHoraDisparo(dto.getDataHoraDisparo());
            alerta.setDisparoRecorrente(dto.isDisparoRecorrente());
            alerta.setFrequenciaDisparo(dto.getFrequenciaDisparo());
            // Relacionamentos podem ser atualizados aqui se necessário
            alerta = alertaRepository.save(alerta);
            return AlertaResponseDTO.fromEntity(alerta);
        });
    }

    public void delete(Integer id) {
        alertaRepository.deleteById(id);
    }
    
    // ===== MÉTODOS MODERNOS PARA O FRONTEND =====
    
    /**
     * Criar alerta personalizado (método moderno)
     */
    public AlertaDTO.Listagem criarAlerta(AlertaDTO.Cadastro alertaDTO, String usuarioCriadorId) throws SmartValidityException {
        log.info("=== Criando alerta personalizado ===");
        log.info("Dados recebidos: {}", alertaDTO);
        
        try {
            // Criar entidade Alerta
            Alerta alerta = new Alerta();
            alerta.setTitulo(alertaDTO.getTitulo());
            alerta.setDescricao(alertaDTO.getDescricao());
            alerta.setTipo(TipoAlerta.PERSONALIZADO); // Sempre PERSONALIZADO para alertas criados pelo usuário
            alerta.setDataHoraDisparo(alertaDTO.getDataHoraDisparo());
            alerta.setDiasAntecedencia(alertaDTO.getDiasAntecedencia());
            alerta.setRecorrente(alertaDTO.getRecorrente() != null ? alertaDTO.getRecorrente() : false);
            alerta.setConfiguracaoRecorrencia(alertaDTO.getConfiguracaoRecorrencia());
            alerta.setAtivo(true);
            alerta.setLido(false);
            
            // Definir usuário criador se fornecido
            if (usuarioCriadorId != null) {
                Usuario usuarioCriador = usuarioService.buscarPorId(usuarioCriadorId);
                alerta.setUsuarioCriador(usuarioCriador);
            }
            
            // Processar usuários que receberão o alerta
            Set<Usuario> usuariosAlerta = new HashSet<>();
            if (alertaDTO.getUsuariosIds() != null && !alertaDTO.getUsuariosIds().isEmpty()) {
                for (String usuarioId : alertaDTO.getUsuariosIds()) {
                    Usuario usuario = usuarioService.buscarPorId(usuarioId);
                    usuariosAlerta.add(usuario);
                }
            }
            alerta.setUsuariosAlerta(usuariosAlerta);
            
            // Processar produtos relacionados E VINCULAR ITENS-PRODUTO
            Set<Produto> produtosAlerta = new HashSet<>();
            if (alertaDTO.getProdutosIds() != null && !alertaDTO.getProdutosIds().isEmpty()) {
                for (String produtoId : alertaDTO.getProdutosIds()) {
                    Produto produto = produtoService.buscarPorId(produtoId);
                    produtosAlerta.add(produto);
                    
                    log.info("Produto vinculado ao alerta: {} (ID: {})", produto.getDescricao(), produto.getId());
                    
                    // Buscar itens-produto não inspecionados deste produto
                    List<ItemProduto> itensNaoInspecionados = itemProdutoService
                        .buscarItensProdutoNaoInspecionadosPorProduto(produtoId);
                    
                    log.info("Encontrados {} itens-produto não inspecionados para o produto {}", 
                        itensNaoInspecionados.size(), produto.getDescricao());
                }
            }
            alerta.setProdutosAlerta(produtosAlerta);
            
            // Se a data/hora de disparo for no passado ou não informada, torna-se ativo imediatamente.
            boolean deveAtivarAgora = alerta.getDataHoraDisparo() == null ||
                    !alerta.getDataHoraDisparo().isAfter(java.time.LocalDateTime.now());
            alerta.setAtivo(deveAtivarAgora);
            
            // Salvar o alerta
            alerta = alertaRepository.save(alerta);
            
            log.info("Alerta personalizado criado com sucesso: ID {}, Título: {}", 
                alerta.getId(), alerta.getTitulo());
            
            // Usar AlertaMapper para conversão
            return AlertaMapper.toListagemDTO(alerta);
            
        } catch (Exception e) {
            log.error("Erro ao criar alerta personalizado: {}", e.getMessage(), e);
            throw new SmartValidityException("Erro ao criar alerta: " + e.getMessage());
        }
    }
    
    /**
     * Buscar alerta por ID (método moderno)
     */
    public AlertaDTO.Listagem buscarPorId(Integer id) throws SmartValidityException {
        Alerta alerta = alertaRepository.findById(id)
            .orElseThrow(() -> new SmartValidityException("Alerta não encontrado com ID: " + id));
        
        // Usar AlertaMapper para conversão
        return AlertaMapper.toListagemDTO(alerta);
    }
    
    /**
     * Atualizar alerta personalizado (método moderno)
     */
    public AlertaDTO.Listagem atualizarAlerta(Integer id, AlertaDTO.Edicao alertaDTO) throws SmartValidityException {
        log.info("=== Atualizando alerta ID {} ===", id);
        
        try {
            Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new SmartValidityException("Alerta não encontrado com ID: " + id));
            
            // Verificar se é um alerta personalizado (só eles podem ser editados)
            if (alerta.getTipo() != TipoAlerta.PERSONALIZADO) {
                throw new SmartValidityException("Apenas alertas personalizados podem ser editados");
            }
            
            // Atualizar campos básicos
            alerta.setTitulo(alertaDTO.getTitulo());
            alerta.setDescricao(alertaDTO.getDescricao());
            alerta.setDataHoraDisparo(alertaDTO.getDataHoraDisparo());
            alerta.setDiasAntecedencia(alertaDTO.getDiasAntecedencia());
            alerta.setRecorrente(alertaDTO.getRecorrente() != null ? alertaDTO.getRecorrente() : false);
            alerta.setConfiguracaoRecorrencia(alertaDTO.getConfiguracaoRecorrencia());
            
            // Reavaliar ativação: se ainda inativo e o horário já passou, ativa
            if (alertaDTO.getAtivo() != null) {
                alerta.setAtivo(alertaDTO.getAtivo());
            } else {
                boolean deveAtivarAgora = alerta.getDataHoraDisparo() == null ||
                        !alerta.getDataHoraDisparo().isAfter(java.time.LocalDateTime.now());
                if (deveAtivarAgora && !Boolean.TRUE.equals(alerta.getAtivo())) {
                    alerta.setAtivo(true);
                }
            }
            
            // Atualizar usuários se fornecidos
            if (alertaDTO.getUsuariosIds() != null) {
                Set<Usuario> usuariosAlerta = new HashSet<>();
                for (String usuarioId : alertaDTO.getUsuariosIds()) {
                    Usuario usuario = usuarioService.buscarPorId(usuarioId);
                    usuariosAlerta.add(usuario);
                }
                alerta.setUsuariosAlerta(usuariosAlerta);
            }
            
            // Atualizar produtos se fornecidos
            if (alertaDTO.getProdutosIds() != null) {
                Set<Produto> produtosAlerta = new HashSet<>();
                for (String produtoId : alertaDTO.getProdutosIds()) {
                    Produto produto = produtoService.buscarPorId(produtoId);
                    produtosAlerta.add(produto);
                }
                alerta.setProdutosAlerta(produtosAlerta);
            }
            
            // Salvar alterações
            alerta = alertaRepository.save(alerta);
            
            log.info("Alerta {} atualizado com sucesso", id);
            
            // Usar AlertaMapper para conversão
            return AlertaMapper.toListagemDTO(alerta);
            
        } catch (Exception e) {
            log.error("Erro ao atualizar alerta {}: {}", id, e.getMessage(), e);
            throw new SmartValidityException("Erro ao atualizar alerta: " + e.getMessage());
        }
    }
    
    /**
     * Alterna o status "ativo" de um alerta.
     */
    public AlertaDTO.Listagem toggleAtivo(Integer id) throws SmartValidityException {
        try {
            Alerta alerta = alertaRepository.findById(id)
                    .orElseThrow(() -> new SmartValidityException("Alerta não encontrado com ID: " + id));

            alerta.setAtivo(!Boolean.TRUE.equals(alerta.getAtivo()));
            alerta = alertaRepository.save(alerta);

            // Usar AlertaMapper para conversão
            return AlertaMapper.toListagemDTO(alerta);
        } catch (Exception e) {
            log.error("Erro ao alternar status ativo do alerta {}: {}", id, e.getMessage(), e);
            throw new SmartValidityException("Erro ao alterar status do alerta: " + e.getMessage());
        }
    }

    /**
     * Buscar alertas aplicando filtros, com suporte a paginação simples em memória.
     */
    public List<AlertaDTO.Listagem> filtrarAlertas(AlertaDTO.Filtro filtro) {
        // Carregar todos e filtrar em memória (simples e sem custo de complexidade)
        List<Alerta> todos = alertaRepository.findAll();

        // ----- FILTROS -----
        var stream = todos.stream();

        if (filtro.getTitulo() != null && !filtro.getTitulo().isBlank()) {
            String termo = filtro.getTitulo().toLowerCase();
            stream = stream.filter(a -> a.getTitulo() != null && a.getTitulo().toLowerCase().contains(termo));
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

        // Coleta pós-filtro para aplicação de sorting / paginação
        List<Alerta> filtrados = stream.toList();

        // ----- SORT -----
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

        // ----- PAGINAÇÃO -----
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

        // Usar AlertaMapper para conversão
        return filtrados.stream()
                .map(AlertaMapper::toListagemDTO)
                .toList();
    }

    /**
     * Conta alertas filtrados (sem paginação).
     */
    public long contarAlertas(AlertaDTO.Filtro filtro) {
        // Reutiliza a lógica de filtro, mas sem paginação
        // Para evitar duplicação, chamar filtrarAlertas sem paginação (temporariamente)
        int paginaOriginal = filtro.getPagina();
        int limiteOriginal = filtro.getLimite();
        // desativa paginação
        filtro.setLimite(0);
        filtro.setPagina(0);
        long total = filtrarAlertas(filtro).size();
        // restaura valores
        filtro.setLimite(limiteOriginal);
        filtro.setPagina(paginaOriginal);
        return total;
    }
} 