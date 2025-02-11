package br.com.smartvalidity.model.seletor;

import br.com.smartvalidity.model.entity.Endereco;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Data
public class EnderecoSeletor extends BaseSeletor implements Specification<Endereco> {

    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String pais;
    private String cep;

    @Override
    public Predicate toPredicate(Root<Endereco> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();

        if (this.getLogradouro() != null && !this.getLogradouro().trim().isEmpty()) {
            predicates.add(cb.like(root.get("logradouro"), "%" + this.getLogradouro() + "%"));
        }

        if (this.getNumero() != null) {
            predicates.add(cb.equal(root.get("numero"), this.getNumero()));
        }

        if (this.getComplemento() != null && !this.getComplemento().trim().isEmpty()) {
            predicates.add(cb.like(root.get("complemento"), "%" + this.getComplemento() + "%"));
        }

        if (this.getBairro() != null && !this.getBairro().trim().isEmpty()) {
            predicates.add(cb.like(root.get("bairro"), "%" + this.getBairro() + "%"));
        }

        if (this.getCidade() != null && !this.getCidade().trim().isEmpty()) {
            predicates.add(cb.like(root.get("cidade"), "%" + this.getCidade() + "%"));
        }

        if (this.getEstado() != null && !this.getEstado().trim().isEmpty()) {
            predicates.add(cb.like(root.get("estado"), "%" + this.getEstado() + "%"));
        }

        if (this.getPais() != null && !this.getPais().trim().isEmpty()) {
            predicates.add(cb.like(root.get("pais"), "%" + this.getPais() + "%"));
        }

        if (this.getCep() != null && !this.getCep().trim().isEmpty()) {
            predicates.add(cb.like(root.get("cep"), "%" + this.getCep() + "%"));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}



