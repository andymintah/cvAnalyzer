package model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "match_scores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private CvData resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_requirement_id", nullable = false)
    private JobRequirement jobRequirement;

    @Column(name = "overall_score")
    private Double overallScore;

    @Column(name = "skill_match_score")
    private Double skillMatchScore;

    @Column(name = "experience_score")
    private Double experienceScore;

    @Column(name = "education_score")
    private Double educationScore;

    @Column(name = "keyword_match_score")
    private Double keywordMatchScore;

    @Column(name = "achievement_score")
    private Double achievementScore;

    private Double confidence;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(columnDefinition = "TEXT")
    private String llmReasoning;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
