package br.com.smartvalidity.model.entity;

import java.time.LocalDateTime;
import java.util.Set;

import br.com.smartvalidity.model.enums.FrequenciaDisparo;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAlerta tipo;

    private LocalDateTime dataHoraCriacao;

    private LocalDateTime dataHoraDisparo;

    private Integer diasAntecedencia;

    @Column(nullable = false)
    private Boolean ativo = true;

    private Boolean recorrente = false;

    private String configuracaoRecorrencia;

    @Column(nullable = false)
    private Boolean lido = false;

    // Campos para compatibilidade com sistema antigo
    private boolean isDisparoRecorrente;

    @Enumerated(EnumType.STRING)
    private FrequenciaDisparo frequenciaDisparo;

    // Relacionamento com usuário criador (para alertas personalizados)
    @ManyToOne
    @JoinColumn(name = "id_usuario_criador")
    private Usuario usuarioCriador;

    // Usuários que devem receber este alerta
    @ManyToMany
    @JoinTable(
            name = "alerta_usuario",
            joinColumns = @JoinColumn(name = "id_alerta", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "id_usuario", referencedColumnName = "id")
    )
    private Set<Usuario> usuariosAlerta;

    // Relacionamento com produtos (para alertas personalizados)
    @ManyToMany
    @JoinTable(
            name = "alerta_produto",
            joinColumns = @JoinColumn(name = "id_alerta", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "id_produto", referencedColumnName = "id")
    )
    private Set<Produto> produtosAlerta;

    // Relacionamento com item-produto específico (para alertas automáticos)
    @ManyToOne
    @JoinColumn(name = "id_item_produto")
    private ItemProduto itemProduto;

    @PrePersist
    protected void onCreate() {
        if (dataHoraCriacao == null) {
            dataHoraCriacao = LocalDateTime.now();
        }
    }
}
