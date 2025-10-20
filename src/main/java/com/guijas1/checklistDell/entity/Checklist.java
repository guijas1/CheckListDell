package com.guijas1.checklistDell.entity;

import com.guijas1.checklistDell.Enums.StatusChamadoDell;
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

    @Column(name = "chamado_dell")
    private String chamadoDell;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_chamado_dell")
    private StatusChamadoDell statusChamadoDell;

    @Column(name = "status_interno")
    private String statusInterno;

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
    @Column(name = "chamado_otrs")
    private String chamadoOTRS;

    @Column(name = "origem_notebook")
    private String historicoNotebook;

    @Column(name = "localizaçao")
    private String localizacao;

    @Column(name = "bateria")
    private String bateria;


}
