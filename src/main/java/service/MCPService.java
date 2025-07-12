package service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dto.MCPRequest;
import dto.MCPResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MCPService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${mcp.server.url}")
    private String mcpServerUrl;

    @Value("${mcp.server.timeout:30}")
    private int timeoutSeconds;

    public MCPResponse.ResumeAnalysisResult analyzeResume(MCPRequest.AnalyzeCVRequest request) {
        try {
            // Create MCP request
            MCPRequest mcpRequest = new MCPRequest();
            mcpRequest.setMethod("analyze_resume");
            mcpRequest.setId(UUID.randomUUID().toString());

            Map<String, Object> params = new HashMap<>();
            params.put("resume_text", request.getCvText());
            params.put("job_description", request.getJobDescription());
            params.put("required_skills", request.getRequiredSkills());
            params.put("preferred_skills", request.getPreferredSkills());
            params.put("min_experience", request.getMinExperience());
            params.put("education_requirements", request.getEducationRequirements());
            params.put("analysis_type", request.getAnalysisType());

            mcpRequest.setParams(params);

            // Send request to MCP server
            MCPResponse response = webClient.post()
                    .uri(mcpServerUrl + "/analyze")
                    .bodyValue(mcpRequest)
                    .retrieve()
                    .bodyToMono(MCPResponse.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (response == null) {
                throw new RuntimeException("No response received from MCP server");
            }

            if (response.getError() != null) {
                throw new RuntimeException("MCP Error: " + response.getError().getMessage());
            }

            // Convert response to ResumeAnalysisResult
            return objectMapper.convertValue(response.getResult(), MCPResponse.ResumeAnalysisResult.class);

        } catch (WebClientResponseException e) {
            log.error("Error calling MCP server: {}", e.getMessage());
            throw new RuntimeException("Failed to analyze resume: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during resume analysis: {}", e.getMessage());
            throw new RuntimeException("Resume analysis failed: " + e.getMessage(), e);
        }
    }

    public MCPResponse.ResumeAnalysisResult extractResumeData(String resumeText) {
        MCPRequest.AnalyzeCVRequest request = new MCPRequest.AnalyzeCVRequest();
        request.setCvText(resumeText);
        request.setAnalysisType("extract");

        return analyzeResume(request);
    }

    public MCPResponse.ResumeAnalysisResult scoreResumeMatch(String resumeText, String jobDescription,
                                                             Map<String, Object> jobRequirements) {
        MCPRequest.AnalyzeCVRequest request = new MCPRequest.AnalyzeCVRequest();
        request.setCvText(resumeText);
        request.setJobDescription(jobDescription);
        request.setRequiredSkills((java.util.List<String>) jobRequirements.get("requiredSkills"));
        request.setPreferredSkills((java.util.List<String>) jobRequirements.get("preferredSkills"));
        request.setMinExperience((Double) jobRequirements.get("minExperience"));
        request.setEducationRequirements((java.util.List<String>) jobRequirements.get("educationRequirements"));
        request.setAnalysisType("score");

        return analyzeResume(request);
    }
}
