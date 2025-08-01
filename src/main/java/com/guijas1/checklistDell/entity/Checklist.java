package com.guijas1.checklistDell.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Data
public class Checklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String modelo;
    private String patrimonio;

    @Column(name = "service_tag")
    private String serviceTag;

    private Boolean liga;

    @Column(name = "tela_funciona")
    private Boolean telaFunciona;

    @Column(name = "teclado_funciona")
    private Boolean tecladoFunciona;

    @Column(name = "wifi_funciona")
    private Boolean wifiFunciona;

    private Boolean carcaca;

    @Lob
    private String observacoes;

    @ElementCollection
    @CollectionTable(name = "checklist_fotos", joinColumns = @JoinColumn(name = "checklist_id"))
    @Column(name = "foto_path")
    private List<String> fotoPath;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Transient
    private transient List<MultipartFile> fotos;

    public String getDataCriacaoFormatada() {
        return dataCriacao != null
                ? dataCriacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "—";
    }
    public List<String> getFotoPath() {
        return this.fotoPath != null ? this.fotoPath : List.of();
    }
}
