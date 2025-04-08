package br.com.smartvalidity.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.smartvalidity.model.dto.MuralListagemDTO;
import br.com.smartvalidity.model.entity.ItemProduto;
import br.com.smartvalidity.exception.SmartValidityException;

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
                .map(this::mapToDTO)
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
        
        return MuralListagemDTO.builder()
                .id(item.getId())
                .itemProduto(item.getProduto() != null ? item.getProduto().getDescricao() : "")
                .produto(MuralListagemDTO.ProdutoDTO.builder()
                        .id(item.getProduto() != null ? item.getProduto().getId() : "")
                        .nome(item.getProduto() != null ? item.getProduto().getDescricao() : "")
                        .descricao(item.getProduto() != null ? item.getProduto().getDescricao() : "")
                        .codigoBarras(item.getProduto() != null ? item.getProduto().getCodigoBarras() : "")
                        .marca(item.getProduto() != null ? item.getProduto().getMarca() : "")
                        .unidadeMedida(item.getProduto() != null ? item.getProduto().getUnidadeMedida() : "")
                        .build())
                .categoria(item.getProduto() != null && item.getProduto().getCategoria() != null ? 
                        item.getProduto().getCategoria().getNome() : "")
                .corredor(item.getProduto() != null && item.getProduto().getCategoria() != null && 
                        item.getProduto().getCategoria().getCorredor() != null ? 
                        item.getProduto().getCategoria().getCorredor().getNome() : "")
                .fornecedor(item.getProduto() != null && item.getProduto().getFornecedores() != null && 
                        !item.getProduto().getFornecedores().isEmpty() ? 
                        item.getProduto().getFornecedores().get(0).getNome() : "")
                .dataValidade(item.getDataVencimento())
                .lote(item.getLote())
                .status(status)
                .inspecionado(item.getInspecionado())
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
     * Marca um item como inspecionado
     * @param id ID do item a ser marcado
     * @return O item atualizado
     * @throws SmartValidityException Se o item não for encontrado
     */
    public MuralListagemDTO marcarInspecionado(String id) throws SmartValidityException {
        ItemProduto item = itemProdutoService.buscarPorId(id);
        item.setInspecionado(!item.getInspecionado()); // Toggle o status de inspeção
        itemProdutoService.salvar(item);
        return mapToDTO(item);
    }
} 