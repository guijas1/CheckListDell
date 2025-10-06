package com.guijas1.checklistDell.service;

import com.guijas1.checklistDell.entity.Checklist;
import com.guijas1.checklistDell.repository.ChecklistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ChecklistService {

    @Autowired
    private ChecklistRepository checklistRepository;

    @Autowired
    private S3Service s3Service;

    public List<Checklist> listarTodos() {
        return checklistRepository.findAll();
    }

    public Optional<Checklist> buscarPorId(Long id) {
        return checklistRepository.findById(id);
    }

    public Checklist salvarChecklist(Checklist checklist, MultipartFile[] fotos) throws IOException {
        if (fotos != null && fotos.length > 0) {
            List<String> caminhos = new ArrayList<>();

            for (MultipartFile foto : fotos) {
                if (!foto.isEmpty()) {
                    String url = s3Service.uploadImagem(foto);
                    caminhos.add(url);
                }
            }

            checklist.setFotoPath(caminhos); // agora é uma lista mesmo
        }

        return checklistRepository.save(checklist);
    }




    public void excluirChecklist(Long id) {
        checklistRepository.deleteById(id);
    }

    public Page<Checklist> buscarPorModelo(String modelo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return checklistRepository.findByModeloContainingIgnoreCase(modelo, pageable);
    }

    public Page<Checklist> buscarPorPatrimonio(String patrimonio, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return checklistRepository.findByPatrimonioContainingIgnoreCase(patrimonio, pageable);
    }

    public Page<Checklist> buscarPorOrigem(String historicoNotebook, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return checklistRepository.findByHistoricoNotebookContainingIgnoreCase(historicoNotebook, pageable);
    }

    // ✅ versão paginada
    public Page<Checklist> buscarPorLocalizacao(String localizacao, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return checklistRepository.findByLocalizacaoContainingIgnoreCase(localizacao, pageable);
    }

    // ✅ versão sem paginação (para QR Code)
    public List<Checklist> buscarPorLocalizacao(String localizacao) {
        return checklistRepository.findByLocalizacao(localizacao);
    }



    public List<String> listarLocalizacoes(){
        return checklistRepository.findAll().stream().map(Checklist::getLocalizacao).filter(Objects::nonNull).distinct().toList();
    }

    public Page<Checklist> listarPaginado(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return checklistRepository.findAll(pageable);
    }

}
