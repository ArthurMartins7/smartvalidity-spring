package br.com.smartvalidity.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


public class MuralDTO {
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
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Filtro {
        private List<String> corredores;
        private List<String> categorias;
        private List<String> fornecedores;
        private List<String> marcas;
        private List<String> lotes;
        private List<String> motivosInspecao;
        private List<String> usuariosInspecao;
        private String corredor;
        private String categoria;
        private String fornecedor;
        private String marca;
        private String lote;
        private String motivoInspecao;
        private String usuarioInspecao;
        private LocalDateTime dataVencimentoInicio;
        private LocalDateTime dataVencimentoFim;
        private LocalDateTime dataFabricacaoInicio;
        private LocalDateTime dataFabricacaoFim;
        private LocalDateTime dataRecebimentoInicio;
        private LocalDateTime dataRecebimentoFim;
        private Boolean inspecionado;
        private String searchTerm;
        private String sortBy;
        private String sortDirection;
        private String status;
        private Integer pagina;
        private Integer limite;
        
        public boolean temPaginacao() {
            return this.limite != null && this.limite > 0 && this.pagina != null && this.pagina > 0;
        }
        
        public List<String> getCorredoresEfetivos() {
            if (corredores != null && !corredores.isEmpty()) {
                return corredores;
            }
            if (corredor != null && !corredor.trim().isEmpty()) {
                return List.of(corredor);
            }
            return List.of();
        }
        
        public List<String> getCategoriasEfetivas() {
            if (categorias != null && !categorias.isEmpty()) {
                return categorias;
            }
            if (categoria != null && !categoria.trim().isEmpty()) {
                return List.of(categoria);
            }
            return List.of();
        }
        
        public List<String> getFornecedoresEfetivos() {
            if (fornecedores != null && !fornecedores.isEmpty()) {
                return fornecedores;
            }
            if (fornecedor != null && !fornecedor.trim().isEmpty()) {
                return List.of(fornecedor);
            }
            return List.of();
        }
        
        public List<String> getMarcasEfetivas() {
            if (marcas != null && !marcas.isEmpty()) {
                return marcas;
            }
            if (marca != null && !marca.trim().isEmpty()) {
                return List.of(marca);
            }
            return List.of();
        }
        
        public List<String> getLotesEfetivos() {
            if (lotes != null && !lotes.isEmpty()) {
                return lotes;
            }
            if (lote != null && !lote.trim().isEmpty()) {
                return List.of(lote);
            }
            return List.of();
        }
        
        public List<String> getMotivosInspecaoEfetivos() {
            if (motivosInspecao != null && !motivosInspecao.isEmpty()) {
                return motivosInspecao;
            }
            if (motivoInspecao != null && !motivoInspecao.trim().isEmpty()) {
                return List.of(motivoInspecao);
            }
            return List.of();
        }
        
        public List<String> getUsuariosInspecaoEfetivos() {
            if (usuariosInspecao != null && !usuariosInspecao.isEmpty()) {
                return usuariosInspecao;
            }
            if (usuarioInspecao != null && !usuarioInspecao.trim().isEmpty()) {
                return List.of(usuarioInspecao);
            }
            return List.of();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InspecaoRequest {
        private String motivo;
        private String motivoCustomizado;
        private String usuarioInspecao;
    }
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatorioRequest {
        private String tipo;
        private List<String> ids;
        private MuralDTO.Filtro filtro;
        private String status;
    }

    public enum TipoRelatorio {
        SELECIONADOS,
        PAGINA,
        TODOS
    }
}