package br.com.smartvalidity.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.EAN;

import java.util.Set;

@Entity
@Table
@Data
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "O campo 'código de barras' não pode ser vazio ou apenas espaços em branco.")
    @EAN
    private String codigoBarras;

    @NotBlank(message = "O campo 'descrição' não pode ser vazio ou apenas espaços em branco.")
    private String descricao;

    @NotBlank(message = "O campo 'marca' não pode ser vazio ou apenas espaços em branco.")
    private String marca;

    @NotBlank(message = "O campo 'unidade de medida' não pode ser vazio ou apenas espaços em branco.")
    private String unidadeMedida;

    private int quantidade;

    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @OneToMany(mappedBy = "produto")
    private Set<ItemProduto> itensProduto;

    @ManyToMany
    @JoinTable(name = "fornecedor_produto", joinColumns = @JoinColumn(name = "id_fornecedor"), inverseJoinColumns = @JoinColumn(name = "id_produto"))
    private Set<Fornecedor> fornecedores;
}
