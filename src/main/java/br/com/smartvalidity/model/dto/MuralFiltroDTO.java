package br.com.smartvalidity.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para filtros do mural
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MuralFiltroDTO {
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