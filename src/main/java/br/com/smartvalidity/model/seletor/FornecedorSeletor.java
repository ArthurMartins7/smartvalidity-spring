package br.com.smartvalidity.model.seletor;

import br.com.smartvalidity.model.entity.Endereco;
import br.com.smartvalidity.model.entity.Fornecedor;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Data
public class FornecedorSeletor extends BaseSeletor implements Specification<Fornecedor> {

    private String nome;
    private String telefone;
    private String cnpj;
    private Endereco endereco;


    @Override
    public Predicate toPredicate(Root<Fornecedor> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();

        if (this.getNome() != null && !this.getNome().trim().isEmpty()) {
            predicates.add(cb.like(root.get("nome"), "%" + this.getNome() + "%"));
        }

        if (this.getTelefone() != null && !this.getTelefone().trim().isEmpty()) {
            predicates.add(cb.like(root.get("telefone"), "%" + this.getTelefone() + "%"));
        }

        if (this.getCnpj() != null && !this.getCnpj().trim().isEmpty()) {
            predicates.add(cb.like(root.get("cnpj"), "%" + this.getCnpj() + "%"));
        }

        if (this.getEndereco() != null) {
            predicates.add(cb.equal(root.get("endereco"), this.getEndereco()));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
