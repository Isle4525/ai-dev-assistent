package com.example.ai_dev_assistent.dto;

import java.util.List;

public record CodeReviewReport(
        String repository,
        int qualityScore,
        String summary,
        String architectureSummary,
        List<ReviewFinding> findings,
        List<String> strengths,
        List<String> priorityRecommendations
) {
    public record ReviewFinding(
            String filePath,
            String category,
            String severity,
            String title,
            String description,
            String codeSnippet,
            String suggestion
    ) {}
}
