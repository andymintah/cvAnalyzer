package service;
import dto.MCPResponse;
import model.JobRequirement;
import model.MatchScore;
import model.CvData;
import repository.CVDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import repository.MatchScoreRepository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CVAnalysisService {
    private final DocumentParsingService documentParsingService;
    private final MCPService mcpService;
    private final CVDataRepository cvRepository;
    private final MatchScoreRepository matchScoreRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public CvData processResume(MultipartFile file) throws IOException {
        log.info("Processing resume file: {}", file.getOriginalFilename());

        // Extract text from file
        String resumeText = documentParsingService.extractTextFromFile(file);

        // Store file
        String filePath = fileStorageService.storeFile(file);

        // Use MCP/LLM to extract structured data
        MCPResponse.ResumeAnalysisResult analysisResult = mcpService.extractResumeData(resumeText);

        // Create ResumeData entity
        CvData resumeData = new CvData();
        resumeData.setFileName(file.getOriginalFilename());
        resumeData.setFilePath(filePath);
        resumeData.setFullText(resumeText);
        resumeData.setCandidateName(analysisResult.getCandidateName());
        resumeData.setEmail(analysisResult.getEmail());
        resumeData.setPhone(analysisResult.getPhone());
        resumeData.setExperienceYears(analysisResult.getExperienceYears());
        resumeData.setSkills(analysisResult.getSkills());
        resumeData.setEducation(analysisResult.getEducation());
        resumeData.setWorkExperience(analysisResult.getWorkExperience());
        resumeData.setAchievements(String.join("; ", analysisResult.getAchievements()));
        resumeData.setCertifications(String.join("; ", analysisResult.getCertifications()));

        return cvRepository.save(resumeData);
    }

    @Transactional
    public MatchScore analyzeResumeMatch(Long resumeId, Long jobRequirementId) throws Throwable {
        log.info("Analyzing resume match for resume ID: {} and job ID: {}", resumeId, jobRequirementId);

        CvData resume = cvRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        JobRequirement jobRequirement = cvRepository.findJobRequirementById(jobRequirementId)
                .orElseThrow(() -> new RuntimeException("Job requirement not found"));

        // Prepare job requirements for MCP service
        Map<String, Object> jobReqs = new HashMap<>();
        jobReqs.put("requiredSkills", jobRequirement.getRequiredSkills());
        jobReqs.put("preferredSkills", jobRequirement.getPreferredSkills());
        jobReqs.put("minExperience", jobRequirement.getMinExperience());
        jobReqs.put("educationRequirements", jobRequirement.getEducationRequirements());

        // Use MCP/LLM to score the match
        MCPResponse.ResumeAnalysisResult analysisResult = mcpService.scoreResumeMatch(
                resume.getFullText(),
                jobRequirement.getJobDescription(),
                jobReqs
        );

        // Create MatchScore entity
        MatchScore matchScore = new MatchScore();
        matchScore.setResume(resume);
        matchScore.setJobRequirement(jobRequirement);
        matchScore.setOverallScore(analysisResult.getOverallScore());
        matchScore.setSkillMatchScore(analysisResult.getSkillMatchScore());
        matchScore.setExperienceScore(analysisResult.getExperienceScore());
        matchScore.setEducationScore(analysisResult.getEducationScore());
        matchScore.setKeywordMatchScore(analysisResult.getKeywordMatchScore());
        matchScore.setAchievementScore(analysisResult.getAchievementScore());
        matchScore.setConfidence(analysisResult.getConfidence());
        matchScore.setExplanation(String.join("; ", analysisResult.getRecommendations()));
        matchScore.setLlmReasoning(analysisResult.getReasoning());

        return matchScoreRepository.save(matchScore);
    }

    @Async
    public CompletableFuture<List<MatchScore>> batchAnalyzeResumes(List<Long> resumeIds, Long jobRequirementId) {
        log.info("Batch analyzing {} resumes for job ID: {}", resumeIds.size(), jobRequirementId);

        List<MatchScore> results = resumeIds.parallelStream()
                .map(resumeId -> {
                    try {
                        return analyzeResumeMatch(resumeId, jobRequirementId);
                    } catch (Exception e) {
                        log.error("Error analyzing resume {}: {}", resumeId, e.getMessage());
                        return null;
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return CompletableFuture.completedFuture(results);
    }

    @Cacheable(value = "rankedResumes", key = "#jobRequirementId")
    public List<MatchScore> getRankedResumes(Long jobRequirementId) {
        return matchScoreRepository.findByJobRequirementIdOrderByOverallScoreDesc(jobRequirementId);
    }

    public List<MatchScore> getRankedResumes(Long jobRequirementId, int limit) {
        return matchScoreRepository.findByJobRequirementIdOrderByOverallScoreDesc(jobRequirementId)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
}
