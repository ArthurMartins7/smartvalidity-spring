package br.com.smartvalidity.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Set;


@Entity
@Table
@Data
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank(message = "O nome não pode ser vazio ou apenas espaços em branco.")
    private String nome;

    @ManyToOne
    @JoinColumn(name = "id_corredor")
    @JsonBackReference
    private Corredor corredor;

    @OneToMany(mappedBy = "categoria")
    @JsonIgnore
    private List<Produto> produtos;
}
