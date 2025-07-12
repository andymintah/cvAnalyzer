package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "job_requirements")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class JobRequirement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String jobTitle;

    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    @ElementCollection
    @CollectionTable(name = "required_skills", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill")
    private List<String> requiredSkills;

    @ElementCollection
    @CollectionTable(name = "preferred_skills", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill")
    private List<String> preferredSkills;

    @Column(name = "min_experience")
    private Double minExperience;

    @ElementCollection
    @CollectionTable(name = "education_requirements", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "requirement")
    private List<String> educationRequirements;

    @ElementCollection
    @CollectionTable(name = "must_have_keywords", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "keyword")
    private List<String> mustHaveKeywords;

    @ElementCollection
    @CollectionTable(name = "nice_to_have_keywords", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "keyword")
    private List<String> niceToHaveKeywords;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
