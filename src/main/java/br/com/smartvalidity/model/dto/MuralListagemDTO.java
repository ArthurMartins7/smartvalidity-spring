package br.com.smartvalidity.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private String motivoInspecao;

    //TODO: Entender melhor o uso
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProdutoDTO { //
        private String id;
        private String nome;
        private String descricao;
        private String codigoBarras;
        private String marca;
        private String unidadeMedida;
    }
} 