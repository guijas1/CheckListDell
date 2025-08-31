package com.guijas1.checklistDell.repository;

import com.guijas1.checklistDell.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChecklistRepository extends JpaRepository<Checklist, Long> {


    List<Checklist> findByPatrimonio (String patrimonio);

    List<Checklist> findByhistoricoNotebook (String historicoNotebook);

    List<Checklist> findByModelo(String modelo);

}
