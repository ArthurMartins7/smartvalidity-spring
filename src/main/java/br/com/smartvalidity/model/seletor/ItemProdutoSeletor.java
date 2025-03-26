package br.com.smartvalidity.model.seletor;

import br.com.smartvalidity.model.entity.Produto;
import br.com.smartvalidity.model.entity.Categoria;
import br.com.smartvalidity.model.entity.ItemProduto;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ItemProdutoSeletor extends BaseSeletor implements Specification<ItemProduto> {

    private String lote;
    private Integer quantidadeMinima;
    private Integer quantidadeMaxima;
    private Double precoVendaMinimo;
    private Double precoVendaMaximo;
    private LocalDateTime dataFabricacaoInicio;
    private LocalDateTime dataFabricacaoFim;
    private LocalDateTime dataVencimentoInicio;
    private LocalDateTime dataVencimentoFim;
    private LocalDateTime dataRecebimentoInicio;
    private LocalDateTime dataRecebimentoFim;
    private Produto produto;

    @Override
    public Predicate toPredicate(Root<ItemProduto> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();

        if (this.getLote() != null && !this.getLote().trim().isEmpty()) {
            predicates.add(cb.like(root.get("lote"), "%" + this.getLote() + "%"));
        }

        if (this.getQuantidadeMinima() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("quantidade"), this.getQuantidadeMinima()));
        }

        if (this.getQuantidadeMaxima() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("quantidade"), this.getQuantidadeMaxima()));
        }

        if (this.getPrecoVendaMinimo() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("precoVenda"), this.getPrecoVendaMinimo()));
        }

        if (this.getPrecoVendaMaximo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("precoVenda"), this.getPrecoVendaMaximo()));
        }

        if (this.getDataFabricacaoInicio() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("dataFabricacao"), this.getDataFabricacaoInicio()));
        }

        if (this.getDataFabricacaoFim() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("dataFabricacao"), this.getDataFabricacaoFim()));
        }

        if (this.getDataVencimentoInicio() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("dataVencimento"), this.getDataVencimentoInicio()));
        }

        if (this.getDataVencimentoFim() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("dataVencimento"), this.getDataVencimentoFim()));
        }

        if (this.getDataRecebimentoInicio() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("dataRecebimento"), this.getDataRecebimentoInicio()));
        }

        if (this.getDataRecebimentoFim() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("dataRecebimento"), this.getDataRecebimentoFim()));
        }

        if (this.getProduto() != null) {
            predicates.add(cb.equal(root.get("produto"), this.getProduto()));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
