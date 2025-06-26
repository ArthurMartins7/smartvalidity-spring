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
            
            // Salvar o alerta
            alerta = alertaRepository.save(alerta);
            
            log.info("Alerta personalizado criado com sucesso: ID {}, Título: {}", 
                alerta.getId(), alerta.getTitulo());
            
            // Converter para DTO de resposta
            return converterParaListagemDTO(alerta);
            
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
        
        return converterParaListagemDTO(alerta);
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
            
            if (alertaDTO.getAtivo() != null) {
                alerta.setAtivo(alertaDTO.getAtivo());
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
            
            return converterParaListagemDTO(alerta);
            
        } catch (Exception e) {
            log.error("Erro ao atualizar alerta {}: {}", id, e.getMessage(), e);
            throw new SmartValidityException("Erro ao atualizar alerta: " + e.getMessage());
        }
    }
    
    /**
     * Converter entidade Alerta para DTO de listagem
     */
    private AlertaDTO.Listagem converterParaListagemDTO(Alerta alerta) {
        AlertaDTO.Listagem dto = new AlertaDTO.Listagem();
        
        dto.setId(alerta.getId());
        dto.setTitulo(alerta.getTitulo());
        dto.setDescricao(alerta.getDescricao());
        dto.setTipo(alerta.getTipo());
        dto.setDataHoraDisparo(alerta.getDataHoraDisparo());
        dto.setDiasAntecedencia(alerta.getDiasAntecedencia());
        dto.setAtivo(alerta.getAtivo());
        dto.setRecorrente(alerta.getRecorrente());
        dto.setConfiguracaoRecorrencia(alerta.getConfiguracaoRecorrencia());
        dto.setDataCriacao(alerta.getDataHoraCriacao());
        
        // Usuário criador
        if (alerta.getUsuarioCriador() != null) {
            dto.setUsuarioCriador(alerta.getUsuarioCriador().getNome());
        }
        
        // Produtos relacionados (nomes para exibição)
        if (alerta.getProdutosAlerta() != null && !alerta.getProdutosAlerta().isEmpty()) {
            List<String> produtosNomes = alerta.getProdutosAlerta().stream()
                .map(Produto::getDescricao)
                .toList();
            dto.setProdutosAlerta(produtosNomes);
            
            List<String> produtosIds = alerta.getProdutosAlerta().stream()
                .map(Produto::getId)
                .toList();
            dto.setProdutosAlertaIds(produtosIds);
        }
        
        // Usuários que recebem o alerta
        if (alerta.getUsuariosAlerta() != null && !alerta.getUsuariosAlerta().isEmpty()) {
            List<String> usuariosNomes = alerta.getUsuariosAlerta().stream()
                .map(Usuario::getNome)
                .toList();
            dto.setUsuariosAlerta(usuariosNomes);
            
            List<String> usuariosIds = alerta.getUsuariosAlerta().stream()
                .map(Usuario::getId)
                .toList();
            dto.setUsuariosAlertaIds(usuariosIds);
        }
        
        return dto;
    }
} 