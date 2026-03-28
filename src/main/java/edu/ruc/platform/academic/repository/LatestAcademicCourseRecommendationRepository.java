package edu.ruc.platform.academic.repository;

import edu.ruc.platform.academic.domain.LatestAcademicCourseRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LatestAcademicCourseRecommendationRepository extends JpaRepository<LatestAcademicCourseRecommendation, Long> {

    List<LatestAcademicCourseRecommendation> findByReportId(Long reportId);
}
