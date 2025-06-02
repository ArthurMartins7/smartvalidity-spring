package br.com.smartvalidity.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Classe que centraliza todos os DTOs relacionados ao mural
 */
public class MuralDTO {

    /**
     * DTO para listagem de itens no mural
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Listagem {
        private String id;
        private String itemProduto;
        private Produto produto;
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
        private String usuarioInspecao;
        private LocalDateTime dataHoraInspecao;
    }

    /**
     * DTO para informações do produto
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Produto {
        private String id;
        private String nome;
        private String descricao;
        private String codigoBarras;
        private String marca;
        private String unidadeMedida;
    }

    /**
     * DTO para filtros do mural
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Filtro {
        private String corredor;
        private String categoria;
        private String fornecedor;
        private String marca;
        private String lote;
        private LocalDateTime dataVencimentoInicio;
        private LocalDateTime dataVencimentoFim;
        private LocalDateTime dataFabricacaoInicio;
        private LocalDateTime dataFabricacaoFim;
        private LocalDateTime dataRecebimentoInicio;
        private LocalDateTime dataRecebimentoFim;
        private Boolean inspecionado;
        private String motivoInspecao;
        private String usuarioInspecao;
        private String searchTerm;
        private String sortBy;
        private String sortDirection;
        private String status; // Pode ser: "proximo", "hoje", "vencido"
        
        // Campos para paginação
        private Integer pagina;
        private Integer limite;
        
        public boolean temPaginacao() {
            return this.limite != null && this.limite > 0 && this.pagina != null && this.pagina > 0;
        }
    }

    /**
     * DTO para requisição de inspeção
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InspecaoRequest {
        private String motivo;
        private String motivoCustomizado;
        private String usuarioInspecao;
    }

    /**
     * DTO para resposta de inspeção em lote
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InspecaoLoteRequest {
        private List<String> ids;
        private String motivo;
        private String motivoCustomizado;
        private String usuarioInspecao;
    }

    /**
     * DTO para requisição de relatório
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatorioRequest {
        private String tipo; // "SELECIONADOS", "PAGINA", "TODOS"
        private List<String> ids; // Para relatório de itens selecionados
        private MuralDTO.Filtro filtro; // Filtros aplicados
        private String status; // Status atual (proximo, hoje, vencido)
    }

    /**
     * Enum para tipos de relatório
     */
    public enum TipoRelatorio {
        SELECIONADOS,
        PAGINA,
        TODOS
    }
} 