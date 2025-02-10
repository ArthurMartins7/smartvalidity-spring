package br.com.smartvalidity.model.seletor;

import br.com.smartvalidity.model.entity.Categoria;
import br.com.smartvalidity.model.entity.Corredor;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoriaSeletor extends BaseSeletor implements Specification<Categoria> {

    private String nome;
    private Corredor corredor;

    @Override
    public Predicate toPredicate(Root<Categoria> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();

        if (this.getNome() != null && !this.getNome().trim().isEmpty()) {
            predicates.add(cb.like(root.get("nome"), "%" + this.getNome() + "%"));
        }

        if (this.getCorredor() != null) {
            predicates.add(cb.equal(root.get("corredor"), this.getCorredor()));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
