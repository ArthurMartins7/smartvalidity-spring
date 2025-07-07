package br.com.smartvalidity.model.entity;

import java.util.List;

import org.hibernate.validator.constraints.EAN;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Entity
@Table
@Data
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank(message = "O campo 'código de barras' não pode ser vazio ou apenas espaços em branco.")
    @EAN(message = "O código de barras deve estar no formato EAN-13 (13 dígitos).")
    @Column(unique = true, nullable = false)
    private String codigoBarras;

    @NotBlank(message = "O campo 'descrição' não pode ser vazio ou apenas espaços em branco.")
    private String descricao;

    @NotBlank(message = "O campo 'marca' não pode ser vazio ou apenas espaços em branco.")
    private String marca;

    @NotBlank(message = "O campo 'unidade de medida' não pode ser vazio ou apenas espaços em branco.")
    private String unidadeMedida;

    @NotNull(message = "A quantidade não pode ser nula.")
    @Min(value = 1, message = "A quantidade deve ser maior ou igual a 1.")
    @Column(nullable = false)
    private Integer quantidade;

    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    @NotNull(message = "O campo 'categoria' é obrigatório.")
    private Categoria categoria;

    @OneToMany(mappedBy = "produto")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<ItemProduto> itensProduto;

    @ManyToMany
    @JoinTable(
            name = "fornecedor_produto",
            joinColumns = @JoinColumn(name = "id_produto"),
            inverseJoinColumns = @JoinColumn(name = "id_fornecedor")
    )
    private List<Fornecedor> fornecedores;

    @ManyToMany(mappedBy = "produtosAlerta")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Alerta> alertas;
}