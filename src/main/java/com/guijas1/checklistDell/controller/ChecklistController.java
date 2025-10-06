package com.guijas1.checklistDell.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.guijas1.checklistDell.entity.Checklist;
import com.guijas1.checklistDell.service.ChecklistService;
import com.guijas1.checklistDell.service.QRCodeUtil;
import com.guijas1.checklistDell.service.S3Service;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    public String exibirFormulario(Model model,
                                   @RequestParam(value = "sucesso", required = false) String sucesso,
                                   @RequestParam(value = "erro", required = false) String erro) {
        logger.info("🔁 Exibindo formulário. Sucesso? {} | Erro: {}", sucesso, erro);

        model.addAttribute("checklist", new Checklist());
        model.addAttribute("sucesso", sucesso != null);
        model.addAttribute("erro", erro);


        List<String> modelos = Arrays.asList(
                "LATITUDE 5410",
                "LATITUDE 5420",
                "LATITUDE 5480",
                "LATITUDE 5270",
                "LATITUDE 5250",
                "LENOVO X230",
                "PRECISION 3561"

        );
        model.addAttribute("modelos", modelos);
        List<String> saudeBateria = Arrays.asList(
                "Excellent",
                "Good",
                "Fair",
                "Poor"
        );

        model.addAttribute("saudeBateria", saudeBateria);
        List<String> localizacao = Arrays.asList("Armário 22", "Armário 21", "Fora do estoque");
        model.addAttribute("localizacao", localizacao);

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
        doc.add(new Paragraph("Chamado OTRS:", labelFont));
        doc.add(new Paragraph(checklist.getChamadoOTRS() != null ? checklist.getChamadoOTRS() : "Sem chamados atribuidos ainda.", textFont));
        doc.add(new Paragraph("Histórico:", labelFont));
        doc.add(new Paragraph(checklist.getHistoricoNotebook() != null ? checklist.getHistoricoNotebook() : "Sem histórico atribuido ainda.", textFont));

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

    @GetMapping("/checklists")
    public String listarChecklists(
            @RequestParam(value = "campoBusca", required = false) String campoBusca,
            @RequestParam(value = "modelo", required = false) String modelo,
            @RequestParam(value = "patrimonio", required = false) String patrimonio,
            @RequestParam(value = "origem_notebook", required = false) String origem_notebook,
            @RequestParam(value = "localizacao", required = false) String localizacao,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<Checklist> pagina;

        if ("modelo".equals(campoBusca) && modelo != null && !modelo.isEmpty()) {
            pagina = checklistService.buscarPorModelo(modelo, page, size);
        } else if ("patrimonio".equals(campoBusca) && patrimonio != null && !patrimonio.isEmpty()) {
            pagina = checklistService.buscarPorPatrimonio(patrimonio, page, size);
        } else if ("origem_notebook".equals(campoBusca) && origem_notebook != null && !origem_notebook.isEmpty()) {
            pagina = checklistService.buscarPorOrigem(origem_notebook, page, size);
        } else if ("localizacao".equals(campoBusca) && localizacao != null && !localizacao.isEmpty()) {
            pagina = checklistService.buscarPorLocalizacao(localizacao, page, size);
        } else {
            pagina = checklistService.listarPaginado(page, size);
        }

        model.addAttribute("pagina", pagina);
        model.addAttribute("checklists", pagina.getContent());
        model.addAttribute("paginaAtual", page);

        return "checklist-lista";
    }
    @PostMapping("/checklists/{id}/atualizar")
    public String atualizarChecklist(@PathVariable Long id,
                                     @RequestParam(required = false) String chamado_otrs,
                                     @RequestParam(required = false) String origem_notebook,
                                     @RequestParam(required = false, name = "localizacao") String localizacao) throws IOException {
        Optional<Checklist> checklistOpt = checklistService.buscarPorId(id);
        if (checklistOpt.isPresent()) {
            Checklist checklist = checklistOpt.get();
            checklist.setChamadoOTRS(chamado_otrs);
            checklist.setHistoricoNotebook(origem_notebook);
            checklist.setLocalizacao(localizacao); // ✅ Agora salva também a localização
            checklistService.salvarChecklist(checklist, new MultipartFile[0]);
            return "redirect:/checklists/" + id;
        } else {
            return "redirect:/checklists?erro=notfound";
        }
    }

    //ENDPOINT DE LEITURA DE QR CODE PARA REDIRECIONAMENTO PARA LOCALIZAÇÃO.
    @GetMapping("/checklists/localizacao/{codigo}")
    public String buscarPorLocalizacao(@PathVariable String codigo, Model model) {
        List<Checklist> notebooks = checklistService.buscarPorLocalizacao(codigo);
        model.addAttribute("checklists", notebooks);
        model.addAttribute("localizacao", codigo);
        return "checklists-por-localizacao";
    }

    @GetMapping("/checklists/qrcode")
    public String paginaQrCode() {
        return "buscar-por-qrcode.html"; // nome do template Thymeleaf
    }
    @GetMapping("/checklists/qrcode/gerar/{localizacao}")
    public void gerarQrCode(@PathVariable String localizacao, HttpServletResponse response) throws IOException {
        try {
            int width = 250;
            int height = 250;

            // Corrigido: espaço deve virar %20, não "+"
            String encoded = URLEncoder.encode(localizacao, StandardCharsets.UTF_8)
                    .replace("+", "%20");

            String conteudo = "http://10.41.5.131:8080/checklists/localizacao/" + encoded;

            BitMatrix matrix = new MultiFormatWriter()
                    .encode(conteudo, BarcodeFormat.QR_CODE, width, height);

            response.setContentType("image/png");
            try (ServletOutputStream outputStream = response.getOutputStream()) {
                MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao gerar QRCode");
        }
    }



    @GetMapping("/checklists/qrcodes")
    public String gerarQRCodes(Model model) {
        // Busca todas as localizações distintas
        List<String> localizacoes = checklistService.listarLocalizacoes();

        model.addAttribute("localizacoes", localizacoes);
        return "qrcodes-lista"; // Thymeleaf
    }

}
