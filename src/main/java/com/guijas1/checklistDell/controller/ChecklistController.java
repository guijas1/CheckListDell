package com.guijas1.checklistDell.controller;

import com.guijas1.checklistDell.entity.Checklist;
import com.guijas1.checklistDell.service.ChecklistService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;

@Controller
public class ChecklistController {

    private final ChecklistService checklistService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public ChecklistController(ChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @GetMapping("/checklist") // usado para formulário de novo checklist
    public String exibirFormulario(Model model) {
        model.addAttribute("checklist", new Checklist());
        return "checklist-form";
    }

    @PostMapping("/checklist")
    public String salvarChecklist(@ModelAttribute Checklist checklist,
                                  @RequestParam("foto") MultipartFile foto) throws IOException {

        if (!foto.isEmpty()) {
            String nomeArquivo = System.currentTimeMillis() + "_" + foto.getOriginalFilename();
            Path caminho = Paths.get(uploadDir, nomeArquivo);
            Files.createDirectories(caminho.getParent());
            Files.write(caminho, foto.getBytes());
            checklist.setFotoPath(caminho.toString());
        }

        checklistService.salvarChecklist(checklist);
        return "redirect:/checklist?sucesso";
    }
    @GetMapping("/checklists") // usado para listar todos os checklists preenchidos
    public String listarChecklists(Model model) {
        model.addAttribute("checklists", checklistService.listarTodos());
        return "checklist-lista";
    }

    @GetMapping("/fotos/{nomeArquivo:.+}")
    @ResponseBody
    public ResponseEntity<Resource> servirFoto(@PathVariable String nomeArquivo) throws IOException {
        Path caminho = Paths.get(uploadDir).resolve(nomeArquivo);
        Resource recurso = new org.springframework.core.io.UrlResource(caminho.toUri());

        if (!recurso.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + recurso.getFilename() + "\"")
                .body(recurso);
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
        doc.add(new Paragraph(" ")); // espaço

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
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Observações:", labelFont));
        doc.add(new Paragraph(checklist.getObservacoes() != null ? checklist.getObservacoes() : "Sem observações.", textFont));

        // Se houver imagem, tente adicionar
        if (checklist.getFotoPath() != null) {
            try {
                Image foto = Image.getInstance(checklist.getFotoPath());
                foto.scaleToFit(400, 300);
                doc.add(new Paragraph(" "));
                doc.add(new Paragraph("Foto anexada:", labelFont));
                doc.add(foto);
            } catch (Exception e) {
                doc.add(new Paragraph("Erro ao carregar imagem: " + e.getMessage(), textFont));
            }
        }

        doc.close();
    }

    private String format(Boolean valor) {
        if (valor == null) return "Não informado";
        return valor ? "Sim" : "Não";
    }

}
