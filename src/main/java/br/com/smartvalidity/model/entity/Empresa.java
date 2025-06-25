package br.com.smartvalidity.model.entity;

import java.util.List;

import org.hibernate.validator.constraints.br.CNPJ;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table
@Data
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @CNPJ
    @NotBlank(message = "O CNPJ não pode ser vazio ou apenas espaços em branco.")
    @Column(unique = true, nullable = false)
    private String cnpj;

    @NotBlank(message = "A raão social não pode ser vazia ou apenas espaços em branco.")
    private String razaoSocial;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "assinante_id", referencedColumnName = "id", nullable = false, unique = true)
    private Usuario usuarioAssinante;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Usuario> usuarios;

}
