package br.com.smartvalidity.model.entity;

<<<<<<< HEAD
=======
import br.com.smartvalidity.model.enums.FrequenciaDisparo;
import br.com.smartvalidity.model.enums.SituacaoValidade;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

>>>>>>> ac60f2e9298f0c29c567180cb212ef149affd74d
import java.time.LocalDateTime;
import java.util.Set;

import br.com.smartvalidity.model.enums.TipoAlerta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "alerta")
@Data
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "O campo 'Título' não pode ser vazio ou apenas espaços em branco.")
    private String titulo;

    @NotBlank(message = "O campo 'Descrição' não pode ser vazio ou apenas espaços em branco.")
    private String descricao;

    @Column(name = "data_hora_disparo", nullable = false)
    private LocalDateTime dataHoraDisparo;

<<<<<<< HEAD
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAlerta tipo;

    @Column(name = "dias_antecedencia")
    private Integer diasAntecedencia;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(nullable = false)
    private Boolean recorrente = false;

    @Column(name = "configuracao_recorrencia")
    private String configuracaoRecorrencia; // Ex: "DIARIO", "SEMANAL", etc.

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;
=======
    private boolean isDisparoRecorrente;

    @Enumerated(EnumType.STRING)
    private FrequenciaDisparo frequenciaDisparo;
>>>>>>> ac60f2e9298f0c29c567180cb212ef149affd74d

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

    @ManyToOne
    @JoinColumn(name = "id_usuario_criador")
    private Usuario usuarioCriador;

    @PrePersist
    protected void onCreate() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }
}
