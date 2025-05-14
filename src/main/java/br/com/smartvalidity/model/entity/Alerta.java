package br.com.smartvalidity.model.entity;

import br.com.smartvalidity.model.enums.FrequenciaDisparo;
import br.com.smartvalidity.model.enums.SituacaoValidade;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table
@Data
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "O campo 'Título' não pode ser vazio ou apenas espaços em branco.")
    private String titulo;

    @NotBlank(message = "O campo 'Descrição' não pode ser vazio ou apenas espaços em branco.")
    private String descricao;

    private LocalDateTime dataHoraDisparo;

    private boolean isDisparoRecorrente;

    @Enumerated(EnumType.STRING)
    private FrequenciaDisparo frequenciaDisparo;

    @ManyToMany
    @JoinTable(
            name = "alerta_usuario",
            joinColumns = @JoinColumn(name = "id_alerta"),
            inverseJoinColumns = @JoinColumn(name = "id_usuario")
    )
    private Set<Usuario> usuariosAlerta;

    @ManyToMany
    @JoinTable(
            name = "alerta_produto",
            joinColumns = @JoinColumn(name = "id_alerta"),
            inverseJoinColumns = @JoinColumn(name = "id_produto")
    )
    private Set<Produto> produtosAlerta;

}
