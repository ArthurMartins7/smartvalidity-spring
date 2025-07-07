package br.com.smartvalidity.model.entity;

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
import lombok.Data;

@Entity
@Table(name = "alerta")
@Data
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAlerta tipo;

    @Column(name = "data_hora_disparo")
    private LocalDateTime dataHoraDisparo;

    @Column(nullable = false)
    private Boolean excluido = false;

    @Column(name = "data_hora_criacao", nullable = false)
    private LocalDateTime dataHoraCriacao;

    // Campos de recorrência removidos - alertas personalizados são mais simples

    @ManyToOne
    @JoinColumn(name = "id_usuario_criador")
    private Usuario usuarioCriador;

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
    @JoinColumn(name = "id_item_produto")
    private ItemProduto itemProduto;

    @PrePersist
    protected void onCreate() {
        if (dataHoraCriacao == null) {
            dataHoraCriacao = LocalDateTime.now();
        }
        
        // Lógica de recorrência removida - alertas personalizados são mais simples
    }
}
