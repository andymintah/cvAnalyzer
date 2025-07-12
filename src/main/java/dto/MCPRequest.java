package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MCPRequest {
    private String method;
    private String id;
    private Map<String, Object> params;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyzeCVRequest {
        private String cvText;
        private String jobDescription;
        private List<String> requiredSkills;
        private List<String> preferredSkills;
        private Double minExperience;
        private List<String> educationRequirements;
        private String analysisType; // "extract", "match", "score"
    }
}
