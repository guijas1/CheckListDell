package com.guijas1.checklistDell.entity;


import jakarta.persistence.*;

import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Checklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String modelo;
    private String patrimonio;
    private String serviceTag;

    private Boolean liga;
    private Boolean telaFunciona;
    private Boolean tecladoFunciona;
    private Boolean wifiFunciona;
    private Boolean carcaca;

    @Lob
    private String observacoes;

    private String fotoPath; // caminho da foto no disco

    private LocalDateTime dataCriacao = LocalDateTime.now();

    // Getters e Setters
}
