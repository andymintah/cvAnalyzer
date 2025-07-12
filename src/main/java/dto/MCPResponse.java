package dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MCPResponse {
    private String id;
    private Map<String, Object> result;
    private MCPError error;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MCPError {
        private int code;
        private String message;
        private Object data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumeAnalysisResult {
        private String candidateName;
        private String email;
        private String phone;
        private Double experienceYears;
        private List<String> skills;
        private String education;
        private String workExperience;
        private List<String> achievements;
        private List<String> certifications;
        private Double overallScore;
        private Double skillMatchScore;
        private Double experienceScore;
        private Double educationScore;
        private Double keywordMatchScore;
        private Double achievementScore;
        private Double confidence;
        private String reasoning;
        private List<String> matchedSkills;
        private List<String> missingSkills;
        private List<String> recommendations;
    }
}
