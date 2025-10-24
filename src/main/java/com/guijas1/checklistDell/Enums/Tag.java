package com.guijas1.checklistDell.Enums;

public enum Tag {
    GA("Gestão de Ativos"),
    ROLLOUT("Rollout");

    private final String label;

    Tag(String label) {
        this.label = label;
    }

    /** Rótulo para exibição (Thymeleaf) */
    public String getLabel() {
        return label;
    }
}
