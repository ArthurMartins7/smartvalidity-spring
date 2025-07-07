package br.com.smartvalidity.model.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.br.CNPJ;

import java.util.List;

@Entity
@Table
@Data
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "O campo 'nome' não pode ser vazio ou apenas espaços em branco.")
    private String nome;

    private String telefone;

    @CNPJ
    @NotBlank(message = "O campo 'cnpj' não pode ser vazio ou apenas espaços em branco.")
    private String cnpj;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_endereco", referencedColumnName = "id")
    private Endereco endereco;

    @ManyToMany(mappedBy = "fornecedores", fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Produto> produtos;
}