package br.com.smartvalidity.model.dto;

import lombok.Data;
import java.time.LocalDateTime;
import br.com.smartvalidity.model.entity.Produto;

@Data
public class ItemProdutoDTO {
    private String id;
    private String lote;
    private Double precoVenda;
    private LocalDateTime dataFabricacao;
    private LocalDateTime dataVencimento;
    private LocalDateTime dataRecebimento;
    private Boolean inspecionado = false;
    private String motivoInspecao;
    private String usuarioInspecao;
    private LocalDateTime dataHoraInspecao;
    private Produto produto;
    private Integer quantidade = 1;
}
