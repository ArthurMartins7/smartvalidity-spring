package br.com.smartvalidity.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Entity
@Table
@Data
public class Corredor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "O campo 'nome' não pode ser vazio ou apenas espaços em branco.")
    private String nome;

    @Column(columnDefinition = "LONGTEXT")
    private String imagemEmBase64;

    @ManyToMany
    @JoinTable(name = "corredor_usuario", joinColumns = @JoinColumn(name = "id_usuario"), inverseJoinColumns = @JoinColumn(name = "id_corredor"))
    private List<Usuario> responsaveis;

    @OneToMany(mappedBy = "corredor")
    private List<Categoria> categorias;
}
