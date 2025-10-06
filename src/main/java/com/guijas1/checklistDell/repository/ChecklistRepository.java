package com.guijas1.checklistDell.repository;

import com.guijas1.checklistDell.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Objects;

public interface ChecklistRepository extends JpaRepository<Checklist, Long> {

    // Lista de métodos para o cadastro do checklist e busca
    List<Checklist> findByPatrimonio (String patrimonio);
    @Query("SELECT c FROM Checklist c WHERE LOWER(c.historicoNotebook) LIKE LOWER(CONCAT('%', :historicoNotebook, '%'))")
    List<Checklist> findByHistoricoNotebook(@Param("historicoNotebook") String historicoNotebook);

    List<Checklist> findByModelo(String modelo);

    @Query("SELECT c FROM Checklist c " +
            "WHERE c.localizacao IS NOT NULL " +
            "AND c.localizacao <> '' " +
            "AND LOWER(c.localizacao) LIKE LOWER(CONCAT('%', :localizacao, '%'))")
    List<Checklist> findByLocalizacao(@Param("localizacao") String localizacao);


    //Lista de métodos para os KPI's
    @Query("SELECT c.modelo, COUNT(c) FROM Checklist c GROUP BY c.modelo")
    List<Object[]> countForModel();
    @Query("SELECT c.localizacao, COUNT(c) FROM Checklist c WHERE c.localizacao IS NOT NULL GROUP BY c.localizacao")
    List<Object[]> countForLocation();
    @Query("SELECT c.liga, COUNT(c) FROM Checklist c GROUP BY c.liga")
    List<Object[]> contarPorStatus();
    @Query("SELECT c.carcaca, COUNT(c) FROM Checklist c GROUP BY c.carcaca")
    List<Object[]> countForCarca();

}
