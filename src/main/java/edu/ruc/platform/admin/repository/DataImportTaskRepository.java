package edu.ruc.platform.admin.repository;

import edu.ruc.platform.admin.domain.DataImportTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataImportTaskRepository extends JpaRepository<DataImportTask, Long> {
}
