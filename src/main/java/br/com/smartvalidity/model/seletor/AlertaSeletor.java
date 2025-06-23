package br.com.smartvalidity.model.seletor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import br.com.smartvalidity.model.entity.Alerta;
import br.com.smartvalidity.model.enums.TipoAlerta;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Data;

@Data
public class AlertaSeletor extends BaseSeletor implements Specification<Alerta> {

    private String titulo;
    private TipoAlerta tipo;
    private Boolean ativo;
    private Boolean recorrente;
    private LocalDateTime dataInicialDisparo;
    private LocalDateTime dataFinalDisparo;
    private String usuarioCriador;

    @Override
    public Predicate toPredicate(Root<Alerta> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (stringValida(titulo)) {
            predicates.add(cb.like(cb.lower(root.get("titulo")), 
                "%" + titulo.toLowerCase() + "%"));
        }

        if (tipo != null) {
            predicates.add(cb.equal(root.get("tipo"), tipo));
        }

        if (ativo != null) {
            predicates.add(cb.equal(root.get("ativo"), ativo));
        }

        if (recorrente != null) {
            predicates.add(cb.equal(root.get("recorrente"), recorrente));
        }

        if (dataInicialDisparo != null || dataFinalDisparo != null) {
            aplicarFiltroPeriodoDateTime(root, cb, predicates, 
                dataInicialDisparo, dataFinalDisparo, "dataHoraDisparo");
        }

        if (stringValida(usuarioCriador)) {
            predicates.add(cb.like(cb.lower(root.get("usuarioCriador").get("nome")), 
                "%" + usuarioCriador.toLowerCase() + "%"));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }

    private static void aplicarFiltroPeriodoDateTime(Root<Alerta> root,
                                                    CriteriaBuilder cb, List<Predicate> predicates,
                                                    LocalDateTime dataInicial, LocalDateTime dataFinal, 
                                                    String nomeAtributo) {
        if (dataInicial != null && dataFinal != null) {
            predicates.add(cb.between(root.get(nomeAtributo), dataInicial, dataFinal));
        } else if (dataInicial != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(nomeAtributo), dataInicial));
        } else if (dataFinal != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(nomeAtributo), dataFinal));
        }
    }
} 