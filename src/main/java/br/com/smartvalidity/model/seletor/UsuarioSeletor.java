package br.com.smartvalidity.model.seletor;

import br.com.smartvalidity.model.entity.Usuario;
import br.com.smartvalidity.model.enums.PerfilAcesso;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Data
public class UsuarioSeletor extends BaseSeletor implements Specification<Usuario> {

    private PerfilAcesso perfilAcesso;
    private String cpf;
    private String nome;
    private String email;

    @Override
    public Predicate toPredicate(Root<Usuario> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();

        if(this.getPerfilAcesso() != null){
            predicates.add(cb.equal(root.get("perfilAcesso"), this.getPerfilAcesso()));
        }

        if (this.getCpf() != null && !this.getCpf().trim().isEmpty()) {
            predicates.add(cb.like(root.get("cpf"), "%" + this.getCpf() + "%"));
        }

        if (this.getNome() != null && !this.getNome().trim().isEmpty()) {
            predicates.add(cb.like(root.get("nome"), "%" + this.getNome() + "%"));
        }

        if (this.getEmail() != null && !this.getEmail().trim().isEmpty()) {
            predicates.add(cb.like(root.get("email"), "%" + this.getEmail() + "%"));
        }

        //aplicarFiltroPeriodo(root, cb, predicates, this.getDataInicialCriacao(), this.getDataFinalCriacao(), "createdAt");

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
