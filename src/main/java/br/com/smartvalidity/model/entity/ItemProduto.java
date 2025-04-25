package br.com.smartvalidity.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table
@Data
public class ItemProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotNull(message = "O lote não pode ser nulo.")
    @Column(nullable = false)
    private String lote;

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
    
    @Column(nullable = false)
    private Boolean inspecionado = false;
    
    @Column(name = "motivo_inspecao")
    private String motivoInspecao;

    @ManyToOne
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;
}
