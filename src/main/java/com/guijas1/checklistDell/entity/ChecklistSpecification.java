package com.guijas1.checklistDell.entity;

import org.springframework.data.jpa.domain.Specification;

public class ChecklistSpecification {

    public static Specification<Checklist> comFiltros(
            String modelo,
            String patrimonio,
            String historicoNotebook,
            String localizacao,
            Boolean carcaca,
            Boolean tecladoRuim) {

        Specification<Checklist> spec = (root, query, cb) -> cb.conjunction();

        if (modelo != null && !modelo.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("modelo")), "%" + modelo.toLowerCase() + "%"));
        }

        if (patrimonio != null && !patrimonio.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("patrimonio")), "%" + patrimonio.toLowerCase() + "%"));
        }

        if (historicoNotebook != null && !historicoNotebook.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("historicoNotebook")), "%" + historicoNotebook.toLowerCase() + "%"));
        }

        if (localizacao != null && !localizacao.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("localizacao")), "%" + localizacao.toLowerCase() + "%"));
        }

        if (carcaca != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("carcaca"), carcaca));
        }

        // 🔧 Corrigido: o nome do campo é tecladoFunciona na entidade
        if (tecladoRuim != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("tecladoFunciona"), tecladoRuim));
        }

        return spec;
    }
}
