package br.com.smartvalidity.model.dto;

import br.com.smartvalidity.model.entity.Produto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EstoqueDTO {

    private String id;

    private String lote;

    private LocalDateTime dataFabricacao;

    private LocalDateTime dataVencimento;

    private LocalDateTime dataRecebimento;

    private Double precoVenda;

    private Produto produto;
}
