package br.com.smartvalidity.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.EAN;

import java.time.LocalDateTime;

@Entity
@Table
@Data
public class ItemProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "O campo 'lote' não pode ser vazio ou apenas espaços em branco.")
    private String lote;

    @NotBlank(message = "O campo 'preço de compra' não pode ser vazio ou apenas espaços em branco.")
    private double precoCompra;

    @NotBlank(message = "O campo 'preço de venda' não pode ser vazio ou apenas espaços em branco.")
    private double precoVenda;

    @NotBlank(message = "O campo 'data de fabricacao' não pode ser vazio ou apenas espaços em branco.")
    private LocalDateTime dataFabricacao;

    @NotBlank(message = "O campo 'data de vencimento' não pode ser vazio ou apenas espaços em branco.")
    private LocalDateTime dataVencimento;

    @NotBlank(message = "O campo 'data de recebimento' não pode ser vazio ou apenas espaços em branco.")
    private LocalDateTime dataRecebimento;

    @ManyToOne()
    @JoinColumn(name = "id_produto")
    private Produto produto;

}
