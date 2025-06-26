package br.com.smartvalidity.model.seletor;

import br.com.smartvalidity.model.entity.Endereco;
import br.com.smartvalidity.model.entity.Fornecedor;
import br.com.smartvalidity.model.entity.Produto;
import jakarta.persistence.criteria.*;
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
    private String descricaoProduto;



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

        if (this.getDescricaoProduto() != null && !this.getDescricaoProduto().trim().isEmpty()) {
            Join<Fornecedor, Produto> joinProduto = root.join("produtos");
            predicates.add(cb.like(cb.lower(joinProduto.get("descricao")), "%"
                    + this.getDescricaoProduto().toLowerCase() + "%"));
        }


        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
