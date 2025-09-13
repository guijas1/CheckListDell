package com.guijas1.checklistDell.repository;

import com.guijas1.checklistDell.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChecklistRepository extends JpaRepository<Checklist, Long> {


    List<Checklist> findByPatrimonio (String patrimonio);
    @Query("SELECT c FROM Checklist c WHERE LOWER(c.historicoNotebook) LIKE LOWER(CONCAT('%', :historicoNotebook, '%'))")
    List<Checklist> findByHistoricoNotebook(@Param("historicoNotebook") String historicoNotebook);

    List<Checklist> findByModelo(String modelo);

}
