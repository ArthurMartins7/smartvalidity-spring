package br.com.smartvalidity.model.entity;

import br.com.smartvalidity.model.enums.PerfilAcesso;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Table
@Data
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    private PerfilAcesso perfilAcesso;

    @CPF
    @NotBlank(message = "O CPF não pode ser vazio ou apenas espaços em branco.")
    @Column(unique = true, nullable = false)
    private String cpf;

    @NotBlank(message = "O nome não pode ser vazio ou apenas espaços em branco.")
    private String nome;

    @Email
    @NotBlank(message = "O e-mail não pode ser vazio ou apenas espaços em branco.")
    private String email;

    @NotBlank(message = "A senha não pode ser vazio ou apenas espaços em branco.")
    @Column(length = 4000)
    private String senha;

    @ManyToMany(mappedBy = "usuariosAlerta")
    private Set<Alerta> alertas;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();

        list.add(new SimpleGrantedAuthority(perfilAcesso.toString()));

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
