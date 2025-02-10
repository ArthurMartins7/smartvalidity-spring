package br.com.smartvalidity.model.seletor;

import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.entity.Categoria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProdutoSeletor extends BaseSeletor implements Specification<Produto> {

    private String codigoBarras;
    private String descricao;
    private String marca;
    private String unidadeMedida;
    private Integer quantidadeMinima;
    private Integer quantidadeMaxima;
    private Categoria categoria;

    @Override
    public Predicate toPredicate(Root<Produto> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();

        if (this.getCodigoBarras() != null && !this.getCodigoBarras().trim().isEmpty()) {
            predicates.add(cb.like(root.get("codigoBarras"), "%" + this.getCodigoBarras() + "%"));
        }

        if (this.getDescricao() != null && !this.getDescricao().trim().isEmpty()) {
            predicates.add(cb.like(root.get("descricao"), "%" + this.getDescricao() + "%"));
        }

        if (this.getMarca() != null && !this.getMarca().trim().isEmpty()) {
            predicates.add(cb.like(root.get("marca"), "%" + this.getMarca() + "%"));
        }

        if (this.getUnidadeMedida() != null && !this.getUnidadeMedida().trim().isEmpty()) {
            predicates.add(cb.like(root.get("unidadeMedida"), "%" + this.getUnidadeMedida() + "%"));
        }

        if (this.getQuantidadeMinima() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("quantidade"), this.getQuantidadeMinima()));
        }

        if (this.getQuantidadeMaxima() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("quantidade"), this.getQuantidadeMaxima()));
        }

        if (this.getCategoria() != null) {
            predicates.add(cb.equal(root.get("categoria"), this.getCategoria()));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
