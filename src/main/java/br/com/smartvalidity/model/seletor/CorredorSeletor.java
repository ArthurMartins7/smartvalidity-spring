package br.com.smartvalidity.model.seletor;

import br.com.smartvalidity.model.entity.Corredor;
import br.com.smartvalidity.model.entity.Usuario;
import jakarta.persistence.criteria.*;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class CorredorSeletor extends BaseSeletor implements Specification<Corredor> {

    private String nome;
    private Set<Usuario> responsaveis;


    @Override
    public Predicate toPredicate(Root<Corredor> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (this.getNome() != null && !this.getNome().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("nome")), "%" + this.getNome().toLowerCase() + "%"));
        }

        if (this.getResponsaveis() != null && !this.getResponsaveis().isEmpty()) {
            Join<Object, Object> join = root.join("responsaveis");

            CriteriaBuilder.In<Object> inClause = cb.in(join.get("id"));
            for (Usuario usuario : this.getResponsaveis()) {
                inClause.value(usuario.getId());
            }

            predicates.add(inClause);

            // 👇 Isso aqui evita duplicatas e garante o join correto
            query.distinct(true);
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }


}
