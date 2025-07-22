package com.guijas1.checklistDell.repository;

import com.guijas1.checklistDell.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChecklistRepository extends JpaRepository<Checklist, Long> {


}
