package br.com.smartvalidity.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table
@Data
public class ItemProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "O lote não pode ser nulo.")
    @Column(nullable = false)
    private String lote;

    @NotNull(message = "O preço de compra não pode ser nulo.")
    @Column(name = "preco_compra", nullable = false)
    private Double precoCompra;

    @NotNull(message = "O preço de venda não pode ser nulo.")
    @Column(name = "preco_venda", nullable = false)
    private Double precoVenda;

    @NotNull(message = "A data de fabricação não pode ser nula.")
    @Column(name = "data_fabricacao", nullable = false)
    private LocalDateTime dataFabricacao;

    @NotNull(message = "A data de vencimento não pode ser nula.")
    @Column(name = "data_vencimento", nullable = false)
    private LocalDateTime dataVencimento;

    @NotNull(message = "A data de recebimento não pode ser nula.")
    @Column(name = "data_recebimento", nullable = false)
    private LocalDateTime dataRecebimento;

    @ManyToOne
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;
}
