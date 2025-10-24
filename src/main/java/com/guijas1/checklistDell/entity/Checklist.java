package com.guijas1.checklistDell.entity;

import com.guijas1.checklistDell.Enums.Localides;
import com.guijas1.checklistDell.Enums.StatusChamadoDell;
import com.guijas1.checklistDell.Enums.Tag;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    // 📋 Identificação
    private String modelo;
    private String patrimonio;

    @Column(name = "service_tag")
    private String serviceTag;

    // 💻 Funcionamento geral
    private Boolean liga;

    @Column(name = "tela_funciona")
    private Boolean telaFunciona;

    @Column(name = "teclado_funciona")
    private Boolean tecladoFunciona;

    @Column(name = "wifi_funciona")
    private Boolean wifiFunciona;

    private Boolean carcaca;

    // 🔋 Estado físico adicional
    @Column(name = "borracha_protecao_ok")
    private Boolean borrachaProtecaoOk;

    @Column(name = "borracha_protecao_faltantes")
    private Integer borrachaProtecaoFaltantes;

    @Column(name = "parafusos_ok")
    private Boolean parafusosOk;

    @Column(name = "parafusos_faltantes")
    private Integer parafusosFaltantes;

    @Column(name = "tampa_inferior_ok")
    private Boolean tampaInferiorOk;

    @Column(name = "tampa_inferior_faltantes")
    private Integer tampaInferiorFaltantes;

    @Column(name = "portas_funcionando")
    private Boolean portasFuncionando;

    @Column(name = "dobradicas_ok")
    private Boolean dobradicasOk;

    @Column(name = "manchas_tela")
    private Boolean manchasTela;

    @Column(name = "trincos_tela")
    private Boolean trincosTela;

    @Column(name = "camera_funciona")
    private Boolean cameraFunciona;

    @Column(name = "microfone_funciona")
    private Boolean microfoneFunciona;

    @Column(name = "alto_falante_funciona")
    private Boolean altoFalanteFunciona;

    @Column(name = "bateria")
    private String bateria;


    // 🗃️ Informações de chamado
    @Column(name = "chamado_dell")
    private String chamadoDell;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_chamado_dell")
    private StatusChamadoDell statusChamadoDell;

    @Column(name = "chamado_otrs")
    private String chamadoOTRS;

    @Column(name = "status_interno")
    private String statusInterno;


    // 📦 Origem e histórico
    @Column(name = "origem_notebook")
    private String historicoNotebook;

    @Column(name = "localizaçao")
    private String localizacao;

    @Column(name = "demanda")
    private String demanda;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag")
    private Tag tag;

    @Column(name = "de_para")
    private String depara;

    @Column(name = "portas_descricao")
    private String portasDescricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "localidade", length = 3)
    private Localides localidade;


    // 🖼️ Fotos e observações
    @Lob
    private String observacoes;

    @ElementCollection
    @CollectionTable(name = "checklist_fotos", joinColumns = @JoinColumn(name = "checklist_id"))
    @Column(name = "foto_path")
    private List<String> fotoPath;

    @Transient
    private transient List<MultipartFile> fotos;


    // 🕒 Controle de data
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    public String getDataCriacaoFormatada() {
        return dataCriacao != null
                ? dataCriacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "—";
    }

    public List<String> getFotoPath() {
        return this.fotoPath != null ? this.fotoPath : List.of();
    }
}
