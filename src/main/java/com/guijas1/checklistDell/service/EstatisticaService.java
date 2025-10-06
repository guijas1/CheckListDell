package com.guijas1.checklistDell.service;

import com.guijas1.checklistDell.repository.ChecklistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class EstatisticaService {
    @Autowired
    private ChecklistRepository checklistRepository;

    public EstatisticaService (ChecklistRepository checklistRepository){
        this.checklistRepository = checklistRepository;
    }

    public long countAll(){
        return checklistRepository.count();
    }

    public Map<String, Long> contarPorModelo() {
        return checklistRepository.countForModel().stream()
                .collect(Collectors.toMap(
                        r -> r[0].toString(),
                        r -> ((Number) r[1]).longValue()
                ));
    }

    public Map<String, Long> contarPorLocalizacao() {
        return checklistRepository.countForLocation().stream()
                .collect(Collectors.toMap(
                        r -> r[0] == null ? "Sem Localização" : r[0].toString(),
                        r -> ((Number) r[1]).longValue()
                ));
    }


    public Map<String, Long> contarPorStatus() {
        return checklistRepository.contarPorStatus().stream()
                .collect(Collectors.toMap(
                        r -> String.valueOf(r[0]),
                        r -> ((Number) r[1]).longValue()
                ));
    }

    public Map<String, Long> contarPorCarcaca() {
        return checklistRepository.countForCarca().stream()
                .collect(Collectors.toMap(
                        r -> String.valueOf(r[0]),
                        r -> ((Number) r[1]).longValue()
                ));
    }
}
