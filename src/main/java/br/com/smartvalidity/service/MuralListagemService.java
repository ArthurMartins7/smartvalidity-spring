package br.com.smartvalidity.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.smartvalidity.exception.SmartValidityException;
import br.com.smartvalidity.model.dto.MuralListagemDTO;
import br.com.smartvalidity.model.entity.ItemProduto;

@Service
public class MuralListagemService {

    @Autowired
    private ItemProdutoService itemProdutoService;

    /**
     * Busca os itens próximos a vencer (até 15 dias)
     */
    public List<MuralListagemDTO> getProximosVencer() {
        List<ItemProduto> itens = itemProdutoService.buscarTodos();
        LocalDateTime hoje = LocalDateTime.now();
        LocalDateTime limite = hoje.plusDays(15);
        
        return itens.stream()
                .filter(item -> {
                    LocalDateTime vencimento = item.getDataVencimento();
                    return vencimento.isAfter(hoje) && vencimento.isBefore(limite);
                })
                .map(this::mapToDTO) //TODO: Pesquisar sobre Method Reference
                .collect(Collectors.toList());
    }

    /**
     * Busca os itens que vencem hoje
     */
    public List<MuralListagemDTO> getVencemHoje() {
        List<ItemProduto> itens = itemProdutoService.buscarTodos();
        LocalDateTime hoje = LocalDateTime.now();
        
        return itens.stream()
                .filter(item -> {
                    LocalDateTime vencimento = item.getDataVencimento();
                    return vencimento.toLocalDate().isEqual(hoje.toLocalDate());
                })
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca os itens já vencidos
     */
    public List<MuralListagemDTO> getVencidos() {
        List<ItemProduto> itens = itemProdutoService.buscarTodos();
        LocalDateTime hoje = LocalDateTime.now();
        
        return itens.stream()
                .filter(item -> item.getDataVencimento().isBefore(hoje))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Mapeia um ItemProduto para MuralListagemDTO
     */
    private MuralListagemDTO mapToDTO(ItemProduto item) {
        String status = determinarStatus(item.getDataVencimento());

        MuralListagemDTO.ProdutoDTO produtoDTO = MuralListagemDTO.ProdutoDTO.builder()
                .id(item.getProduto() != null ? item.getProduto().getId() : "")
                .nome(item.getProduto() != null ? item.getProduto().getDescricao() : "")
                .descricao(item.getProduto() != null ? item.getProduto().getDescricao() : "")
                .codigoBarras(item.getProduto() != null ? item.getProduto().getCodigoBarras() : "")
                .marca(item.getProduto() != null ? item.getProduto().getMarca() : "")
                .unidadeMedida(item.getProduto() != null ? item.getProduto().getUnidadeMedida() : "")
                .build();
        
        String categoria = item.getProduto() != null && item.getProduto().getCategoria() != null ?
                item.getProduto().getCategoria().getNome() : "";

        String corredor = item.getProduto() != null && item.getProduto().getCategoria() != null &&
                item.getProduto().getCategoria().getCorredor() != null ?
                item.getProduto().getCategoria().getCorredor().getNome() : "";

        String fornecedor = item.getProduto() != null && item.getProduto().getFornecedores() != null &&
                !item.getProduto().getFornecedores().isEmpty() ?
                item.getProduto().getFornecedores().get(0).getNome() : "";

        return MuralListagemDTO.builder()
                .id(item.getId())
                .itemProduto(item.getProduto() != null ? item.getProduto().getDescricao() : "")
                .produto(produtoDTO)
                .categoria(categoria)
                .corredor(corredor)
                .fornecedor(fornecedor)
                .dataValidade(item.getDataVencimento())
                .dataFabricacao(item.getDataFabricacao())
                .dataRecebimento(item.getDataRecebimento())
                .lote(item.getLote())
                .precoVenda(item.getPrecoVenda())
                .status(status)
                .inspecionado(item.getInspecionado())
                .motivoInspecao(item.getMotivoInspecao())
                .build();
    }

    /**
     * Determina o status do item com base na data de validade
     */
    private String determinarStatus(LocalDateTime dataVencimento) {
        LocalDateTime hoje = LocalDateTime.now();
        if (dataVencimento.isBefore(hoje)) {
            return "vencido";
        } else if (dataVencimento.toLocalDate().isEqual(hoje.toLocalDate())) {
            return "hoje";
        } else {
            return "proximo";
        }
    }
    
    /**
     * Método base para marcar um item como inspecionado
     * @param id ID do item a ser marcado
     * @param motivo Motivo da inspeção (opcional)
     * @return O item atualizado
     * @throws SmartValidityException Se o item não for encontrado
     */
    private MuralListagemDTO marcarItemInspecionado(String id, String motivo) throws SmartValidityException {
        ItemProduto item = itemProdutoService.buscarPorId(id);
        item.setInspecionado(true);
        item.setMotivoInspecao(motivo);
        itemProdutoService.salvar(item);
        return mapToDTO(item);
    }
    
    /**
     * Marca um item como inspecionado
     * @param id ID do item a ser marcado
     * @param motivo Motivo da inspeção
     * @return O item atualizado
     * @throws SmartValidityException Se o item não for encontrado
     */
    public MuralListagemDTO marcarInspecionado(String id, String motivo) throws SmartValidityException {
        return marcarItemInspecionado(id, motivo);
    }
    
    /**
     * Marca um item como inspecionado (método de compatibilidade)
     * @param id ID do item a ser marcado
     * @return O item atualizado
     * @throws SmartValidityException Se o item não for encontrado
     */
    public MuralListagemDTO marcarInspecionado(String id) throws SmartValidityException {
        return marcarItemInspecionado(id, null);
    }
    
    /**
     * Método base para marcar vários itens como inspecionados
     * @param ids Lista de IDs dos itens a serem marcados
     * @param motivo Motivo da inspeção (opcional)
     * @return Lista de itens atualizados
     * @throws SmartValidityException Se algum item não for encontrado
     */
    private List<MuralListagemDTO> marcarVariosItensInspecionados(List<String> ids, String motivo) throws SmartValidityException {
        List<MuralListagemDTO> itensAtualizados = new ArrayList<>();
        
        for (String id : ids) {
            try {
                ItemProduto item = itemProdutoService.buscarPorId(id);
                item.setInspecionado(true);
                item.setMotivoInspecao(motivo);
                itemProdutoService.salvar(item);
                itensAtualizados.add(mapToDTO(item));
            } catch (SmartValidityException e) {
                // Loga o erro mas continua processando os outros IDs
                System.err.println("Erro ao marcar item " + id + " como inspecionado: " + e.getMessage());
            }
        }
        
        return itensAtualizados;
    }
    
    /**
     * Marca vários itens como inspecionados
     * @param ids Lista de IDs dos itens a serem marcados
     * @param motivo Motivo da inspeção
     * @return Lista de itens atualizados
     * @throws SmartValidityException Se algum item não for encontrado
     */
    public List<MuralListagemDTO> marcarVariosInspecionados(List<String> ids, String motivo) throws SmartValidityException {
        return marcarVariosItensInspecionados(ids, motivo);
    }
    
    /**
     * Marca vários itens como inspecionados (método de compatibilidade)
     * @param ids Lista de IDs dos itens a serem marcados
     * @return Lista de itens atualizados
     * @throws SmartValidityException Se algum item não for encontrado
     */
    public List<MuralListagemDTO> marcarVariosInspecionados(List<String> ids) throws SmartValidityException {
        return marcarVariosItensInspecionados(ids, null);
    }
    
    /**
     * Busca um item específico por ID
     * @param id ID do item a ser buscado
     * @return O item encontrado
     * @throws SmartValidityException Se o item não for encontrado
     */
    public MuralListagemDTO getItemById(String id) throws SmartValidityException {
        ItemProduto item = itemProdutoService.buscarPorId(id);
        return mapToDTO(item);
    }
} 