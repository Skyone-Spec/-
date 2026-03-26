package edu.ruc.platform.admin.repository;

import edu.ruc.platform.admin.domain.DataImportErrorItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataImportErrorItemRepository extends JpaRepository<DataImportErrorItem, Long> {

    List<DataImportErrorItem> findByTaskIdOrderByRowNumberAscCreatedAtAsc(Long taskId);
}
