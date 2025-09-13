package com.guijas1.checklistDell.service;

import com.guijas1.checklistDell.entity.Checklist;
import com.guijas1.checklistDell.repository.ChecklistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    public List<Checklist> buscarPorPatrimonio(String patrimonio){
        return checklistRepository.findByPatrimonio(patrimonio);
    }

    public List<Checklist> buscarPorOrigem(String historicoNotebook){
        return checklistRepository.findByHistoricoNotebook(historicoNotebook);
    }

    public List<Checklist> buscarPorModelo(String modelo){
        return checklistRepository.findByModelo(modelo);
    }
}
