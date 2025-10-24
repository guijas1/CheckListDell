package com.guijas1.checklistDell.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.guijas1.checklistDell.Enums.Tag;
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

        // 🔹 Modelos disponíveis
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

        // 🔋 Saúde da bateria
        List<String> saudeBateria = Arrays.asList(
                "Excellent",
                "Good",
                "Fair",
                "Poor"
        );
        model.addAttribute("saudeBateria", saudeBateria);

        // 📍 Localizações conhecidas
        List<String> localizacao = Arrays.asList(
                "Armário 21",
                "Armário 22",
                "Fora do estoque",
                "Armário 04",
                "Armário 01",
                "Armário 02",
                "Armário 03",
                "Armário 05"
        );
        model.addAttribute("localizacao", localizacao);

        // 🏷️ Tipos de Tag (Rollout / Gestão de Ativos)
        List<String> tags = Arrays.asList("ROLLOUT", "GESTAO_ATIVOS");
        model.addAttribute("tags", tags);

        // 🔧 Campos adicionais (para exibir labels dinâmicos, se quiser iterar no HTML futuramente)
        List<String> verificacoesFisicas = Arrays.asList(
                "Borracha de proteção",
                "Parafusos",
                "Tampa inferior",
                "Portas",
                "Dobradiças",
                "Manchas na tela",
                "Trincos na tela",
                "Câmera",
                "Microfone",
                "Alto-falante"
        );
        model.addAttribute("verificacoesFisicas", verificacoesFisicas);

        List<String> localidades = Arrays.asList("RJ", "BSB", "SC", "PE");
        model.addAttribute("localidades", localidades);

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
            model.addAttribute("tags", Tag.values());
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
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) String patrimonio,
            @RequestParam(required = false) String historico_notebook,
            @RequestParam(required = false) String localizacao,
            @RequestParam(required = false) Boolean carcaca,
            @RequestParam(required = false, name = "tecladoFunciona") Boolean tecladoFunciona,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<Checklist> pagina = checklistService.buscarAvancadoPaginado(
                modelo, patrimonio, historico_notebook, localizacao, carcaca, tecladoFunciona, page, size);

        model.addAttribute("checklists", pagina.getContent());
        model.addAttribute("pagina", pagina);

        model.addAttribute("paramModelo", modelo);
        model.addAttribute("paramPatrimonio", patrimonio);
        model.addAttribute("paramHistoricoNotebook", historico_notebook);
        model.addAttribute("paramLocalizacao", localizacao);
        model.addAttribute("paramCarcaca", carcaca);
        model.addAttribute("paramTecladoFunciona", tecladoFunciona);

        return "checklist-lista";
    }


    @PostMapping("/checklists/{id}/atualizar")
    public String atualizarChecklist(@PathVariable Long id,
                                     @ModelAttribute Checklist checklistAtualizado) throws IOException {
        Optional<Checklist> checklistOpt = checklistService.buscarPorId(id);
        if (checklistOpt.isEmpty()) {
            return "redirect:/checklists?erro=notfound";
        }

        Checklist checklistExistente = checklistOpt.get();

        // 🧩 Identificação
        checklistExistente.setPatrimonio(checklistAtualizado.getPatrimonio());

        // 💻 Funcionamento geral
        checklistExistente.setLiga(checklistAtualizado.getLiga());
        checklistExistente.setTelaFunciona(checklistAtualizado.getTelaFunciona());
        checklistExistente.setTecladoFunciona(checklistAtualizado.getTecladoFunciona());
        checklistExistente.setWifiFunciona(checklistAtualizado.getWifiFunciona());
        checklistExistente.setCarcaca(checklistAtualizado.getCarcaca());

        // 🔋 Estado físico adicional
        checklistExistente.setBorrachaProtecaoOk(checklistAtualizado.getBorrachaProtecaoOk());
        checklistExistente.setBorrachaProtecaoFaltantes(checklistAtualizado.getBorrachaProtecaoFaltantes());
        checklistExistente.setParafusosOk(checklistAtualizado.getParafusosOk());
        checklistExistente.setParafusosFaltantes(checklistAtualizado.getParafusosFaltantes());
        checklistExistente.setTampaInferiorOk(checklistAtualizado.getTampaInferiorOk());
        checklistExistente.setTampaInferiorFaltantes(checklistAtualizado.getTampaInferiorFaltantes());
        checklistExistente.setPortasFuncionando(checklistAtualizado.getPortasFuncionando());
        checklistExistente.setDobradicasOk(checklistAtualizado.getDobradicasOk());
        checklistExistente.setManchasTela(checklistAtualizado.getManchasTela());
        checklistExistente.setTrincosTela(checklistAtualizado.getTrincosTela());
        checklistExistente.setCameraFunciona(checklistAtualizado.getCameraFunciona());
        checklistExistente.setMicrofoneFunciona(checklistAtualizado.getMicrofoneFunciona());
        checklistExistente.setAltoFalanteFunciona(checklistAtualizado.getAltoFalanteFunciona());
        checklistExistente.setBateria(checklistAtualizado.getBateria());

        // 🗃️ Chamados e status
        checklistExistente.setChamadoDell(checklistAtualizado.getChamadoDell());
        checklistExistente.setStatusChamadoDell(checklistAtualizado.getStatusChamadoDell());
        checklistExistente.setChamadoOTRS(checklistAtualizado.getChamadoOTRS());
        checklistExistente.setStatusInterno(checklistAtualizado.getStatusInterno());

        // 📦 Origem e histórico
        checklistExistente.setHistoricoNotebook(checklistAtualizado.getHistoricoNotebook());
        checklistExistente.setLocalizacao(checklistAtualizado.getLocalizacao());
        checklistExistente.setDemanda(checklistAtualizado.getDemanda());
        checklistExistente.setTag(checklistAtualizado.getTag());
        checklistExistente.setDepara(checklistAtualizado.getDepara());
        checklistExistente.setLocalidade(checklistAtualizado.getLocalidade());

        // 🖋️ Observações
        checklistExistente.setObservacoes(checklistAtualizado.getObservacoes());

        checklistService.salvarChecklist(checklistExistente, new MultipartFile[0]);
        return "redirect:/checklists/" + id;
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

    @GetMapping("/checklists/exportar-avancado")
    public void exportarAvancado(
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) String patrimonio,
            @RequestParam(required = false, name = "historico_notebook") String historicoNotebook,
            @RequestParam(required = false) String localizacao,
            @RequestParam(required = false) Boolean carcaca,
            @RequestParam(required = false) Boolean tecladoFunciona,
            HttpServletResponse response
    ) throws IOException {

        // 🔹 Busca dinâmica com todos os filtros
        List<Checklist> resultados = checklistService.buscarAvancado(
                modelo, patrimonio, historicoNotebook, localizacao, carcaca, tecladoFunciona);

        if (resultados.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Nenhum registro encontrado com os filtros aplicados.");
            return;
        }

        // 🔸 Configura retorno do PDF
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=checklists_filtrados.pdf");

        Document doc = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(doc, response.getOutputStream());
        doc.open();

        Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font textFont = new Font(Font.HELVETICA, 10);

        doc.add(new Paragraph("Exportação Avançada de Checklists", titleFont));
        doc.add(new Paragraph(" "));

        // 🔹 Tabela agora com 7 colunas (acrescentamos teclado)
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);

        table.addCell(new PdfPCell(new Phrase("Modelo", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Patrimônio", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Histórico", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Localização", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Carcaça OK", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Teclado OK", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Data", headerFont)));

        for (Checklist c : resultados) {
            table.addCell(new Phrase(c.getModelo() != null ? c.getModelo() : "-", textFont));
            table.addCell(new Phrase(c.getPatrimonio() != null ? c.getPatrimonio() : "-", textFont));
            table.addCell(new Phrase(c.getHistoricoNotebook() != null ? c.getHistoricoNotebook() : "-", textFont));
            table.addCell(new Phrase(c.getLocalizacao() != null ? c.getLocalizacao() : "-", textFont));
            table.addCell(new Phrase(c.getCarcaca() != null ? (c.getCarcaca() ? "Sim" : "Não") : "-", textFont));
            table.addCell(new Phrase(c.getTecladoFunciona() != null ? (c.getTecladoFunciona() ? "Sim" : "Não") : "-", textFont));
            table.addCell(new Phrase(c.getDataCriacao() != null ? c.getDataCriacao().toString() : "-", textFont));
        }

        doc.add(table);
        doc.close();
    }

    @GetMapping("/checklists/exportar-avancado-xls")
    public void exportarAvancadoXls(
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) String patrimonio,
            @RequestParam(required = false, name = "service_tag") String serviceTag,
            @RequestParam(required = false, name = "historico_notebook") String historicoNotebook,
            @RequestParam(required = false) String localizacao,
            @RequestParam(required = false) Boolean carcaca,
            @RequestParam(required = false) Boolean tecladoRuim,
            HttpServletResponse response
    ) throws IOException {

        List<Checklist> resultados = checklistService.buscarAvancado(
                modelo, patrimonio, historicoNotebook, localizacao, carcaca, tecladoRuim);

        if (resultados.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Nenhum registro encontrado com os filtros aplicados.");
            return;
        }

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            String abaNome = "Checklists";
            if (modelo != null && !modelo.isEmpty()) abaNome += " - " + modelo;
            var sheet = workbook.createSheet(abaNome);

            // 🔹 Estilos
            var headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());

            var headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);

            var zebraStyle = workbook.createCellStyle();
            zebraStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            zebraStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            var borderStyle = workbook.createCellStyle();
            borderStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            borderStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            borderStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            borderStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);

            // 🔹 Cabeçalhos atualizados
            String[] colunas = {"Modelo", "Patrimônio","Service Tag", "Histórico", "Localização", "Carcaça OK", "Teclado Ruim", "Data"};
            var header = sheet.createRow(0);
            for (int i = 0; i < colunas.length; i++) {
                var cell = header.createCell(i);
                cell.setCellValue(colunas[i]);
                cell.setCellStyle(headerStyle);
            }

            // 🔹 Preenche linhas
            int rowNum = 1;
            for (Checklist c : resultados) {
                var row = sheet.createRow(rowNum++);

                String[] valores = {
                        c.getModelo() != null ? c.getModelo() : "",
                        c.getPatrimonio() != null ? c.getPatrimonio() : "",
                        c.getServiceTag() != null ? c.getServiceTag() : "",
                        c.getHistoricoNotebook() != null ? c.getHistoricoNotebook() : "",
                        c.getLocalizacao() != null ? c.getLocalizacao() : "",
                        c.getCarcaca() != null ? (c.getCarcaca() ? "Sim" : "Não") : "",
                        c.getTecladoFunciona() != null ? (c.getTecladoFunciona() ? "Sim" : "Não") : "",
                        c.getDataCriacao() != null ? c.getDataCriacao().toString() : ""
                };

                for (int i = 0; i < valores.length; i++) {
                    var cell = row.createCell(i);
                    cell.setCellValue(valores[i]);
                    if (rowNum % 2 == 0) cell.setCellStyle(zebraStyle);
                    else cell.setCellStyle(borderStyle);
                }
            }

            // 🔹 Ajustes visuais
            for (int i = 0; i < colunas.length; i++) sheet.autoSizeColumn(i);
            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, rowNum - 1, 0, colunas.length - 1));

            // 🔸 Retorno HTTP
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=checklists_filtrados.xlsx");

            workbook.write(response.getOutputStream());
        }
    }

    @GetMapping("/checklists/{id}/dell")
    public String verInfoDell(@PathVariable Long id, Model model){
        Checklist checklist = checklistService.buscarPorId(id).orElseThrow(() -> new RuntimeException("Checklist não encontrado"));
        model.addAttribute("checklist", checklist);
        return "checklist-dell-detalhes";
    }

    // GET → abre a página de edição (formulário)
    @GetMapping("/checklists/{id}/editar-dell")
    public String exibirFormularioEditarDell(@PathVariable Long id, Model model) {
        Checklist checklist = checklistService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Checklist não encontrado"));
        model.addAttribute("checklist", checklist);
        return "editar-dell"; // nome do HTML com o formulário
    }

    // POST → salva o que foi editado
    @PostMapping("/checklists/{id}/editar-dell")
    public String editarDell(@PathVariable Long id,
                             @ModelAttribute Checklist checklistAtualizado) throws IOException {
        Optional<Checklist> checklistOpt = checklistService.buscarPorId(id);
        if (checklistOpt.isEmpty()) {
            return "redirect:/checklists?erro=notfound";
        }

        Checklist checklistExistente = checklistOpt.get();

        checklistExistente.setChamadoDell(checklistAtualizado.getChamadoDell());
        checklistExistente.setStatusChamadoDell(checklistAtualizado.getStatusChamadoDell());
        checklistExistente.setStatusInterno(checklistAtualizado.getStatusInterno());

        checklistService.salvarChecklist(checklistExistente, new MultipartFile[0]);

        return "redirect:/checklists/" + id + "/dell?sucesso=true";
    }







}
