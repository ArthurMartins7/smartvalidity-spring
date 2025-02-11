package br.com.smartvalidity.model.seletor;

import br.com.smartvalidity.model.entity.Corredor;
import br.com.smartvalidity.model.entity.Usuario;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
            predicates.add(cb.like(root.get("nome"), "%" + this.getNome() + "%"));
        }

        if (this.getResponsaveis() != null) {
            predicates.add(cb.equal(root.get("responsaveis"), this.getResponsaveis() + "%"));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
