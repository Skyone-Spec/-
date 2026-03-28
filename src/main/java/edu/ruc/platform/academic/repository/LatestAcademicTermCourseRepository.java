package edu.ruc.platform.academic.repository;

import edu.ruc.platform.academic.domain.LatestAcademicTermCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LatestAcademicTermCourseRepository extends JpaRepository<LatestAcademicTermCourse, Long> {
}
