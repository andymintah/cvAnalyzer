package repository;

import model.CvData;
import model.JobRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CVDataRepository extends JpaRepository<CvData, Long> {
    List<CvData> findByCandidateNameContainingIgnoreCase(String candidateName);

    List<CvData> findBySkillsContaining(String skill);

    List<CvData> findByExperienceYearsGreaterThanEqual(Double minExperience);
    List<CvData> findByEducationContainingIgnoreCase(String education);

    Optional<JobRequirement> findJobRequirementById(Long id);

}
