package br.com.smartvalidity.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import br.com.smartvalidity.model.enums.PerfilAcesso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table
@Getter
@Setter
@ToString(exclude = "alertas") // Excluir alertas do toString para evitar problemas de lazy loading
@EqualsAndHashCode(exclude = "alertas") // Excluir alertas do equals/hashCode para evitar problemas de lazy loading
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    private PerfilAcesso perfilAcesso;

    @NotBlank(message = "O nome não pode ser vazio ou apenas espaços em branco.")
    private String nome;

    @Email
    @NotBlank(message = "O e-mail não pode ser vazio ou apenas espaços em branco.")
    private String email;

    @NotBlank(message = "A senha não pode ser vazio ou apenas espaços em branco.")
    @Column(length = 4000)
    private String senha;

    @NotBlank(message = "O cargo não pode ser vazio ou apenas espaços em branco.")
    private String cargo;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    //@JsonIgnore
    private Empresa empresa;

    @ManyToMany(mappedBy = "usuariosAlerta")
    private Set<Alerta> alertas;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> list = new ArrayList<>();
        if (perfilAcesso != null) {
            list.add(new SimpleGrantedAuthority(perfilAcesso.toString()));
        }
        return list;
    }


    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email;
    }
}
