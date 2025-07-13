package repository;

import model.MatchScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchScoreRepository extends JpaRepository<MatchScore, Long> {
    List<MatchScore> findByJobRequirementIdOrderByOverallScoreDesc(Long jobRequirementId);

    Optional<MatchScore> findByResumeIdAndJobRequirementId(Long resumeId, Long jobRequirementId);
    List<MatchScore> findByResumeId(Long resumeId);
}
