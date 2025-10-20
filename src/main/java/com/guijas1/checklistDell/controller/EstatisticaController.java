package com.guijas1.checklistDell.controller;

import com.guijas1.checklistDell.service.EstatisticaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/KPI")
public class EstatisticaController {

    @Autowired
    private EstatisticaService estatisticaService;

    public EstatisticaController(EstatisticaService estatisticaService){
        this.estatisticaService = estatisticaService;
    }

    @GetMapping("/geral")
    public Map<String, Object> generalStats(){
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", estatisticaService.countAll());
        stats.put("porModelo", estatisticaService.contarPorModelo());
        stats.put("porLocalizacao", estatisticaService.contarPorLocalizacao());
        stats.put("porStatus", estatisticaService.contarPorStatus());
        stats.put("porCarcaça", estatisticaService.contarPorCarcaca());
        return stats;
    }
}
