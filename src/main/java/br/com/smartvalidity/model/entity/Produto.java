package br.com.smartvalidity.model.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.EAN;

import java.util.List;

@Entity
@Table
@Data
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank(message = "O campo 'código de barras' não pode ser vazio ou apenas espaços em branco.")
    @EAN
    private String codigoBarras;

    @NotBlank(message = "O campo 'descrição' não pode ser vazio ou apenas espaços em branco.")
    private String descricao;

    @NotBlank(message = "O campo 'marca' não pode ser vazio ou apenas espaços em branco.")
    private String marca;

    @NotBlank(message = "O campo 'unidade de medida' não pode ser vazio ou apenas espaços em branco.")
    private String unidadeMedida;

    @NotNull(message = "A quantidade não pode ser nula.")
    @Column(nullable = false)
    private Integer quantidade;

    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @OneToMany(mappedBy = "produto")
    private List<ItemProduto> itensProduto;

    @ManyToMany
    @JoinTable(
            name = "fornecedor_produto",
            joinColumns = @JoinColumn(name = "id_produto"),
            inverseJoinColumns = @JoinColumn(name = "id_fornecedor")
    )
    private List<Fornecedor> fornecedores;

    @ManyToMany(mappedBy = "produtosAlerta")
    private List<Alerta> alertas;
}