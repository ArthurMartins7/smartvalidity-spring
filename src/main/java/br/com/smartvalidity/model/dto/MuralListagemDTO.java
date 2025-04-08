package br.com.smartvalidity.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferência de dados do mural de validades
 * Contém todas as informações necessárias para exibir os itens no mural
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MuralListagemDTO {
    private String id;
    private String itemProduto;
    private ProdutoDTO produto;
    private String categoria;
    private String corredor;
    private String fornecedor;
    private LocalDateTime dataValidade;
    private LocalDateTime dataFabricacao;
    private LocalDateTime dataRecebimento;
    private String lote;
    private Double precoVenda;
    private String status;
    private Boolean inspecionado;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProdutoDTO {
        private String id;
        private String nome;
        private String descricao;
        private String codigoBarras;
        private String marca;
        private String unidadeMedida;
    }
} 