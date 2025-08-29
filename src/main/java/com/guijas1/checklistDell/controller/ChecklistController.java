package com.guijas1.checklistDell.controller;

import com.guijas1.checklistDell.entity.Checklist;
import com.guijas1.checklistDell.service.ChecklistService;
import com.guijas1.checklistDell.service.S3Service;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Optional;

@Controller("/checklist")
public class ChecklistController {

    private static final Logger logger = LoggerFactory.getLogger(ChecklistController.class);

    private final ChecklistService checklistService;

    private final S3Service s3Service;

    public ChecklistController(ChecklistService checklistService, S3Service s3Service) {
        this.checklistService = checklistService;
        this.s3Service = s3Service;
    }

    @GetMapping("/checklist")
    public String exibirFormulario(Model model, @RequestParam(value = "sucesso", required = false) String sucesso,
                                   @RequestParam(value = "erro", required = false) String erro) {
        logger.info("🔁 Exibindo formulário. Sucesso? {} | Erro: {}", sucesso, erro);
        model.addAttribute("checklist", new Checklist());
        model.addAttribute("sucesso", sucesso != null);
        model.addAttribute("erro", erro);
        return "checklist-form";
    }

    @PostMapping("/checklist")
    public String salvarChecklist(@ModelAttribute Checklist checklist,
                                  @RequestParam("fotos") MultipartFile[] fotos
    ) {
        try {
            checklistService.salvarChecklist(checklist, fotos);
            return "redirect:/checklist?sucesso";
        } catch (Exception e) {
            logger.error("❌ Erro ao salvar checklist: {}", e.getMessage());
            return "redirect:/checklist?erro=upload";
        }
    }

    @GetMapping("/checklists")
    public String listarChecklists(Model model) {
        model.addAttribute("checklists", checklistService.listarTodos());
        return "checklist-lista";
    }

    @GetMapping("/checklists/{id}")
    public String exibirDetalhes(@PathVariable Long id, Model model) {
        Optional<Checklist> checklist = checklistService.buscarPorId(id);
        if (checklist.isPresent()) {
            model.addAttribute("checklist", checklist.get());
            return "checklist-detalhes";
        } else {
            return "redirect:/checklists?erro=notfound";
        }
    }


    @GetMapping("/checklists/{id}/exportar")
    public void exportarChecklistPdf(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Optional<Checklist> opt = checklistService.buscarPorId(id);
        if (opt.isEmpty()) {
            response.sendRedirect("/checklists?erro");
            return;
        }

        Checklist checklist = opt.get();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=checklist_" + checklist.getId() + ".pdf");

        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, response.getOutputStream());
        doc.open();

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font labelFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font textFont = new Font(Font.HELVETICA, 12);

        doc.add(new Paragraph("Checklist Dell", titleFont));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("ID: " + checklist.getId(), textFont));
        doc.add(new Paragraph("Modelo: " + checklist.getModelo(), textFont));
        doc.add(new Paragraph("Patrimônio: " + checklist.getPatrimonio(), textFont));
        doc.add(new Paragraph("Service Tag: " + checklist.getServiceTag(), textFont));
        doc.add(new Paragraph("Data: " + checklist.getDataCriacao(), textFont));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Respostas do Checklist:", labelFont));
        doc.add(new Paragraph("Liga: " + format(checklist.getLiga()), textFont));
        doc.add(new Paragraph("Tela Funciona: " + format(checklist.getTelaFunciona()), textFont));
        doc.add(new Paragraph("Teclado Funciona: " + format(checklist.getTecladoFunciona()), textFont));
        doc.add(new Paragraph("Wi-Fi Funciona: " + format(checklist.getWifiFunciona()), textFont));
        doc.add(new Paragraph("Carcaça sem avarias: " + format(checklist.getCarcaca()), textFont));

        doc.add(new Paragraph("Observações:", labelFont));
        doc.add(new Paragraph(checklist.getObservacoes() != null ? checklist.getObservacoes() : "Sem observações.", textFont));

        if (checklist.getFotoPath() != null && !checklist.getFotoPath().isEmpty()) {
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Fotos anexadas:", labelFont));
            for (String path : checklist.getFotoPath()) {
                try {
                    Image foto = Image.getInstance(path);
                    foto.scaleToFit(400, 300);
                    foto.setSpacingBefore(10);
                    doc.add(foto);
                } catch (Exception e) {
                    doc.add(new Paragraph("Erro ao carregar imagem: " + e.getMessage(), textFont));
                }
            }
        }

        doc.close();
    }

    private String format(Boolean valor) {
        if (valor == null) return "Não informado";
        return valor ? "Sim" : "Não";
    }

    @PostMapping("/checklists/{id}/deletar")
    public String deletarChecklist(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            checklistService.excluirChecklist(id);
            redirectAttributes.addFlashAttribute("mensagem", "Checklist excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir checklist.");
        }
        return "redirect:/checklists";
    }

}
