package com.guijas1.checklistDell.service;

import com.guijas1.checklistDell.entity.Checklist;
import com.guijas1.checklistDell.repository.ChecklistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChecklistService {

    @Autowired
    private ChecklistRepository checklistRepository;

    public ChecklistService(ChecklistRepository checklistRepository) {
        this.checklistRepository = checklistRepository;
    }

    public Checklist salvarChecklist(Checklist checklist) {
        return checklistRepository.save(checklist);
    }

    public List<Checklist> listarTodos() {
        return checklistRepository.findAll();
    }

    public Optional<Checklist> buscarPorId(Long id) {
        return checklistRepository.findById(id);
    }

}
