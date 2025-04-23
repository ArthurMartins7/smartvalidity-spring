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
    private String responsavel;
    private String responsavelId;
    private Set<Usuario> responsaveis;

    @Override
    public Predicate toPredicate(Root<Corredor> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // Filtro por nome
        if (this.getNome() != null && !this.getNome().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("nome")), "%" + this.getNome().toLowerCase() + "%"));
        }

        // Filtro por responsável (usando responsavelId)
        if (this.getResponsavelId() != null && !this.getResponsavelId().trim().isEmpty()) {
            Join<Corredor, Usuario> responsaveisJoin = root.join("responsaveis");
            predicates.add(cb.equal(responsaveisJoin.get("id"), this.getResponsavelId()));
            query.distinct(true);
        }

        // Filtro por conjunto de responsáveis (mantido para compatibilidade)
        if (this.getResponsaveis() != null && !this.getResponsaveis().isEmpty()) {
            Join<Corredor, Usuario> responsaveisJoin = root.join("responsaveis");
            CriteriaBuilder.In<Object> inClause = cb.in(responsaveisJoin.get("id"));
            for (Usuario usuario : this.getResponsaveis()) {
                inClause.value(usuario.getId());
            }
            predicates.add(inClause);
            query.distinct(true);
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}